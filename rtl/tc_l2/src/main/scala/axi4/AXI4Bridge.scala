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

// Memory size
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

class AXI4Bridge extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    // inst oper

    val instValidIn: Bool = Input(Bool())
    val instReqIn:   UInt = Input(UInt(2.W)) // can only read
    val instAddrIn:  UInt = Input(UInt(AxiDataWidth.W))
    val instSizeIn:  UInt = Input(UInt(AxiSizeLen.W))

    val instReadyOut:  Bool = Output(Bool())
    val instRdDataOut: UInt = Output(UInt(BusWidth.W))
    val instRespOut:   UInt = Output(UInt(AxiRespLen.W))

    // mem oper
    val memValidIn: Bool = Input(Bool())
    val memReqIn:   UInt = Input(UInt(2.W)) // read or write
    val memDataIn:  UInt = Input(UInt(AxiDataWidth.W)) // write to the dram
    val memAddrIn:  UInt = Input(UInt(AxiDataWidth.W))
    val memSizeIn:  UInt = Input(UInt(AxiSizeLen.W))

    val memReadyOut:  Bool = Output(Bool())
    val memRdDataOut: UInt = Output(UInt(BusWidth.W))
    val memRespOut:   UInt = Output(UInt(AxiRespLen.W))

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
    val axiAwRegionOut: UInt = Output(UInt(4.W)) // not use

    // write data
    val axiWtReadyIn: Bool = Input(Bool())

    val axiWtValidOut: Bool = Output(Bool())
    val axiWtDataOut:  UInt = Output(UInt(AxiDataWidth.W))
    val axiWtStrbOut:  UInt = Output(UInt((AxiDataWidth / 8).W))
    val axiWtLastOut:  Bool = Output(Bool())
    val axiWtIdOut:    UInt = Output(UInt(AxiIdLen.W))
    val axiWtUserOut:  UInt = Output(UInt(AxiUserLen.W)) // not use

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
    val axiArRegionOut: UInt = Output(UInt(4.W)) // not use

    // read data
    val axiRdValidIn: Bool = Input(Bool())
    val axiRdRespIn:  UInt = Input(UInt(AxiRespLen.W))
    val axiRdDataIn:  UInt = Input(UInt(AxiDataWidth.W))
    val axiRdLastIn:  Bool = Input(Bool())
    val axiRdIdIn:    UInt = Input(UInt(AxiIdLen.W))
    val axiRdUserIn:  UInt = Input(UInt(AxiUserLen.W))

    val axiRdReadyOut: Bool = Output(Bool())
  })

  // preposition
  protected val instAxiId = Fill(AxiIdLen, "b0".U(1.W))
  protected val memAxiId  = Cat(AxiIdLen.U - Fill(1, "b0".U(1.W)), "b1".U(1.W)) // TODO: bug? b1001

  // only mem oper can write dram
  protected val wtTrans     = WireDefault(io.memReqIn === AxiReqWt.U)
  protected val instRdTrans = WireDefault(io.instReqIn === AxiReqRd.U)
  protected val memRdTrans  = WireDefault(io.memReqIn === AxiReqRd.U)
  protected val wtValid     = WireDefault(io.memValidIn && wtTrans)
  protected val rdValid     = WireDefault((io.instValidIn && instRdTrans) || io.memValidIn && memRdTrans)

  // handshake
  protected val awHdShk  = WireDefault(io.axiAwReadyIn && io.axiAwValidOut)
  protected val wtHdShk  = WireDefault(io.axiWtReadyIn && io.axiWtValidOut)
  protected val wtbHdShk = WireDefault(io.axiWtbReadyOut && io.axiWtbValidIn)
  protected val arHdShk  = WireDefault(io.axiArReadyIn && io.axiArValidOut)
  protected val rdHdShk  = WireDefault(io.axiRdReadyOut && io.axiRdValidIn)

  protected val wtDone        = WireDefault(wtHdShk && io.axiWtLastOut)
  protected val instRdDone    = WireDefault(rdHdShk && io.axiRdLastIn && io.axiRdIdIn === instAxiId)
  protected val memRdDone     = WireDefault(rdHdShk && io.axiRdLastIn && io.axiRdIdIn === memAxiId)
  protected val memTransDone  = Mux(wtTrans, wtbHdShk, memRdDone)
  protected val instTransDone = WireDefault(instRdDone)

  // FSM for read/write
  protected val fsmWtIDLE  = 0.U(2.W)
  protected val fsmWtADDR  = 1.U(2.W)
  protected val fsmWtWRITE = 2.U(2.W)
  protected val fsmWtRESP  = 3.U(2.W)

  protected val fsmRdIDLE          = 0.U(3.W)
  protected val fsmIfARwithMemIDLE = 1.U(3.W)
  protected val fsmIfRDwithMemIDLE = 2.U(3.W)
  protected val fsmIfRDwithMemAR   = 3.U(3.W)
  protected val fsmIfARwithMemRD   = 4.U(3.W)
  protected val fsmIfRDwithMemRD   = 5.U(3.W)
  protected val fsmIfIDLEwithMemAR = 6.U(3.W)
  protected val fsmIfIDLEwithMemRD = 7.U(3.W)

  protected val wtStateReg = RegInit(fsmWtIDLE)
  protected val rdStateReg = RegInit(fsmRdIDLE)

  protected val wtStateIdle  = WireDefault(wtStateReg === fsmWtIDLE)
  protected val wtStateAddr  = WireDefault(wtStateReg === fsmWtADDR)
  protected val wtStateWrite = WireDefault(wtStateReg === fsmWtWRITE)
  protected val wtStateResp  = WireDefault(wtStateReg === fsmWtRESP)

  protected val rdStateIdle       = WireDefault(rdStateReg === fsmRdIDLE)
  protected val rdIfARwithMemIDLE = WireDefault(rdStateReg === fsmIfARwithMemIDLE)
  protected val rdIfRDwithMemIDLE = WireDefault(rdStateReg === fsmIfRDwithMemIDLE)
  protected val rdIfRDwithMemAR   = WireDefault(rdStateReg === fsmIfRDwithMemAR)
  protected val rdIfARwithMemRD   = WireDefault(rdStateReg === fsmIfARwithMemRD)
  protected val rdIfRDwithMemRD   = WireDefault(rdStateReg === fsmIfRDwithMemRD)
  protected val rdIfIDLEwithMemAR = WireDefault(rdStateReg === fsmIfIDLEwithMemAR)
  protected val rdIfIDLEwithMemRD = WireDefault(rdStateReg === fsmIfIDLEwithMemRD)

  when(wtValid) {
    switch(wtStateReg) {
      is(fsmWtIDLE) {
        wtStateReg := fsmWtADDR
      }
      is(fsmWtADDR) {
        when(awHdShk) {
          wtStateReg := fsmWtWRITE
        }
      }
      is(fsmWtWRITE) {
        when(wtDone) {
          wtStateReg := fsmWtRESP
        }
      }
      is(fsmWtRESP) {
        when(wtbHdShk) {
          wtStateReg := fsmWtIDLE
        }
      }
    }
  }

  when(rdValid) {
    switch(rdStateReg) {
      is(fsmRdIDLE) {
        when(io.memValidIn && memRdTrans) {
          rdStateReg := fsmIfIDLEwithMemAR
        }.otherwise {
          rdStateReg := fsmIfARwithMemIDLE
        }
      }
      is(fsmIfIDLEwithMemAR) {
        when(arHdShk && io.instValidIn && instRdTrans) {
          rdStateReg := fsmIfARwithMemRD
        }.elsewhen(arHdShk) {
          rdStateReg := fsmIfIDLEwithMemRD
        }
      }
      is(fsmIfARwithMemIDLE) {
        when(arHdShk && io.memValidIn && memRdTrans) {
          rdStateReg := fsmIfRDwithMemAR
        }.elsewhen(arHdShk) {
          rdStateReg := fsmIfRDwithMemIDLE
        }
      }
      is(fsmIfARwithMemRD) {
        when(arHdShk && (~memRdDone)) {
          rdStateReg := fsmIfRDwithMemRD
        }.elsewhen((~arHdShk) && memRdDone) {
          rdStateReg := fsmIfARwithMemIDLE
        }.elsewhen(arHdShk && memRdDone) {
          rdStateReg := fsmIfRDwithMemIDLE
        }
      }
      is(fsmIfIDLEwithMemRD) {
        when(io.instValidIn && instRdTrans && (~memRdDone)) {
          rdStateReg := fsmIfARwithMemRD
        }.elsewhen((~(io.instValidIn && instRdTrans)) && memRdDone) {
          rdStateReg := fsmRdIDLE
        }.elsewhen(io.instValidIn && instRdTrans && memRdDone) {
          rdStateReg := fsmIfARwithMemIDLE
        }
      }
      is(fsmIfRDwithMemAR) {
        when(arHdShk && (~instRdDone)) {
          rdStateReg := fsmIfRDwithMemRD
        }.elsewhen((~arHdShk) && instRdDone) {
          rdStateReg := fsmIfIDLEwithMemAR
        }.elsewhen(arHdShk && instRdDone) {
          rdStateReg := fsmIfIDLEwithMemRD
        }
      }
      is(fsmIfRDwithMemIDLE) {
        when(io.memValidIn && memRdTrans && (~instRdDone)) {
          rdStateReg := fsmIfRDwithMemAR
        }.elsewhen((~(io.memValidIn && memRdTrans)) && instRdDone) {
          rdStateReg := fsmRdIDLE
        }.elsewhen(io.memValidIn && memRdTrans && instRdDone) {
          rdStateReg := fsmIfIDLEwithMemAR
        }
      }
      is(fsmIfRDwithMemRD) {
        when(instRdDone) {
          rdStateReg := fsmIfIDLEwithMemRD
        }
        when(memRdDone) {
          rdStateReg := fsmIfRDwithMemIDLE
        }
      }
    }
  }

  // ------------------Number of transmission------------------
  protected val instTransLen        = RegInit(0.U(8.W))
  protected val instTransLenReset   = WireDefault(this.reset.asBool() || (instRdTrans && rdStateIdle))
  protected val instAxiLen          = Wire(UInt(8.W))
  protected val instTransLenIncrEna = WireDefault((instTransLen =/= instAxiLen) && rdHdShk && (io.axiRdIdIn === instAxiId))

  when(instTransLenReset) {
    instTransLen := 0.U;
  }.elsewhen(instTransLenIncrEna) {
    instTransLen := instTransLen + 1.U
  }

  protected val memTransLen        = RegInit(0.U(8.W))
  protected val memTransLenReset   = WireDefault(this.reset.asBool() || (wtTrans && wtStateIdle) || (memRdTrans && rdStateIdle))
  protected val memAxiLen          = Wire(UInt(8.W))
  protected val memTransLenIncrEna = WireDefault((memTransLen =/= memAxiLen) && (wtHdShk || (rdHdShk && (io.axiRdIdIn === memAxiId))))
  when(memTransLenReset) {
    memTransLen := 0.U
  }.elsewhen(memTransLenIncrEna) {
    memTransLen := memTransLen + 1.U
  }

// ------------------Process Data------------------
  protected val ALIGNED_WIDTH = 3 // eval: log2(AxiDataWidth / 8)
  protected val OFFSET_WIDTH  = 6 // eval: log2(AxiDataWidth)
  protected val AXI_SIZE      = 3.U // eval: log2(AxiDataWidth / 8)
  protected val MASK_WIDTH    = 128 // eval: AxiDataWidth * 2
  protected val TRANS_LEN     = 1 // eval: 1
  protected val BLOCK_TRANS   = false.B

  // inst data
  // no-aligned visit
  protected val instTransAligned = WireDefault(BLOCK_TRANS || io.instAddrIn(ALIGNED_WIDTH - 1, 0) === 0.U)
  protected val instSizeByte     = WireDefault(io.instSizeIn === AXI4Bridge.SIZE_B)
  protected val instSizeHalf     = WireDefault(io.instSizeIn === AXI4Bridge.SIZE_H)
  protected val instSizeWord     = WireDefault(io.instSizeIn === AXI4Bridge.SIZE_W)
  protected val instSizeDouble   = WireDefault(io.instSizeIn === AXI4Bridge.SIZE_D)
  // opa: 0100xxx
  // opb: b: 0000
  //      h: 0001
  //      w: 0011
  //      d: 0111
  protected val instAddrOpA = WireDefault(UInt(4.W), Cat(4.U - Fill(ALIGNED_WIDTH, 0.U), io.instAddrIn(ALIGNED_WIDTH - 1, 0)))
  protected val instAddrOpB = WireDefault(
    UInt(4.W),
    (Fill(4, instSizeByte) & "b0".U(4.W))
      | (Fill(4, instSizeHalf) & "b1".U(4.W))
      | (Fill(4, instSizeWord) & "b11".U(4.W))
      | (Fill(4, instSizeDouble) & "b111".U(4.W))
  )

  protected val instAddrEnd  = WireDefault(UInt(4.W), instAddrOpA + instAddrOpB)
  protected val instOverstep = WireDefault(instAddrEnd(3, ALIGNED_WIDTH) =/= 0.U)

  instAxiLen := Mux(instTransAligned.asBool(), (TRANS_LEN - 1).U, Cat(Fill(7, "b0".U(1.W)), instOverstep))
  // TODO: bug?
  protected val instAxiSize          = AXI_SIZE(2, 0);
  protected val instAxiAddr          = Cat(io.instAddrIn(AxiAddrWidth - 1, ALIGNED_WIDTH), Fill(ALIGNED_WIDTH, "b0".U(1.W)))
  protected val instAlignedOffsetLow = Wire(UInt(OFFSET_WIDTH.W))
  protected val instAlignedOffsetHig = Wire(UInt(OFFSET_WIDTH.W))
  protected val instMask             = Wire(UInt(MASK_WIDTH.W))

  instAlignedOffsetLow := Cat(OFFSET_WIDTH.U - Fill(ALIGNED_WIDTH, "b0".U(1.W)), io.instAddrIn(ALIGNED_WIDTH - 1, 0)) << 3
  instAlignedOffsetHig := BusWidth.U - instAlignedOffsetLow
  instMask := (
    (Fill(MASK_WIDTH, instSizeByte) & Cat(MASK_WIDTH.U - Fill(8, "b0".U(1.W)), "hff".U(8.W)))
      | (Fill(MASK_WIDTH, instSizeHalf) & Cat(MASK_WIDTH.U - Fill(16, "b0".U(1.W)), "hffff".U(16.W)))
      | (Fill(MASK_WIDTH, instSizeWord) & Cat(MASK_WIDTH.U - Fill(32, "b0".U(1.W)), "hffffffff".U(32.W)))
      | (Fill(MASK_WIDTH, instSizeDouble) & Cat(MASK_WIDTH.U - Fill(64, "b0".U(1.W)), "hffffffff_ffffffff".U(64.W)))
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
  io.instReadyOut := instReady

  protected val instResp    = RegInit(0.U(2.W))
  protected val instRespNxt = io.axiRdRespIn
  protected val instRespEna = WireDefault(instTransDone)

  when(instRespEna) {
    instResp := instRespNxt
  }
  io.instRespOut := instResp

  // ================================mem data
  protected val memTransAligned = WireDefault(BLOCK_TRANS || io.memAddrIn(ALIGNED_WIDTH - 1, 0) === 0.U)
  protected val memSizeByte     = WireDefault(io.memSizeIn === AXI4Bridge.SIZE_B)
  protected val memSizeHalf     = WireDefault(io.memSizeIn === AXI4Bridge.SIZE_H)
  protected val memSizeWord     = WireDefault(io.memSizeIn === AXI4Bridge.SIZE_W)
  protected val memSizeDouble   = WireDefault(io.memSizeIn === AXI4Bridge.SIZE_D)

  protected val memAddrOpA = WireDefault(UInt(4.W), Cat(4.U - Fill(ALIGNED_WIDTH, 0.U), io.memAddrIn(ALIGNED_WIDTH - 1, 0)))
  protected val memAddrOpB = WireDefault(
    UInt(4.W),
    (Fill(4, memSizeByte) & "b0".U(4.W))
      | (Fill(4, memSizeHalf) & "b1".U(4.W))
      | (Fill(4, memSizeWord) & "b11".U(4.W))
      | (Fill(4, memSizeDouble) & "b111".U(4.W))
  )

  protected val memAddrEnd  = WireDefault(UInt(4.W), memAddrOpA + memAddrOpB)
  protected val memOverstep = WireDefault(memAddrEnd(3, ALIGNED_WIDTH) =/= 0.U)

  memAxiLen := Mux(memTransAligned.asBool(), (TRANS_LEN - 1).U, Cat(Fill(7, "b0".U(1.W)), memOverstep))

  protected val memAxiSize          = AXI_SIZE(2, 0);
  protected val memAxiAddr          = Cat(io.memAddrIn(AxiAddrWidth - 1, ALIGNED_WIDTH), Fill(ALIGNED_WIDTH, "b0".U(1.W)))
  protected val memAlignedOffsetLow = Wire(UInt(OFFSET_WIDTH.W))
  protected val memAlignedOffsetHig = Wire(UInt(OFFSET_WIDTH.W))
  protected val memMask             = Wire(UInt(MASK_WIDTH.W))

  memAlignedOffsetLow := Cat(OFFSET_WIDTH.U - Fill(ALIGNED_WIDTH, "b0".U(1.W)), io.memAddrIn(ALIGNED_WIDTH - 1, 0)) << 3
  memAlignedOffsetHig := BusWidth.U - memAlignedOffsetLow
  memMask := (
    (Fill(MASK_WIDTH, memSizeByte) & Cat(MASK_WIDTH.U - Fill(8, "b0".U(1.W)), "hff".U(8.W)))
      | (Fill(MASK_WIDTH, memSizeHalf) & Cat(MASK_WIDTH.U - Fill(16, "b0".U(1.W)), "hffff".U(16.W)))
      | (Fill(MASK_WIDTH, memSizeWord) & Cat(MASK_WIDTH.U - Fill(32, "b0".U(1.W)), "hffffffff".U(32.W)))
      | (Fill(MASK_WIDTH, memSizeDouble) & Cat(MASK_WIDTH.U - Fill(64, "b0".U(1.W)), "hffffffff_ffffffff".U(64.W)))
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

  protected val memStrbLow = WireDefault(UInt((AxiDataWidth / 8).W), memStrb << io.memAddrIn(ALIGNED_WIDTH - 1, 0))
  protected val memStrbHig =
    WireDefault(UInt((AxiDataWidth / 8).W), memStrb >> ((AxiDataWidth / 8).U - io.memAddrIn(ALIGNED_WIDTH - 1, 0)))

  protected val memAxiUser  = Fill(AxiUserLen, "b0".U(1.W))
  protected val memReady    = RegInit(false.B)
  protected val memReadyNxt = WireDefault(memTransDone)
  protected val memReadyEna = WireDefault(memTransDone || memReady)

  when(memReadyEna) {
    memReady := memReadyNxt
  }
  io.memReadyOut := memReady

  protected val memResp    = RegInit(0.U(2.W))
  protected val memRespNxt = Mux(wtTrans, io.axiWtbRespIn, io.axiRdRespIn)
  protected val memRespEna = WireDefault(memTransDone)

  when(memRespEna) {
    memResp := memRespNxt
  }
  io.memRespOut := memResp

  // ------------------Write Transaction------------------
  io.axiAwValidOut  := wtStateAddr
  io.axiAwAddrOut   := memAxiAddr
  io.axiAwProtOut   := AXI4Bridge.AXI_PROT_UNPRIVILEGED_ACCESS | AXI4Bridge.AXI_PROT_SECURE_ACCESS | AXI4Bridge.AXI_PROT_DATA_ACCESS
  io.axiAwIdOut     := memAxiId
  io.axiAwUserOut   := memAxiUser
  io.axiAwLenOut    := memAxiLen
  io.axiAwSizeOut   := memAxiSize
  io.axiAwBurstOut  := AXI4Bridge.AXI_BURST_TYPE_INCR
  io.axiAwLockOut   := "b0".U(1.W)
  io.axiAwCacheOut  := AXI4Bridge.AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE
  io.axiAwQosOut    := "h0".U(4.W)
  io.axiAwRegionOut := DontCare

  protected val axiWtDataLow = WireDefault(UInt(AxiDataWidth.W), io.memDataIn << memAlignedOffsetLow)
  protected val axiWtDataHig = WireDefault(UInt(AxiDataWidth.W), io.memDataIn >> memAlignedOffsetHig)

  io.axiWtValidOut := wtStateWrite
  io.axiWtIdOut    := memAxiId
  io.axiWtDataOut := Mux(
    io.axiWtValidOut,
    Mux(memTransLen(0) === "b0".U(1.W), axiWtDataLow, axiWtDataHig),
    Fill(AxiDataWidth, "b0".U(1.W))
  )
  io.axiWtStrbOut := Mux(
    io.axiWtValidOut,
    Mux(memTransLen(0) === "b0".U(1.W), memStrbLow, memStrbHig),
    Fill(AxiDataWidth / 8, "b0".U(1.W))
  )
  io.axiWtLastOut := Mux(io.axiWtValidOut, (memTransLen === memAxiLen), false.B)
  io.axiWtUserOut := DontCare

  // wt resp
  io.axWtbIdIn    := DontCare
  io.axiWtbUserIn := DontCare

  io.axiWtbReadyOut := wtStateResp
  // ------------------Read Transaction------------------

  // Read address channel signals
  io.axiArValidOut := rdIfARwithMemIDLE || rdIfIDLEwithMemAR || rdIfRDwithMemAR || rdIfARwithMemRD
  io.axiArAddrOut := (Fill(AxiAddrWidth, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiAddr) | (Fill(
    AxiAddrWidth,
    rdIfIDLEwithMemAR || rdIfRDwithMemAR
  ) & memAxiAddr)

  io.axiArProtOut := AXI4Bridge.AXI_PROT_UNPRIVILEGED_ACCESS | AXI4Bridge.AXI_PROT_SECURE_ACCESS | AXI4Bridge.AXI_PROT_DATA_ACCESS
  io.axiArIdOut := (Fill(AxiIdLen, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiId) | (Fill(
    AxiIdLen,
    rdIfIDLEwithMemAR || rdIfRDwithMemAR
  ) & memAxiId)

  io.axiArUserOut := (Fill(AxiUserLen, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiUser) | (Fill(
    AxiUserLen,
    rdIfIDLEwithMemAR || rdIfRDwithMemAR
  ) & memAxiUser)

  io.axiArLenOut := (Fill(8, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiLen) | (Fill(
    8,
    rdIfIDLEwithMemAR || rdIfRDwithMemAR
  ) & memAxiLen)

  io.axiArSizeOut := (Fill(3, rdIfARwithMemIDLE || rdIfARwithMemRD) & instAxiSize) | (Fill(
    3,
    rdIfIDLEwithMemAR || rdIfRDwithMemAR
  ) & memAxiSize)

  io.axiArBurstOut  := AXI4Bridge.AXI_BURST_TYPE_INCR
  io.axiArLockOut   := "b0".U(1.W)
  io.axiArCacheOut  := AXI4Bridge.AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE
  io.axiArQosOut    := "h0".U(4.W)
  io.axiArRegionOut := DontCare
  // Read data channel signals
  io.axiRdReadyOut := rdIfARwithMemRD || rdIfIDLEwithMemRD || rdIfRDwithMemAR || rdIfRDwithMemIDLE || rdIfRDwithMemRD

  protected val axiRdDataLow = WireDefault(
    UInt(AxiDataWidth.W),
    Mux(
      io.axiRdIdIn === instAxiId,
      (io.axiRdDataIn & instMaskLow) >> instAlignedOffsetLow,
      (io.axiRdDataIn & memMaskLow) >> memAlignedOffsetLow
    )
  )

  protected val axiRdDataHig = WireDefault(
    UInt(AxiDataWidth.W),
    Mux(
      io.axiRdIdIn === instAxiId,
      (io.axiRdDataIn & instMaskHig) << instAlignedOffsetHig,
      (io.axiRdDataIn & memMaskHig) << memAlignedOffsetHig
    )
  )

  //========================= read data oper
  protected val instDataReadReg = RegInit(0.U(AxiDataWidth.W))
  io.instRdDataOut := instDataReadReg
  for (i <- 0 until TRANS_LEN) {
    when(rdHdShk && io.axiRdIdIn === instAxiId) {
      when((~instTransAligned) && instOverstep) {
        when(instTransLen(0) =/= 0.U) {
          instDataReadReg := instDataReadReg | axiRdDataHig
        }.otherwise {
          instDataReadReg := axiRdDataLow
        }
      }.elsewhen(instTransLen === i.U) {
        instDataReadReg := axiRdDataLow
      }
    }
  }

  protected val memDataReadReg = RegInit(0.U(AxiDataWidth.W))
  io.memRdDataOut := memDataReadReg
  for (i <- 0 until TRANS_LEN) {
    when(rdHdShk && io.axiRdIdIn === memAxiId) {
      when((~memTransAligned) && memOverstep) {
        when(memTransLen(0) =/= 0.U) {
          memDataReadReg := memDataReadReg | axiRdDataHig
        }.otherwise {
          memDataReadReg := axiRdDataLow
        }
      }.elsewhen(memTransLen === i.U) {
        memDataReadReg := axiRdDataLow
      }
    }
  }
}
