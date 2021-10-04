package treecorel2

import chisel3._
import chisel3.util._
import treecorel2.common.ConstVal._

object AXI4Bridge {
  // Burst types
  val AXI_BURST_TYPE_FIXED = "b00".U(2.W)
  val AXI_BURST_TYPE_INCR  = "b01".U(2.W)
  val AXI_BURST_TYPE_WRAP  = "b10".U(2.W)

  // Access permissions
  val AXI_PROT_UNPRIVILEGED_ACCESS = "b000".U(3.W)
  val AXI_PROT_PRIVILEGED_ACCESS   = "b001".U(3.W)
  val AXI_PROT_SECURE_ACCESS       = "b000".U(3.W)
  val AXI_PROT_NON_SECURE_ACCESS   = "b010".U(3.W)
  val AXI_PROT_DATA_ACCESS         = "b000".U(3.W)
  val AXI_PROT_INSTRUCTION_ACCESS  = "b100".U(3.W)

  // Memory types (AR)
  val AXI_ARCACHE_DEVICE_NON_BUFFERABLE                 = "b0000".U(4.W)
  val AXI_ARCACHE_DEVICE_BUFFERABLE                     = "b0001".U(4.W)
  val AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE   = "b0010".U(4.W)
  val AXI_ARCACHE_NORMAL_NON_CACHEABLE_BUFFERABLE       = "b0011".U(4.W)
  val AXI_ARCACHE_WRITE_THROUGH_NO_ALLOCATE             = "b1010".U(4.W)
  val AXI_ARCACHE_WRITE_THROUGH_READ_ALLOCATE           = "b1110".U(4.W)
  val AXI_ARCACHE_WRITE_THROUGH_WRITE_ALLOCATE          = "b1010".U(4.W)
  val AXI_ARCACHE_WRITE_THROUGH_READ_AND_WRITE_ALLOCATE = "b1110".U(4.W)
  val AXI_ARCACHE_WRITE_BACK_NO_ALLOCATE                = "b1011".U(4.W)
  val AXI_ARCACHE_WRITE_BACK_READ_ALLOCATE              = "b1111".U(4.W)
  val AXI_ARCACHE_WRITE_BACK_WRITE_ALLOCATE             = "b1011".U(4.W)
  val AXI_ARCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE    = "b1111".U(4.W)

  // Memory types (AW)
  val AXI_AWCACHE_DEVICE_NON_BUFFERABLE                 = "b0000".U(4.W)
  val AXI_AWCACHE_DEVICE_BUFFERABLE                     = "b0001".U(4.W)
  val AXI_AWCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE   = "b0010".U(4.W)
  val AXI_AWCACHE_NORMAL_NON_CACHEABLE_BUFFERABLE       = "b0011".U(4.W)
  val AXI_AWCACHE_WRITE_THROUGH_NO_ALLOCATE             = "b0110".U(4.W)
  val AXI_AWCACHE_WRITE_THROUGH_READ_ALLOCATE           = "b0110".U(4.W)
  val AXI_AWCACHE_WRITE_THROUGH_WRITE_ALLOCATE          = "b1110".U(4.W)
  val AXI_AWCACHE_WRITE_THROUGH_READ_AND_WRITE_ALLOCATE = "b1110".U(4.W)
  val AXI_AWCACHE_WRITE_BACK_NO_ALLOCATE                = "b0111".U(4.W)
  val AXI_AWCACHE_WRITE_BACK_READ_ALLOCATE              = "b0111".U(4.W)
  val AXI_AWCACHE_WRITE_BACK_WRITE_ALLOCATE             = "b1111".U(4.W)
  val AXI_AWCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE    = "b1111".U(4.W)

  // Memory size
  val AXI_SIZE_BYTES_1   = "b000".U(3.W)
  val AXI_SIZE_BYTES_2   = "b001".U(3.W)
  val AXI_SIZE_BYTES_4   = "b010".U(3.W)
  val AXI_SIZE_BYTES_8   = "b011".U(3.W)
  val AXI_SIZE_BYTES_16  = "b100".U(3.W)
  val AXI_SIZE_BYTES_32  = "b101".U(3.W)
  val AXI_SIZE_BYTES_64  = "b110".U(3.W)
  val AXI_SIZE_BYTES_128 = "b111".U(3.W)

  // need to access by PCReg module
  val SIZE_B = "b00".U(2.W)
  val SIZE_H = "b01".U(2.W)
  val SIZE_W = "b10".U(2.W)
  val SIZE_D = "b11".U(2.W)
}

class AXI4Bridge() extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    val inst: AXI4USERIO = new AXI4USERIO
    val mem:  AXI4USERIO = new AXI4USERIO
    val axi = if (SoCEna) new SOCAXI4IO else new AXI4IO
  })

  // preposition
  protected val instAxiId = Fill(AxiIdLen, "b0".U(1.W))
  protected val memAxiId  = Fill(AxiIdLen, "b1".U(1.W))

  // only mem oper can write dram
  protected val wtTrans     = WireDefault(io.mem.req === AxiReqWt.U)
  protected val instRdTrans = WireDefault(io.inst.req === AxiReqRd.U)
  protected val memRdTrans  = WireDefault(io.mem.req === AxiReqRd.U)
  protected val wtValid     = WireDefault(io.mem.valid && wtTrans)
  protected val rdValid     = WireDefault((io.inst.valid && instRdTrans) || io.mem.valid && memRdTrans)

  // handshake
  protected val awHdShk  = WireDefault(io.axi.aw.ready && io.axi.aw.valid)
  protected val wtHdShk  = WireDefault(io.axi.w.ready && io.axi.w.valid)
  protected val wtbHdShk = WireDefault(io.axi.b.ready && io.axi.b.valid)
  protected val arHdShk  = WireDefault(io.axi.ar.ready && io.axi.ar.valid)
  protected val rdHdShk  = WireDefault(io.axi.r.ready && io.axi.r.valid)

  protected val wtDone        = WireDefault(wtHdShk && io.axi.w.bits.last)
  protected val instRdDone    = WireDefault(rdHdShk && io.axi.r.bits.last && io.axi.r.bits.id === instAxiId)
  protected val memRdDone     = WireDefault(rdHdShk && io.axi.r.bits.last && io.axi.r.bits.id === memAxiId)
  protected val memTransDone  = Mux(wtTrans, wtbHdShk, memRdDone)
  protected val instTransDone = WireDefault(instRdDone)

  // FSM for read/write
  protected val eumWtIDLE :: eumWtADDR :: eumWtWRITE :: eumWtRESP :: eumWtRESP2 :: Nil = Enum(5)

  protected val eumRdIDLE :: eumIfARwithMemIDLE :: eumIfRDwithMemIDLE :: eumIfRDwithMemAR :: eumIfARwithMemRD :: eumIfRDwithMemRD :: eumIfIDLEwithMemAR :: eumIfIDLEwithMemRD :: eumDelayRdIDLE :: eumDelayARWithIDLE :: fsmDelayRdWithIDLE :: Nil = Enum(11)

  protected val wtStateReg = RegInit(eumWtIDLE)
  protected val rdStateReg = RegInit(eumRdIDLE)

  protected val wtStateIdle       = WireDefault(wtStateReg === eumWtIDLE)
  protected val wtStateAddr       = WireDefault(wtStateReg === eumWtADDR)
  protected val wtStateWrite      = WireDefault(wtStateReg === eumWtWRITE)
  protected val wtStateResp       = WireDefault(wtStateReg === eumWtRESP)
  protected val rdStateIdle       = WireDefault(rdStateReg === eumRdIDLE)
  protected val rdIfARwithMemIDLE = WireDefault(rdStateReg === eumIfARwithMemIDLE)
  protected val rdIfRDwithMemIDLE = WireDefault(rdStateReg === eumIfRDwithMemIDLE)
  protected val rdIfRDwithMemAR   = WireDefault(rdStateReg === eumIfRDwithMemAR)
  protected val rdIfARwithMemRD   = WireDefault(rdStateReg === eumIfARwithMemRD)
  protected val rdIfRDwithMemRD   = WireDefault(rdStateReg === eumIfRDwithMemRD)
  protected val rdIfIDLEwithMemAR = WireDefault(rdStateReg === eumIfIDLEwithMemAR)
  protected val rdIfIDLEwithMemRD = WireDefault(rdStateReg === eumIfIDLEwithMemRD)

  when(wtValid) {
    switch(wtStateReg) {
      is(eumWtIDLE) {
        wtStateReg := eumWtADDR
      }
      is(eumWtADDR) {
        // printf("[axi]write addr!!!!!!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi]io.axi.aw.bits.addr = 0x${Hexadecimal(io.axi.aw.bits.addr)}\n")
        when(awHdShk) {
          wtStateReg := eumWtWRITE
          // printf("[axi]change wt addr --> data\n")
        }
      }
      is(eumWtWRITE) {
        // printf("[axi]write data!!!!!!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi]io.axi.w.bits.data = 0x${Hexadecimal(io.axi.w.bits.data)}\n")
        // printf(p"[axi]axiWtDataLow = 0x${Hexadecimal(axiWtDataLow)}\n")
        // printf(p"[axi]axiWtDataHig = 0x${Hexadecimal(axiWtDataHig)}\n")
        when(wtDone) {
          wtStateReg := eumWtRESP
          // printf("[axi]change wt data --> resp\n")
        }
      }
      is(eumWtRESP) {
        when(wtbHdShk) {
          wtStateReg := eumWtRESP2
          // printf("[axi]change resp --> idle\n")
        }
      }
      is(eumWtRESP2) {
        wtStateReg := eumWtIDLE
      }
    }
  }

  when(rdValid) {
    switch(rdStateReg) {
      is(eumRdIDLE) {
        // printf("[axi] if-idle mem-idle!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi] io.debug = 0x${Hexadecimal(io.debug)}\n")
        // printf(p"[axi] io.axi.aw.bits.len = 0x${Hexadecimal(io.axi.aw.bits.len)}\n")
        when(io.mem.valid && memRdTrans) {
          rdStateReg := eumIfIDLEwithMemAR
        }.otherwise {
          rdStateReg := eumIfARwithMemIDLE
        }
      }
      is(eumIfIDLEwithMemAR) {
        // printf("[axi] if-idle mem-ar!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi] io.axi.aw.bits.len = 0x${Hexadecimal(io.axi.aw.bits.len)}\n")
        // printf(p"[axi] io.axi.ar.bits.addr = 0x${Hexadecimal(io.axi.ar.bits.addr)}\n")
        when(arHdShk && io.inst.valid && instRdTrans) {
          rdStateReg := eumIfARwithMemRD
        }.elsewhen(arHdShk) {
          rdStateReg := eumIfIDLEwithMemRD
        }
      }
      is(eumIfARwithMemIDLE) {
        // printf("[axi] if-ar mem-idle!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi] io.axi.aw.bits.len = 0x${Hexadecimal(io.axi.aw.bits.len)}\n")
        when(arHdShk && io.mem.valid && memRdTrans) {
          rdStateReg := eumIfRDwithMemAR
        }.elsewhen(arHdShk) {
          rdStateReg := eumIfRDwithMemIDLE
        }
      }
      is(eumIfARwithMemRD) {
        // printf("[axi] if-ar mem-rd!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi] io.axi.aw.bits.len = 0x${Hexadecimal(io.axi.aw.bits.len)}\n")
        // printf(p"[axi] io.axi.r.bits.data = 0x${Hexadecimal(io.axi.r.bits.data)}\n")
        when(arHdShk && (~memRdDone)) {
          rdStateReg := eumIfRDwithMemRD
        }.elsewhen((~arHdShk) && memRdDone) {
          // rdStateReg := eumIfARwithMemIDLE
          rdStateReg := eumDelayARWithIDLE
        }.elsewhen(arHdShk && memRdDone) {
          // rdStateReg := eumIfRDwithMemIDLE
          rdStateReg := fsmDelayRdWithIDLE
        }
      }
      is(eumIfIDLEwithMemRD) {
        // printf("[axi] if-idle mem-rd!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi] io.axi.aw.bits.len = 0x${Hexadecimal(io.axi.aw.bits.len)}\n")
        // printf(p"[axi] io.axi.r.bits.data = 0x${Hexadecimal(io.axi.r.bits.data)}\n")
        when(io.inst.valid && instRdTrans && (~memRdDone)) {
          rdStateReg := eumIfARwithMemRD
        }.elsewhen((~(io.inst.valid && instRdTrans)) && memRdDone) {
          // rdStateReg := eumRdIDLE
          rdStateReg := eumDelayRdIDLE
        }.elsewhen(io.inst.valid && instRdTrans && memRdDone) {
          // rdStateReg := eumIfARwithMemIDLE
          rdStateReg := eumDelayARWithIDLE
        }
      }
      is(eumIfRDwithMemAR) {
        // printf("[axi] if-rd mem-ar!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi] io.axi.aw.bits.len = 0x${Hexadecimal(io.axi.aw.bits.len)}\n")
        // printf(p"[axi] io.axi.ar.bits.addr = 0x${Hexadecimal(io.axi.ar.bits.addr)}\n")
        when(arHdShk && (~instRdDone)) {
          rdStateReg := eumIfRDwithMemRD
        }.elsewhen((~arHdShk) && instRdDone) {
          rdStateReg := eumIfIDLEwithMemAR
        }.elsewhen(arHdShk && instRdDone) {
          rdStateReg := eumIfIDLEwithMemRD
        }
      }
      is(eumIfRDwithMemIDLE) {
        // printf("[axi] if-rd mem-idle!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi] io.axi.aw.bits.len = 0x${Hexadecimal(io.axi.aw.bits.len)}\n")
        when(io.mem.valid && memRdTrans && (~instRdDone)) {
          rdStateReg := eumIfRDwithMemAR
        }.elsewhen((~(io.mem.valid && memRdTrans)) && instRdDone) {
          rdStateReg := eumRdIDLE
        }.elsewhen(io.mem.valid && memRdTrans && instRdDone) {
          rdStateReg := eumIfIDLEwithMemAR
        }
      }
      is(eumIfRDwithMemRD) {
        // printf("[axi] if-rd mem-rd!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi] io.axi.aw.bits.len = 0x${Hexadecimal(io.axi.aw.bits.len)}\n")
        // printf(p"[axi] io.axi.r.bits.data = 0x${Hexadecimal(io.axi.r.bits.data)}\n")
        when(instRdDone) {
          rdStateReg := eumIfIDLEwithMemRD
        }
        when(memRdDone) {
          // rdStateReg := eumIfRDwithMemIDLE
          rdStateReg := fsmDelayRdWithIDLE
        }
      }
      is(eumDelayARWithIDLE) {
        // printf("[axi] if-ar mem-idle(delay)!!!!!!!!!!!!!!!!!!!!\n")
        rdStateReg := eumIfARwithMemIDLE
      }
      is(eumDelayRdIDLE) {
        // printf("[axi] if-idle mem-idle(delay)!!!!!!!!!!!!!!!!!!!!\n")
        rdStateReg := eumRdIDLE
      }
      is(fsmDelayRdWithIDLE) {
        // printf("[axi] if-rd mem-idle(delay)!!!!!!!!!!!!!!!!!!!!\n")
        rdStateReg := eumIfRDwithMemIDLE
      }
    }
  }

  // ------------------Number of transmission------------------
  protected val instTransLen        = RegInit(0.U(8.W))
  protected val instTransLenReset   = WireDefault(this.reset.asBool() || (instRdTrans && rdStateIdle))
  protected val instAxiLen          = Wire(UInt(8.W))
  protected val instTransLenIncrEna = WireDefault((instTransLen =/= instAxiLen) && rdHdShk && (io.axi.r.bits.id === instAxiId))

  when(instTransLenReset) {
    instTransLen := 0.U;
  }.elsewhen(instTransLenIncrEna) {
    instTransLen := instTransLen + 1.U
  }

  protected val memTransLen      = RegInit(0.U(8.W))
  protected val memTransLenReset = WireDefault(this.reset.asBool() || (wtTrans && wtStateIdle) || (memRdTrans && rdStateIdle))
  protected val memAxiLen        = Wire(UInt(8.W))
  protected val memTransLenIncrEna = WireDefault(
    (memTransLen =/= memAxiLen) && (wtHdShk || (rdHdShk && (io.axi.r.bits.id === memAxiId)))
  )
  when(memTransLenReset) {
    memTransLen := 0.U
  }.elsewhen(memTransLenIncrEna) {
    memTransLen := memTransLen + 1.U
  }

// ------------------Process Data------------------
  protected val ALIGNED_WIDTH = 3 // eval: log2(AxiDataWidth / 8)
  protected val OFFSET_WIDTH  = 6 // eval: log2(AxiDataWidth)
  protected val AXI_INST_SIZE = if (SoCEna) 2.U else 3.U // because the flash only support 4 bytes access
  protected val MASK_WIDTH    = 128 // eval: AxiDataWidth * 2
  protected val TRANS_LEN     = 1 // eval: 1
  protected val BLOCK_TRANS   = false.B

  // inst data
  // no-aligned visit
  protected val instTransAligned = WireDefault(BLOCK_TRANS || io.inst.addr(ALIGNED_WIDTH - 1, 0) === 0.U)
  protected val instSizeByte     = WireDefault(io.inst.size === AXI4Bridge.SIZE_B)
  protected val instSizeHalf     = WireDefault(io.inst.size === AXI4Bridge.SIZE_H)
  protected val instSizeWord     = WireDefault(io.inst.size === AXI4Bridge.SIZE_W)
  protected val instSizeDouble   = WireDefault(io.inst.size === AXI4Bridge.SIZE_D)
  // opa: 0100xxx
  // opb: b: 0000
  //      h: 0001
  //      w: 0011
  //      d: 0111
  protected val instAddrOpA = WireDefault(UInt(4.W), Cat(0.U, io.inst.addr(ALIGNED_WIDTH - 1, 0)))
  protected val instAddrOpB = WireDefault(
    UInt(4.W),
    (Fill(4, instSizeByte) & "b0000".U(4.W))
      | (Fill(4, instSizeHalf) & "b0001".U(4.W))
      | (Fill(4, instSizeWord) & "b0011".U(4.W))
      | (Fill(4, instSizeDouble) & "b0111".U(4.W))
  )

  protected val instAddrEnd  = WireDefault(UInt(4.W), instAddrOpA + instAddrOpB)
  protected val instOverstep = WireDefault(instAddrEnd(3, ALIGNED_WIDTH) =/= 0.U)

  instAxiLen := Mux(instTransAligned.asBool(), (TRANS_LEN - 1).U, Cat(Fill(7, "b0".U(1.W)), instOverstep))
  protected val instAxiSize          = AXI_INST_SIZE
  protected val instAxiAddr          = if (SoCEna) io.inst.addr else Cat(io.inst.addr(AxiAddrWidth - 1, ALIGNED_WIDTH), Fill(ALIGNED_WIDTH, "b0".U(1.W)))
  protected val instAlignedOffsetLow = Wire(UInt(OFFSET_WIDTH.W))
  protected val instAlignedOffsetHig = Wire(UInt(OFFSET_WIDTH.W))
  protected val instMask             = Wire(UInt(MASK_WIDTH.W))

  instAlignedOffsetLow := Cat(OFFSET_WIDTH.U - Fill(ALIGNED_WIDTH, "b0".U(1.W)), io.inst.addr(ALIGNED_WIDTH - 1, 0)) << 3
  instAlignedOffsetHig := BusWidth.U - instAlignedOffsetLow
  instMask := (
    (Fill(MASK_WIDTH, instSizeByte) & Cat(Fill(8, "b0".U(1.W)), "hff".U(8.W)))
      | (Fill(MASK_WIDTH, instSizeHalf) & Cat(Fill(16, "b0".U(1.W)), "hffff".U(16.W)))
      | (Fill(MASK_WIDTH, instSizeWord) & Cat(Fill(32, "b0".U(1.W)), "hffffffff".U(32.W)))
      | (Fill(MASK_WIDTH, instSizeDouble) & Cat(Fill(64, "b0".U(1.W)), "hffffffff_ffffffff".U(64.W)))
  ) << instAlignedOffsetLow

  protected val instMaskLow  = instMask(AxiDataWidth - 1, 0)
  protected val instMaskHig  = instMask(MASK_WIDTH - 1, AxiDataWidth)
  protected val instAxiUser  = WireDefault(UInt(AxiUserLen.W), Fill(AxiUserLen, "b0".U(1.W)))
  protected val instReady    = RegInit(false.B)
  protected val instReadyNxt = WireDefault(instTransDone)
  protected val instReadyEna = WireDefault(instTransDone || instReady)

  when(instReadyEna) {
    instReady := instReadyNxt
  }
  io.inst.ready := instReady

  protected val instResp    = RegInit(0.U(2.W))
  protected val instRespNxt = io.axi.r.bits.resp
  protected val instRespEna = WireDefault(instTransDone)

  when(instRespEna) {
    instResp := instRespNxt
  }
  io.inst.resp := instResp

  // ================================mem data
  protected val memTransAligned = WireDefault(BLOCK_TRANS || io.mem.addr(ALIGNED_WIDTH - 1, 0) === 0.U)
  protected val memSizeByte     = WireDefault(io.mem.size === AXI4Bridge.SIZE_B)
  protected val memSizeHalf     = WireDefault(io.mem.size === AXI4Bridge.SIZE_H)
  protected val memSizeWord     = WireDefault(io.mem.size === AXI4Bridge.SIZE_W)
  protected val memSizeDouble   = WireDefault(io.mem.size === AXI4Bridge.SIZE_D)

  protected val memAddrOpA = WireDefault(UInt(4.W), Cat(0.U, io.mem.addr(ALIGNED_WIDTH - 1, 0)))
  protected val memAddrOpB = WireDefault(
    UInt(4.W),
    (Fill(4, memSizeByte) & "b0000".U(4.W))
      | (Fill(4, memSizeHalf) & "b0001".U(4.W))
      | (Fill(4, memSizeWord) & "b0011".U(4.W))
      | (Fill(4, memSizeDouble) & "b0111".U(4.W))
  )

  protected val memAddrEnd  = WireDefault(UInt(4.W), memAddrOpA + memAddrOpB)
  protected val memOverstep = WireDefault(memAddrEnd(3, ALIGNED_WIDTH) =/= 0.U)

  memAxiLen := Mux(memTransAligned.asBool(), (TRANS_LEN - 1).U, Cat(Fill(7, "b0".U(1.W)), memOverstep))

  // flash only support 4 bytes rd(0x3000_0000~0x3fff_ffff)
  // periph suport 4 bytes w/r(0x1000_0000~0x1000_1fff)
  // chiplink suport 4 bytes w/r(0x4000_0000~0x7fff_ffff)
  protected val memAxiSize = Wire(UInt(3.W))
  when(
    (io.mem.addr >= UartBaseAddr && io.mem.addr <= UartBoundAddr) ||
      (io.mem.addr >= SpiBaseAddr && io.mem.addr <= SpiBoundAddr) ||
      (io.mem.addr >= ChiplinkBaseAddr && io.mem.addr <= ChiplinkBoundAddr)
  ) {
    memAxiSize := 2.U
  }.otherwise {
    memAxiSize := 3.U
  }

  protected val memAxiAddr          = Cat(io.mem.addr(AxiAddrWidth - 1, ALIGNED_WIDTH), Fill(ALIGNED_WIDTH, "b0".U(1.W)))
  protected val memAlignedOffsetLow = Wire(UInt(OFFSET_WIDTH.W))
  protected val memAlignedOffsetHig = Wire(UInt(OFFSET_WIDTH.W))
  protected val memMask             = Wire(UInt(MASK_WIDTH.W))

  memAlignedOffsetLow := Cat(OFFSET_WIDTH.U - Fill(ALIGNED_WIDTH, "b0".U(1.W)), io.mem.addr(ALIGNED_WIDTH - 1, 0)) << 3
  memAlignedOffsetHig := BusWidth.U - memAlignedOffsetLow
  memMask := (
    (Fill(MASK_WIDTH, memSizeByte) & Cat(Fill(8, "b0".U(1.W)), "hff".U(8.W)))
      | (Fill(MASK_WIDTH, memSizeHalf) & Cat(Fill(16, "b0".U(1.W)), "hffff".U(16.W)))
      | (Fill(MASK_WIDTH, memSizeWord) & Cat(Fill(32, "b0".U(1.W)), "hffffffff".U(32.W)))
      | (Fill(MASK_WIDTH, memSizeDouble) & Cat(Fill(64, "b0".U(1.W)), "hffffffff_ffffffff".U(64.W)))
  ) << memAlignedOffsetLow

  protected val memMaskLow = memMask(AxiDataWidth - 1, 0)
  protected val memMaskHig = memMask(MASK_WIDTH - 1, AxiDataWidth)
  protected val memStrb    = Wire(UInt((AxiDataWidth / 8).W))

  memStrb := (
    (Fill(8, memSizeByte) & "b1".U(8.W))
      | ((Fill(8, memSizeHalf) & "b11".U(8.W)))
      | ((Fill(8, memSizeWord) & "b1111".U(8.W)))
      | ((Fill(8, memSizeDouble) & "b1111_1111".U(8.W)))
  )

  protected val memStrbLow = WireDefault(UInt((AxiDataWidth / 8).W), memStrb << io.mem.addr(ALIGNED_WIDTH - 1, 0))
  protected val memStrbHig = WireDefault(UInt((AxiDataWidth / 8).W), memStrb >> ((AxiDataWidth / 8).U - io.mem.addr(ALIGNED_WIDTH - 1, 0)))

  protected val memAxiUser  = Fill(AxiUserLen, "b0".U(1.W))
  protected val memReady    = RegInit(false.B)
  protected val memReadyNxt = WireDefault(memTransDone)
  protected val memReadyEna = WireDefault(memTransDone || memReady)

  when(memReadyEna) {
    memReady := memReadyNxt
  }
  io.mem.ready := memReady

  protected val memResp    = RegInit(0.U(2.W))
  protected val memRespNxt = Mux(wtTrans, io.axi.b.bits.resp, io.axi.r.bits.resp)
  protected val memRespEna = WireDefault(memTransDone)

  when(memRespEna) {
    memResp := memRespNxt
  }
  io.mem.resp := memResp

  // ------------------Write Transaction------------------
  io.axi.aw.valid      := wtStateAddr
  io.axi.aw.bits.addr  := memAxiAddr
  io.axi.aw.bits.id    := memAxiId
  io.axi.aw.bits.len   := memAxiLen
  io.axi.aw.bits.size  := memAxiSize
  io.axi.aw.bits.burst := AXI4Bridge.AXI_BURST_TYPE_INCR

  if (!SoCEna) {
    val sim = io.axi.asInstanceOf[AXI4IO]
    sim.aw.bits.prot  := AXI4Bridge.AXI_PROT_UNPRIVILEGED_ACCESS | AXI4Bridge.AXI_PROT_SECURE_ACCESS | AXI4Bridge.AXI_PROT_DATA_ACCESS
    sim.aw.bits.user  := memAxiUser
    sim.aw.bits.lock  := "b0".U(1.W)
    sim.aw.bits.cache := AXI4Bridge.AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE
    sim.aw.bits.qos   := "h0".U(4.W)
  }

  protected val axiWtDataLow = WireDefault(UInt(AxiDataWidth.W), io.mem.wdata << memAlignedOffsetLow)
  protected val axiWtDataHig = WireDefault(UInt(AxiDataWidth.W), io.mem.wdata >> memAlignedOffsetHig)

  io.axi.w.valid := wtStateWrite
  io.axi.w.bits.data := Mux(
    io.axi.w.valid,
    Mux(memTransLen(0, 0) === "b0".U(1.W), axiWtDataLow, axiWtDataHig),
    Fill(AxiDataWidth, "b0".U(1.W))
  )
  io.axi.w.bits.strb := Mux(
    io.axi.w.valid,
    Mux(memTransLen(0, 0) === "b0".U(1.W), memStrbLow, memStrbHig),
    Fill(AxiDataWidth / 8, "b0".U(1.W))
  )
  io.axi.w.bits.last := Mux(io.axi.w.valid, memTransLen === memAxiLen, false.B)

  // wt resp
  io.axi.b.bits.id := DontCare
  if (!SoCEna) {
    val sim = io.axi.asInstanceOf[AXI4IO]
    sim.b.bits.user := DontCare
  }

  io.axi.b.ready := wtStateResp
  // ------------------Read Transaction------------------

  // Read address channel signals
  io.axi.ar.valid      := rdIfARwithMemIDLE || rdIfIDLEwithMemAR || rdIfRDwithMemAR || rdIfARwithMemRD
  io.axi.ar.bits.addr  := (Fill(AxiAddrWidth, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiAddr) | (Fill(AxiAddrWidth, rdIfIDLEwithMemAR || rdIfRDwithMemAR) & memAxiAddr)
  io.axi.ar.bits.id    := (Fill(AxiIdLen, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiId) | (Fill(AxiIdLen, rdIfIDLEwithMemAR || rdIfRDwithMemAR) & memAxiId)
  io.axi.ar.bits.len   := (Fill(8, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiLen) | (Fill(8, rdIfIDLEwithMemAR || rdIfRDwithMemAR) & memAxiLen)
  io.axi.ar.bits.size  := (Fill(3, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiSize) | (Fill(3, rdIfIDLEwithMemAR || rdIfRDwithMemAR) & memAxiSize)
  io.axi.ar.bits.burst := AXI4Bridge.AXI_BURST_TYPE_INCR

  if (!SoCEna) {
    val sim = io.axi.asInstanceOf[AXI4IO]
    sim.ar.bits.prot  := AXI4Bridge.AXI_PROT_UNPRIVILEGED_ACCESS | AXI4Bridge.AXI_PROT_SECURE_ACCESS | AXI4Bridge.AXI_PROT_DATA_ACCESS
    sim.ar.bits.user  := (Fill(AxiUserLen, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiUser) | (Fill(AxiUserLen, rdIfIDLEwithMemAR || rdIfRDwithMemAR) & memAxiUser)
    sim.ar.bits.lock  := "b0".U(1.W)
    sim.ar.bits.cache := AXI4Bridge.AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE
    sim.ar.bits.qos   := "h0".U(4.W)
  }

  // Read data channel signals
  io.axi.r.ready := rdIfARwithMemRD || rdIfIDLEwithMemRD || rdIfRDwithMemAR || rdIfRDwithMemIDLE || rdIfRDwithMemRD

  protected val axiRdDataLow = WireDefault(
    UInt(AxiDataWidth.W),
    Mux(
      io.axi.r.bits.id === instAxiId,
      (io.axi.r.bits.data & instMaskLow) >> instAlignedOffsetLow,
      (io.axi.r.bits.data & memMaskLow) >> memAlignedOffsetLow
    )
  )

  protected val axiRdDataHig = WireDefault(
    UInt(AxiDataWidth.W),
    Mux(
      io.axi.r.bits.id === instAxiId,
      (io.axi.r.bits.data & instMaskHig) << instAlignedOffsetHig,
      (io.axi.r.bits.data & memMaskHig) << memAlignedOffsetHig
    )
  )

  //========================= read data oper
  protected val instDataReadReg = RegInit(0.U(AxiDataWidth.W))
  io.inst.rdata := instDataReadReg
  for (i <- 0 until TRANS_LEN) {
    when(rdHdShk && io.axi.r.bits.id === instAxiId) {
      when((~instTransAligned) && instOverstep) {
        printf("inst not aligned!!!!!!\n")
        when(instTransLen(0, 0) =/= 0.U) {
          instDataReadReg := instDataReadReg | axiRdDataHig
        }.otherwise {
          instDataReadReg := axiRdDataLow
        }
      }.elsewhen(instTransLen === i.U) {
        // printf("inst rdata align!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi4]instAlignedOffsetLow = 0x${Hexadecimal(instAlignedOffsetLow)}\n")
        // printf(p"[axi4]instAlignedOffsetHig = 0x${Hexadecimal(instAlignedOffsetHig)}\n")
        // printf(p"[axi4]instMask = 0x${Hexadecimal(instMask)}\n")
        // printf(p"[axi4]instMaskLow = 0x${Hexadecimal(instMaskLow)}\n")
        // printf(p"[axi4]instMaskHig = 0x${Hexadecimal(instMaskHig)}\n\n")
        instDataReadReg := axiRdDataLow
      }
    }
  }

  protected val memDataReadReg = RegInit(0.U(AxiDataWidth.W))
  io.mem.rdata := memDataReadReg
  for (i <- 0 until TRANS_LEN) {
    when(rdHdShk && io.axi.r.bits.id === memAxiId) {
      when((~memTransAligned) && memOverstep) {
        when(memTransLen(0, 0) =/= 0.U) {
          memDataReadReg := memDataReadReg | axiRdDataHig
          // printf("mem rdata hig!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
          // printf(p"[axi4]memAlignedOffsetLow = 0x${Hexadecimal(memAlignedOffsetLow)}\n")
          // printf(p"[axi4]memAlignedOffsetHig = 0x${Hexadecimal(memAlignedOffsetHig)}\n")
          // printf(p"[axi4]memMask = 0x${Hexadecimal(memMask)}\n")
          // printf(p"[axi4]memMaskLow = 0x${Hexadecimal(memMaskLow)}\n")
          // printf(p"[axi4]memMaskHig = 0x${Hexadecimal(memMaskHig)}\n")
          // printf(p"[axi4]io.axi.r.bits.data = 0x${Hexadecimal(io.axi.r.bits.data)}\n")
          // printf(p"[axi4]axiRdDataHig = 0x${Hexadecimal(axiRdDataHig)}\n\n")
        }.otherwise {
          memDataReadReg := axiRdDataLow
          // printf("mem rdata low!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
          // printf(p"[axi4]memAlignedOffsetLow = 0x${Hexadecimal(memAlignedOffsetLow)}\n")
          // printf(p"[axi4]memAlignedOffsetHig = 0x${Hexadecimal(memAlignedOffsetHig)}\n")
          // printf(p"[axi4]memMask = 0x${Hexadecimal(memMask)}\n")
          // printf(p"[axi4]memMaskLow = 0x${Hexadecimal(memMaskLow)}\n")
          // printf(p"[axi4]memMaskHig = 0x${Hexadecimal(memMaskHig)}\n")
          // printf(p"[axi4]io.axi.r.bits.data = 0x${Hexadecimal(io.axi.r.bits.data)}\n")
          // printf(p"[axi4]axiRdDataLow = 0x${Hexadecimal(axiRdDataLow)}\n\n")
        }
      }.elsewhen(memTransLen === i.U) {
        memDataReadReg := axiRdDataLow
        // printf("mem rdata align!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[axi4]memAlignedOffsetLow = 0x${Hexadecimal(memAlignedOffsetLow)}\n")
        // printf(p"[axi4]memAlignedOffsetHig = 0x${Hexadecimal(memAlignedOffsetHig)}\n")
        // printf(p"[axi4]memMask = 0x${Hexadecimal(memMask)}\n")
        // printf(p"[axi4]memMaskLow = 0x${Hexadecimal(memMaskLow)}\n")
        // printf(p"[axi4]memMaskHig = 0x${Hexadecimal(memMaskHig)}\n")
        // printf(p"[axi4]io.axi.r.bits.data = 0x${Hexadecimal(io.axi.r.bits.data)}\n")
        // printf(p"[axi4]axiRdDataLow = 0x${Hexadecimal(axiRdDataLow)}\n\n")
      }
    }
  }
}
