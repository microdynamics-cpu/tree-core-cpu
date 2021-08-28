package treecorel2

import chisel3._
import chisel3.util._

object AXI4Bridge {
  // Burst types
  protected val AXI_BURST_TYPE_FIXED = "b00".U(2.W)
  protected val AXI_BURST_TYPE_INCR  = "b01".U(2.W)
  protected val AXI_BURST_TYPE_WRAP  = "b10".U(2.W)

// Access permissions
  protected val AXI_PROT_UNPRIVILEGED_ACCESS = "b000".U(3.W)
  protected val AXI_PROT_PRIVILEGED_ACCESS   = "b001".U(3.W)
  protected val AXI_PROT_SECURE_ACCESS       = "b000".U(3.W)
  protected val AXI_PROT_NON_SECURE_ACCESS   = "b010".U(3.W)
  protected val AXI_PROT_DATA_ACCESS         = "b000".U(3.W)
  protected val AXI_PROT_INSTRUCTION_ACCESS  = "b100".U(3.W)
// Memory types (AR)
  protected val AXI_ARCACHE_DEVICE_NON_BUFFERABLE                 = "b0000".U(4.W)
  protected val AXI_ARCACHE_DEVICE_BUFFERABLE                     = "b0001".U(4.W)
  protected val AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE   = "b0010".U(4.W)
  protected val AXI_ARCACHE_NORMAL_NON_CACHEABLE_BUFFERABLE       = "b0011".U(4.W)
  protected val AXI_ARCACHE_WRITE_THROUGH_NO_ALLOCATE             = "b1010".U(4.W)
  protected val AXI_ARCACHE_WRITE_THROUGH_READ_ALLOCATE           = "b1110".U(4.W)
  protected val AXI_ARCACHE_WRITE_THROUGH_WRITE_ALLOCATE          = "b1010".U(4.W)
  protected val AXI_ARCACHE_WRITE_THROUGH_READ_AND_WRITE_ALLOCATE = "b1110".U(4.W)
  protected val AXI_ARCACHE_WRITE_BACK_NO_ALLOCATE                = "b1011".U(4.W)
  protected val AXI_ARCACHE_WRITE_BACK_READ_ALLOCATE              = "b1111".U(4.W)
  protected val AXI_ARCACHE_WRITE_BACK_WRITE_ALLOCATE             = "b1011".U(4.W)
  protected val AXI_ARCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE    = "b1111".U(4.W)
// Memory types (AW)
  protected val AXI_AWCACHE_DEVICE_NON_BUFFERABLE                 = "b0000".U(4.W)
  protected val AXI_AWCACHE_DEVICE_BUFFERABLE                     = "b0001".U(4.W)
  protected val AXI_AWCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE   = "b0010".U(4.W)
  protected val AXI_AWCACHE_NORMAL_NON_CACHEABLE_BUFFERABLE       = "b0011".U(4.W)
  protected val AXI_AWCACHE_WRITE_THROUGH_NO_ALLOCATE             = "b0110".U(4.W)
  protected val AXI_AWCACHE_WRITE_THROUGH_READ_ALLOCATE           = "b0110".U(4.W)
  protected val AXI_AWCACHE_WRITE_THROUGH_WRITE_ALLOCATE          = "b1110".U(4.W)
  protected val AXI_AWCACHE_WRITE_THROUGH_READ_AND_WRITE_ALLOCATE = "b1110".U(4.W)
  protected val AXI_AWCACHE_WRITE_BACK_NO_ALLOCATE                = "b0111".U(4.W)
  protected val AXI_AWCACHE_WRITE_BACK_READ_ALLOCATE              = "b0111".U(4.W)
  protected val AXI_AWCACHE_WRITE_BACK_WRITE_ALLOCATE             = "b1111".U(4.W)
  protected val AXI_AWCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE    = "b1111".U(4.W)

  protected val AXI_SIZE_BYTES_1   = "b000".U(3.W)
  protected val AXI_SIZE_BYTES_2   = "b001".U(3.W)
  protected val AXI_SIZE_BYTES_4   = "b010".U(3.W)
  protected val AXI_SIZE_BYTES_8   = "b011".U(3.W)
  protected val AXI_SIZE_BYTES_16  = "b100".U(3.W)
  protected val AXI_SIZE_BYTES_32  = "b101".U(3.W)
  protected val AXI_SIZE_BYTES_64  = "b110".U(3.W)
  protected val AXI_SIZE_BYTES_128 = "b111".U(3.W)

  // need to access by PCReg module
  val SIZE_B = "b00".U(2.W)
  val SIZE_H = "b01".U(2.W)
  val SIZE_W = "b10".U(2.W)
  val SIZE_D = "b11".U(2.W)
}

class AXI4Bridge extends Module with InstConfig {
  val io = IO(new Bundle {
    val rwValidIn: Bool = Input(Bool())
    val rwReqIn:   UInt = Input(UInt(2.W))
    val wtDataIn:  UInt = Input(UInt(BusWidth.W))
    val rwAddrIn:  UInt = Input(UInt(AxiDataWidth.W))
    val rwSizeIn:  UInt = Input(UInt(AxiSizeLen.W))

    val rwReadyOut: Bool = Output(Bool())
    val rdDataOut:  UInt = Output(UInt(BusWidth.W))
    val rwRespOut:  UInt = Output(UInt(AxiRespLen.W))

    // write addr
    val axiAwReadyIn: Bool = Input(Bool())

    val axiAwValidOut:  Bool = Output(Bool())
    val axiAwAddrOut:   UInt = Output(UInt(AxiAddrWidth.W))
    val axiAwProtOut:   UInt = Output(UInt(AxiProtLen.W))
    val axiAwIdOut:     UInt = Output(UInt(AxiIdLen.W))
    val axiAwUserOut:   UInt = Output(UInt(AxiUserLen.W))
    val axiAwLenOut:    UInt = Output(UInt(8.W))
    val axiAwSizeOut:   UInt = Output(UInt(3.W))
    val axiAwBurstOut:  UInt = Output(UInt(2.W))
    val axiAwLockOut:   Bool = Output(Bool())
    val axiAwCacheOut:  UInt = Output(UInt(4.W))
    val axiAwQosOut:    UInt = Output(UInt(4.W))
    val axiAwRegionOut: UInt = Output(UInt(4.W))

    // write data
    val axiWtReadyIn: Bool = Input(Bool())

    val axiWtValidOut: Bool = Output(Bool())
    val axiWtDataOut:  UInt = Output(UInt(AxiDataWidth.W))
    val axiWtStrbOut:  UInt = Output(UInt((AxiDataWidth / 8).W))
    val axiWtLastOut:  Bool = Output(Bool())
    val axiWtUserOut:  UInt = Output(UInt(AxiUserLen.W))

    // write resp
    val axiWtbValidIn: Bool = Input(Bool())
    val axiWtbRespIn:  UInt = Input(UInt(AxiRespLen.W))
    val axWtbIdIn:     UInt = Input(UInt(AxiIdLen.W))
    val axiWtbUserIn:  UInt = Input(UInt(AxiUserLen.W))

    val axiWtbReadyOut: Bool = Output(Bool())

    // read addr
    val axiArReadyIn: Bool = Input(Bool())

    val axiArValidOut:  Bool = Output(Bool())
    val axiArAddrOut:   UInt = Output(UInt(AxiAddrWidth.W))
    val axiArProtOut:   UInt = Output(UInt(AxiProtLen.W))
    val axiArIdOut:     UInt = Output(UInt(AxiIdLen.W))
    val axiArUserOut:   UInt = Output(UInt(AxiUserLen.W))
    val axiArLenOut:    UInt = Output(UInt(8.W))
    val axiArSizeOut:   UInt = Output(UInt(3.W))
    val axiArBurstOut:  UInt = Output(UInt(2.W))
    val axiArLockOut:   Bool = Output(Bool())
    val axiArCacheOut:  UInt = Output(UInt(4.W))
    val axiArQosOut:    UInt = Output(UInt(4.W))
    val axiArRegionOut: UInt = Output(UInt(4.W))

    // read data
    val axiRdValidIn: Bool = Input(Bool())
    val axiRdRespIn:  UInt = Input(UInt(AxiRespLen.W))
    val axiRdDataIn:  UInt = Input(UInt(AxiDataWidth.W))
    val axiRdLastIn:  Bool = Input(Bool())
    val axiRdIdIn:    UInt = Input(UInt(AxiIdLen.W))
    val axiRdUserIn:  UInt = Input(UInt(AxiUserLen.W))

    val axiRdReadyOut: Bool = Output(Bool())
  })

  protected val wtTrans = WireDefault(io.rwReqIn === AxiReqWt.U)
  protected val rdTrans = WireDefault(io.rwReqIn === AxiReqRd.U)
  protected val wtValid = WireDefault(io.rwValidIn && wtTrans)
  protected val rdValid = WireDefault(io.rwValidIn && rdTrans)

  // handshake
  protected val awHdShk  = WireDefault(io.axiAwReadyIn && io.axiAwValidOut)
  protected val wtHdShk  = WireDefault(io.axiWtReadyIn && io.axiWtValidOut)
  protected val wtbHdShk = WireDefault(io.axiWtbReadyOut && io.axiWtbValidIn)
  protected val arHdShk  = WireDefault(io.axiArReadyIn && io.axiArValidOut)
  protected val rdHdShk  = WireDefault(io.axiRdReadyOut && io.axiRdValidIn)

  protected val wtDone = WireDefault(wtHdShk && io.axiWtLastOut)
  protected val rdDone = WireDefault(rdHdShk && io.axiRdLastIn)
  // TODO: 'transDone' is good name?
  protected val transDone = Mux(wtTrans, wtbHdShk, rdDone)

  // FSM for read/write
  protected val fsmWtIDLE  = 0.U(2.W)
  protected val fsmWtADDR  = 1.U(2.W)
  protected val fsmWtWRITE = 2.U(2.W)
  protected val fsmWtRESP  = 3.U(2.W)

  protected val fsmRdIDLE = 0.U(2.W)
  protected val fsmRdADDR = 1.U(2.W)
  protected val fsmRdREAD = 2.U(2.W)

  protected val wtStateReg = RegInit(fsmWtIDLE)
  protected val rdStateReg = RegInit(fsmRdIDLE)

  protected val wtStateIdle  = WireDefault(wtStateReg === fsmWtIDLE)
  protected val wtStateAddr  = WireDefault(wtStateReg === fsmWtADDR)
  protected val wtStateWrite = WireDefault(wtStateReg === fsmWtWRITE)
  protected val wtStateResp  = WireDefault(wtStateReg === fsmWtRESP)

  protected val rdStateIdle = WireDefault(rdStateReg === fsmRdIDLE)
  protected val rdStateAddr = WireDefault(rdStateReg === fsmRdADDR)
  protected val rdStateRead = WireDefault(rdStateReg === fsmRdREAD)

  switch(wtStateReg) {
    is(fsmWtIDLE) {
      when(wtValid) {
        wtStateReg := fsmWtADDR
      }
    }
    is(fsmWtADDR) {
      when(wtValid && awHdShk) {
        wtStateReg := fsmWtWRITE
      }
    }
    is(fsmWtWRITE) {
      when(wtValid && wtDone) {
        wtStateReg := fsmWtRESP
      }
    }
    is(fsmWtRESP) {
      when(wtValid && wtbHdShk) {
        wtStateReg := fsmWtIDLE
      }
    }
  }

  switch(rdStateReg) {
    is(fsmRdIDLE) {
      when(rdValid) {
        rdStateReg := fsmRdADDR
      }
    }
    is(fsmRdADDR) {
      when(rdValid && arHdShk) {
        rdStateReg := fsmRdREAD
      }
    }
    is(fsmRdREAD) {
      when(rdValid && rdDone) {
        rdStateReg := fsmRdIDLE
      }
    }
  }

  // ------------------Number of transmission------------------
  protected val transLen        = RegInit(0.U(8.W))
  protected val transLenReset   = WireDefault(this.reset.asBool() || (wtTrans && wtStateIdle) || (rdTrans && rdStateIdle))
  protected val axiLen          = Wire(UInt(8.W))
  protected val transLenIncrEna = WireDefault((transLen =/= axiLen) && (wtHdShk || rdHdShk))

  when(transLenReset) {
    transLen := 0.U;
  }.elsewhen(transLenIncrEna) {
    transLen := transLen + 1.U
  }

// ------------------Process Data------------------
  protected val ALIGNED_WIDTH = 3 // eval: log2(AxiDataWidth / 8)
  protected val OFFSET_WIDTH  = 6 // eval: log2(AxiDataWidth)
  protected val AXI_SIZE      = 3.U // eval: log2(AxiDataWidth / 8)
  protected val MASK_WIDTH    = AxiDataWidth * 2 // eval: 128
  protected val TRANS_LEN     = 1 // eval: 1
  protected val BLOCK_TRANS   = false.B

  // no-aligned visit
  protected val transAligned = WireDefault(BLOCK_TRANS || io.rwAddrIn(ALIGNED_WIDTH - 1, 0) === 0.U)
  protected val sizeByte     = WireDefault(io.rwSizeIn === AXI4Bridge.SIZE_B)
  protected val sizeHalf     = WireDefault(io.rwSizeIn === AXI4Bridge.SIZE_H)
  protected val sizeWord     = WireDefault(io.rwSizeIn === AXI4Bridge.SIZE_W)
  protected val sizeDouble   = WireDefault(io.rwSizeIn === AXI4Bridge.SIZE_D)
  // 0100xxx
  protected val addrOpA = WireDefault(UInt(4.W), Cat(4.U - Fill(ALIGNED_WIDTH, 0.U), io.rwAddrIn(ALIGNED_WIDTH - 1, 0)))
  // b: 0000
  // h: 0001
  // w: 0011
  // d: 0111
  protected val addrOpB = WireDefault(
    UInt(4.W),
    (Fill(4, sizeByte) & "b0".U(4.W))
      | (Fill(4, sizeHalf) & "b1".U(4.W))
      | (Fill(4, sizeWord) & "b11".U(4.W))
      | (Fill(4, sizeDouble) & "b111".U(4.W))
  )

  protected val addrEnd  = WireDefault(UInt(4.W), addrOpA + addrOpB)
  protected val overstep = WireDefault(addrEnd(3, ALIGNED_WIDTH) =/= 0.U)

  axiLen := Mux(transAligned.asBool(), (TRANS_LEN - 1).U, Cat(Fill(7, "b0".U(1.W)), overstep))
  // TODO: bug?
  protected val axiSize = AXI_SIZE(2, 0);

  protected val axiAddr          = Cat(io.rwAddrIn(AxiAddrWidth - 1, ALIGNED_WIDTH), Fill(ALIGNED_WIDTH, "b0".U(1.W)))
  protected val alignedOffsetLow = Wire(UInt(OFFSET_WIDTH.W))
  protected val alignedOffsetHig = Wire(UInt(OFFSET_WIDTH.W))
  protected val mask             = Wire(UInt(MASK_WIDTH.W))

  alignedOffsetLow := Cat(OFFSET_WIDTH.U - Fill(ALIGNED_WIDTH, "b0".U(1.W)), io.rwAddrIn(ALIGNED_WIDTH - 1, 0)) << 3
  alignedOffsetHig := BusWidth.U - alignedOffsetLow
  mask := (
    (Fill(MASK_WIDTH, sizeByte) & Cat(MASK_WIDTH.U - Fill(8, "b0".U(1.W)), "hff".U(8.W)))
      | (Fill(MASK_WIDTH, sizeHalf) & Cat(MASK_WIDTH.U - Fill(16, "b0".U(1.W)), "hffff".U(16.W)))
      | (Fill(MASK_WIDTH, sizeWord) & Cat(MASK_WIDTH.U - Fill(32, "b0".U(1.W)), "hffffffff".U(32.W)))
      | (Fill(MASK_WIDTH, sizeDouble) & Cat(MASK_WIDTH.U - Fill(64, "b0".U(1.W)), "hffffffff_ffffffff".U(64.W)))
  ) << alignedOffsetLow

  protected val maskLow = mask(AxiDataWidth - 1, 0)
  protected val maskHig = mask(MASK_WIDTH - 1, AxiDataWidth)

  protected val axiId   = Fill(AxiIdLen, "b0".U(1.W))
  protected val axiUser = Fill(AxiUserLen, "b0".U(1.W))

  protected val rwReady = RegInit(false.B)

  protected val rwReadyNxt = WireDefault(transDone)
  protected val rwReadyEna = WireDefault(transDone || rwReady)

  when(rwReadyEna) {
    rwReady := rwReadyNxt
  }
  io.rwReadyOut := rwReady

  protected val rwResp    = RegInit(0.U(2.W))
  protected val rwRespNxt = Mux(wtTrans, io.axiWtbRespIn, io.axiRdRespIn)
  protected val respEna   = WireDefault(transDone)

  when(respEna) {
    rwResp := rwRespNxt
  }
  io.rwRespOut := rwResp

  // ------------------Write Transaction------------------
  io.axiAwReadyIn   := DontCare
  io.axiAwValidOut  := DontCare
  io.axiAwAddrOut   := DontCare
  io.axiAwProtOut   := DontCare
  io.axiAwIdOut     := DontCare
  io.axiAwUserOut   := DontCare
  io.axiAwLenOut    := DontCare
  io.axiAwSizeOut   := DontCare
  io.axiAwBurstOut  := DontCare
  io.axiAwLockOut   := DontCare
  io.axiAwCacheOut  := DontCare
  io.axiAwQosOut    := DontCare
  io.axiAwRegionOut := DontCare

  io.axiWtReadyIn  := DontCare
  io.axiWtValidOut := DontCare
  io.axiWtDataOut  := DontCare
  io.axiWtStrbOut  := DontCare
  io.axiWtLastOut  := DontCare
  io.axiWtUserOut  := DontCare

  io.axiWtbValidIn := DontCare
  io.axiWtbRespIn  := DontCare
  io.axWtbIdIn     := DontCare
  io.axiWtbUserIn  := DontCare

  io.axiWtbReadyOut := DontCare
  // ------------------Read Transaction------------------

  // Read address channel signals
  io.axiArValidOut  := rdStateAddr
  io.axiArAddrOut   := axiAddr
  io.axiArProtOut   := AXI4Bridge.AXI_PROT_UNPRIVILEGED_ACCESS | AXI4Bridge.AXI_PROT_SECURE_ACCESS | AXI4Bridge.AXI_PROT_DATA_ACCESS
  io.axiArIdOut     := axiId
  io.axiArUserOut   := axiUser
  io.axiArLenOut    := axiLen
  io.axiArSizeOut   := axiSize
  io.axiArBurstOut  := AXI4Bridge.AXI_BURST_TYPE_INCR
  io.axiArLockOut   := "b0".U(1.W)
  io.axiArCacheOut  := AXI4Bridge.AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE
  io.axiArQosOut    := "h0".U(4.W)
  io.axiArRegionOut := DontCare
  // Read data channel signals
  io.axiRdReadyOut := rdStateRead

  protected val axiRdDataLow = WireDefault(UInt(AxiDataWidth.W), (io.axiRdDataIn & maskLow) >> alignedOffsetLow)
  protected val axiRdDataHig = WireDefault(UInt(AxiDataWidth.W), (io.axiRdDataIn & maskHig) << alignedOffsetHig)

  protected val dataReadReg = RegInit(0.U(AxiDataWidth.W))
  io.rdDataOut := dataReadReg
  for (i <- 0 until TRANS_LEN) {
    when(io.axiRdReadyOut && io.axiRdValidIn) {
      when((~transAligned) && overstep) {
        when(transLen(0) =/= 0.U) {
          dataReadReg := dataReadReg | axiRdDataHig
        }.otherwise {
          dataReadReg := axiRdDataLow
        }
      }.elsewhen(transLen === i.U) {
        dataReadReg := axiRdDataLow
      }
    }
  }
}
