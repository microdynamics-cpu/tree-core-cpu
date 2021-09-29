package treecorel2

import chisel3._
import chisel3.util._
import AXI4Bridge._

class AXI4SigBridge extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    val rw:  AXI4USERIO = new AXI4USERIO
    val axi: AXI4IO     = new AXI4IO
  })

  protected val wtTrans: Bool = WireDefault(io.rw.req === AxiReqWt.U)
  protected val rdTrans: Bool = WireDefault(io.rw.req === AxiReqRd.U)

  // valid triggered when multi master start an r/w oper and send valid signal in meantime
  // this time, master start a r/w transition
  protected val wtValid: Bool = WireDefault(wtTrans && io.rw.valid)
  protected val rdValid: Bool = WireDefault(rdTrans && io.rw.valid)

  // handshake sign come from axi
  protected val awHdShk: Bool = WireDefault(io.axi.aw.valid && io.axi.aw.ready)
  protected val wHdShk:  Bool = WireDefault(io.axi.w.valid && io.axi.w.ready)
  protected val bHdShk:  Bool = WireDefault(io.axi.b.valid && io.axi.b.ready)
  protected val arHdShk: Bool = WireDefault(io.axi.ar.valid && io.axi.ar.ready)
  protected val rHdShk:  Bool = WireDefault(io.axi.r.valid && io.axi.r.ready)

  // after handshake, the transition end sign
  protected val wtDone:    Bool = WireDefault(wHdShk && io.axi.w.bits.last)
  protected val rdDone:    Bool = WireDefault(rHdShk && io.axi.r.bits.last) // according to id to identify the rd master
  protected val transDone: Bool = Mux(wtTrans, bHdShk, rdDone)

  val eumWtIDLE :: eumWtADDR :: eumWtWRITE :: eumWtRESP :: Nil = Enum(4)
  val wtOperState: UInt = RegInit(eumWtIDLE)

  when(wtValid) {
    switch(wtOperState) {
      is(eumWtIDLE) {
        wtOperState := eumWtADDR
      }
      is(eumWtADDR) {
        when(awHdShk) { wtOperState := eumWtWRITE }
      }
      is(eumWtWRITE) {
        when(wtDone) { wtOperState := eumWtRESP }
      }
      is(eumWtRESP) {
        when(bHdShk) { wtOperState := eumWtIDLE }
      }
    }
  }

  // read oper
  val eumRdIDLE :: eumRdADDR :: eumRdREAD :: eumRdIDLE2 :: eumRdIDLE3 :: Nil = Enum(5)
  val rdOperState: UInt = RegInit(eumRdIDLE)

  when(rdValid) {
    switch(rdOperState) {
      is(eumRdIDLE) {
        rdOperState := eumRdADDR
        // printf(p"[if2id]io.instOut.data = 0x${Hexadecimal(if2id.io.instOut.data)}\n")
        // printf("[sig] eumRdADDR\n")
      }
      is(eumRdADDR) {
        when(arHdShk) {
          rdOperState := eumRdREAD
          // printf("[sig] eumRdREAD\n")
        }
      }
      is(eumRdREAD) {
        when(rdDone) {
          rdOperState := eumRdIDLE2
          // printf(p"[sig]io.rw.rdata = 0x${Hexadecimal(io.rw.rdata)}\n")
        }
      }
      is(eumRdIDLE2) {
        rdOperState := eumRdIDLE3
      }
      is(eumRdIDLE3) {
        rdOperState := eumRdIDLE
        // printf("[sig] eumRdIDLE\n")
      }
    }
  }

  protected val ALIGNED_WIDTH = 3 // eval: log2(AxiDataWidth / 8)
  protected val OFFSET_WIDTH  = 6 // eval: log2(AxiDataWidth)
  protected val AXI_SIZE      = 3.U // eval: log2(AxiDataWidth / 8)
  protected val MASK_WIDTH    = 128 // eval: AxiDataWidth * 2
  protected val TRANS_LEN     = 1 // eval: 1
  protected val BLOCK_TRANS   = false.B
  protected val aligned: Bool = WireDefault(io.rw.addr(ALIGNED_WIDTH - 1, 0) === 0.U)
  protected val addrOp1: UInt = Wire(UInt(4.W))
  addrOp1 := Cat(4.U - Fill(ALIGNED_WIDTH, 0.U), io.rw.addr(ALIGNED_WIDTH - 1, 0))
  protected val addrOp2: UInt = Wire(UInt(4.W))
  addrOp2 := Mux(
    io.rw.size === "b00".U,
    "b0000".U,
    Mux(io.rw.size === "b01".U, "b0001".U, Mux(io.rw.size === "b10".U, "b0011".U, "b0111".U))
  )

  protected val addrEnd: UInt = Wire(UInt(4.W))
  addrEnd := addrOp1 + addrOp2
  protected val overStep: Bool = WireDefault(addrEnd(3, ALIGNED_WIDTH) =/= 0.U)

  protected val axiLen: UInt = Wire(UInt(8.W))
  axiLen := Mux(aligned, (TRANS_LEN - 1).U, Cat(Fill(7, 0.U), overStep))

  protected val rwLen: UInt = RegInit(0.U(8.W))
  protected val rwLenRst: Bool = (wtTrans && (wtOperState === eumWtIDLE)) ||
    (rdTrans && (rdOperState === eumRdIDLE))
  protected val rwLenIncEna: Bool = (rwLen =/= axiLen) && (wHdShk || rHdShk)

  when(rwLenRst) {
    rwLen := 0.U
  }.elsewhen(rwLenIncEna) {
    rwLen := rwLen + 1.U
  }

  protected val axiSize: UInt = Wire(UInt(3.W))
  axiSize := 3.U // TODO: just for debug

  protected val axiAddr: UInt = Wire(UInt(AxiAddrWidth.W))
  axiAddr := Cat(io.rw.addr(AxiAddrWidth - 1, ALIGNED_WIDTH), Fill(ALIGNED_WIDTH, 0.U))

  protected val alignedOffsetLow: UInt = Wire(UInt(OFFSET_WIDTH.W))
  alignedOffsetLow := Cat(OFFSET_WIDTH.U - Fill(ALIGNED_WIDTH, 0.U), io.rw.addr(ALIGNED_WIDTH - 1, 0)) << 3

  protected val alignedOffsetHig: UInt = Wire(UInt(OFFSET_WIDTH.W))
  alignedOffsetHig := AxiDataWidth.U - alignedOffsetLow

  protected val mask: UInt = Wire(UInt(MASK_WIDTH.W))
  mask := Mux(
    io.rw.size === "b00".U,
    "hff".U << alignedOffsetLow,
    Mux(io.rw.size === "b01".U, "hffff".U << alignedOffsetLow, Mux(io.rw.size === "b10".U, "hffffffff".U, "hffffffffffffffff".U))
  )

  protected val maskLow: UInt = Wire(UInt(AxiDataWidth.W))
  maskLow := mask(AxiDataWidth - 1, 0)

  protected val maskHig: UInt = Wire(UInt(AxiDataWidth.W))
  maskHig := mask(MASK_WIDTH - 1, AxiDataWidth)

  protected val axiId: UInt = Wire(UInt(AxiIdLen.W))
  // axiId := Fill(AxiIdLen, 0.U)
  axiId := io.rw.id

  protected val axiUser: UInt = Wire(UInt(AxiUserLen.W))
  axiUser := Fill(AxiUserLen, 0.U)

  protected val rwReadyEna: Bool = Wire(Bool())
  protected val rwReady:    Bool = RegEnable(transDone, false.B, rwReadyEna)
  rwReadyEna  := transDone || rwReady
  io.rw.ready := rwReady

  protected val rwResp: UInt = RegEnable(Mux(wtTrans, io.axi.b.bits.resp, io.axi.r.bits.resp), 0.U, transDone)
  io.rw.resp := rwResp

  // ------------------Write Transaction------------------
  io.axi.aw.valid       := WireDefault(wtOperState === eumWtADDR)
  io.axi.aw.bits.addr   := axiAddr
  io.axi.aw.bits.prot   := AXI4Bridge.AXI_PROT_UNPRIVILEGED_ACCESS | AXI4Bridge.AXI_PROT_SECURE_ACCESS | AXI4Bridge.AXI_PROT_DATA_ACCESS
  io.axi.aw.bits.id     := axiId
  io.axi.aw.bits.user   := axiUser
  io.axi.aw.bits.len    := axiLen
  io.axi.aw.bits.size   := axiSize
  io.axi.aw.bits.burst  := AXI4Bridge.AXI_BURST_TYPE_INCR
  io.axi.aw.bits.lock   := 0.U
  io.axi.aw.bits.cache  := AXI4Bridge.AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE
  io.axi.aw.bits.qos    := 0.U
  io.axi.aw.bits.region := 0.U
  io.axi.w.valid        := WireDefault(wtOperState === eumWtWRITE)
  io.axi.w.bits.strb    := "b11111111".U
  io.axi.w.bits.last    := WireDefault(rwLen === axiLen)
  io.axi.w.bits.user    := axiUser
  io.axi.w.bits.id      := axiId
  // prepare write data
  protected val rwWtData: UInt = RegInit(0.U(AxiDataWidth.W))
  for (i <- 0 until TRANS_LEN) {
    when(io.axi.w.valid && io.axi.w.ready) {
      when((aligned === false.B) && overStep) {
        when(rwLen(0) === 1.U) {
          rwWtData := io.rw.wdata(AxiDataWidth - 1, 0) >> alignedOffsetLow
        }.otherwise {
          rwWtData := io.rw.wdata(AxiDataWidth - 1, 0) << alignedOffsetHig
        }
      }.elsewhen(rwLen === i.U) {
        rwWtData := io.rw.wdata((i + 1) * AxiDataWidth - 1, i * AxiDataWidth)
      }
    }
  }

  io.axi.w.bits.data := rwWtData
  io.axi.b.ready     := (wtOperState === eumWtRESP)

  // ------------------Read Transaction------------------
  io.axi.ar.valid       := WireDefault(rdOperState === eumRdADDR)
  io.axi.ar.bits.addr   := axiAddr
  io.axi.ar.bits.prot   := AXI4Bridge.AXI_PROT_UNPRIVILEGED_ACCESS | AXI4Bridge.AXI_PROT_SECURE_ACCESS | AXI4Bridge.AXI_PROT_DATA_ACCESS
  io.axi.ar.bits.id     := axiId
  io.axi.ar.bits.user   := axiUser
  io.axi.ar.bits.len    := axiLen
  io.axi.ar.bits.size   := axiSize
  io.axi.ar.bits.burst  := AXI4Bridge.AXI_BURST_TYPE_INCR
  io.axi.ar.bits.lock   := 0.U
  io.axi.ar.bits.cache  := AXI4Bridge.AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE
  io.axi.ar.bits.qos    := 0.U
  io.axi.ar.bits.region := 0.U

  io.axi.r.ready := WireDefault(rdOperState === eumRdREAD)

  //data transfer
  protected val axiRdDataLow: UInt = Wire(UInt(AxiDataWidth.W))
  axiRdDataLow := (io.axi.r.bits.data & maskLow) >> alignedOffsetLow
  protected val axiRdDataHig: UInt = Wire(UInt(AxiDataWidth.W))
  axiRdDataHig := (io.axi.r.bits.data & maskHig) << alignedOffsetHig

  // because now the TRANS_LEN is 1, so the data is one-dens
  protected val rwRdData: UInt = RegInit(0.U(AxiDataWidth.W))
  io.rw.rdata := rwRdData

  for (i <- 0 until TRANS_LEN) {
    when(io.axi.r.valid && io.axi.r.ready) {
      when((aligned === false.B) && overStep) {
        when(rwLen(0) === 1.U) {
          rwRdData := rwRdData | axiRdDataHig
        }.otherwise {
          rwRdData := axiRdDataLow
        }
      }.elsewhen(rwLen === i.U) {
        rwRdData := axiRdDataLow
      }
    }
  }
}
