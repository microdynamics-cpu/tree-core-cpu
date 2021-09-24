package treecorel2

import chisel3._
import chisel3.util._
import difftest._
import treecorel2.common.ConstVal._

class CSRReg(val ifDiffTest: Boolean) extends Module with InstConfig {
  val io = IO(new Bundle {
    // from id
    val rdAddrIn:       UInt   = Input(UInt(CSRAddrLen.W))
    val instOperTypeIn: UInt   = Input(UInt(InstOperTypeLen.W))
    val inst:           INSTIO = new INSTIO
    // from ex's out
    val wtEnaIn:  Bool = Input(Bool())
    val wtDataIn: UInt = Input(UInt(BusWidth.W))
    // from clint
    val intrInfo: INTRIO = Flipped(new INTRIO)
    // to ex's in
    val rdDataOut: UInt = Output(UInt(BusWidth.W))
    // to difftest
    val ifNeedSkip: Bool = Output(Bool())
    // to id
    val excpJumpInfo: JUMPIO = new JUMPIO
    val intrJumpInfo: JUMPIO = new JUMPIO
  })

  protected val privMode: UInt = RegInit(mPrivMode)
  protected val addrReg:  UInt = RegNext(io.rdAddrIn)
  // machine mode reg
  protected val mhartid:  UInt = RegInit(0.U(BusWidth.W))
  protected val mstatus:  UInt = RegInit("h00001880".U(BusWidth.W))
  protected val mie:      UInt = RegInit(0.U(BusWidth.W))
  protected val mtvec:    UInt = RegInit(0.U(BusWidth.W))
  protected val mscratch: UInt = RegInit(0.U(BusWidth.W))
  protected val mepc:     UInt = RegInit(0.U(BusWidth.W))
  protected val mcause:   UInt = RegInit(0.U(BusWidth.W))
  protected val mtval:    UInt = RegInit(0.U(BusWidth.W))
  protected val mip:      UInt = RegInit(0.U(BusWidth.W))
  protected val mcycle:   UInt = RegInit(0.U(BusWidth.W))

  protected val excpJumpType = WireDefault(UInt(JumpTypeLen.W), noJumpType)
  protected val excpJumpAddr = WireDefault(UInt(BusWidth.W), 0.U)
  // difftest run right code in user mode, when throw exception, enter machine mode
  when(io.instOperTypeIn === sysECALLType) {
    mepc         := io.inst.addr
    mcause       := 11.U // ecall cause code
    mstatus      := Cat(mstatus(63, 13), privMode(1, 0), mstatus(10, 8), mstatus(3), mstatus(6, 4), 0.U, mstatus(2, 0))
    excpJumpType := csrJumpType
    excpJumpAddr := Cat(mtvec(63, 2), Fill(2, 0.U))
  }

  io.excpJumpInfo.kind := excpJumpType
  io.excpJumpInfo.addr := excpJumpAddr

  // intr
  protected val intrJumpType    = WireDefault(UInt(JumpTypeLen.W), noJumpType)
  protected val intrJumpAddr    = WireDefault(UInt(BusWidth.W), 0.U)
  protected val intrJumpTypeReg = RegNext(intrJumpType)

  // solve the mtimecmp init val is 0 and trigger interrupt bug
  // use the fsm to delay one inst
  protected val enumIDLE :: enumIntr :: Nil = Enum(2)
  protected val intrState                   = RegInit(enumIDLE)
  switch(intrState) {
    is(enumIDLE) {
      when(mstatus(3) === 1.U && mie(7) === 1.U) {
        intrState := enumIntr
      }
    }
    is(enumIntr) {
      when(io.inst.data =/= NopInst.U && io.intrInfo.mtip === true.B) {
        mepc         := io.inst.addr
        mcause       := "h8000000000000007".U
        mstatus      := Cat(mstatus(63, 13), privMode(1, 0), mstatus(10, 8), mstatus(3), mstatus(6, 4), 0.U, mstatus(2, 0))
        intrJumpType := csrJumpType
        intrJumpAddr := Cat(mtvec(63, 2), Fill(2, 0.U))
        intrState    := enumIDLE
      }
    }
  }

  io.intrJumpInfo.kind := intrJumpType
  io.intrJumpInfo.addr := intrJumpAddr

  // mret
  when(io.instOperTypeIn === sysMRETType) {
    mstatus      := Cat(mstatus(63, 13), uPrivMode(1, 0), mstatus(10, 8), 1.U, mstatus(6, 4), mstatus(7), mstatus(2, 0))
    excpJumpType := csrJumpType
    excpJumpAddr := mepc
    privMode     := mstatus(12, 11) // mstatus.MPP
  }

  //TODO: maybe some bug? the right value after wtena sig trigger
  // the addrReg is also the wt addr
  when(io.wtEnaIn) {
    switch(addrReg) {
      is(mHartidAddr) {
        mhartid := io.wtDataIn
      }
      is(mStatusAddr) {
        // solve SD bit when the XS[1:0] or FS[1:0] is '11'
        when(io.wtDataIn(16, 15) === 3.U || io.wtDataIn(14, 13) === 3.U) {
          mstatus := io.wtDataIn | "h8000000000000000".U
        }.otherwise {
          mstatus := io.wtDataIn
        }
      }
      is(mIeAddr) {
        mie := io.wtDataIn
      }
      is(mTvecAddr) {
        mtvec := io.wtDataIn
      }
      is(mScratchAddr) {
        mscratch := io.wtDataIn
      }
      is(mEpcAddr) {
        mepc := io.wtDataIn
      }
      is(mCauseAddr) {
        mcause := io.wtDataIn
      }
      is(mTvalAddr) {
        mtval := io.wtDataIn
      }
      is(mIpAddr) {
        mip := io.wtDataIn
      }
      is(mCycleAddr) {
        mcycle := io.wtDataIn
      }
    }
  }.otherwise {
    mcycle := mcycle + 1.U(BusWidth.W)
  }

  io.rdDataOut := MuxLookup(
    io.rdAddrIn,
    0.U(BusWidth.W),
    Seq(
      mHartidAddr  -> mhartid,
      mStatusAddr  -> mstatus,
      mIeAddr      -> mie,
      mTvecAddr    -> mtvec,
      mScratchAddr -> mscratch,
      mEpcAddr     -> mepc,
      mCauseAddr   -> mcause,
      mTvalAddr    -> mtval,
      mIpAddr      -> mip,
      mCycleAddr   -> mcycle
    )
  )

  when(io.rdAddrIn === mCycleAddr) {
    io.ifNeedSkip := true.B
  }.otherwise {
    io.ifNeedSkip := false.B
  }

  if (ifDiffTest) {
    val diffArchState = Module(new DifftestArchEvent())
    diffArchState.io.clock       := this.clock
    diffArchState.io.coreid      := 0.U
    diffArchState.io.intrNO      := Mux(intrJumpTypeReg === csrJumpType, 7.U, 0.U)
    diffArchState.io.cause       := 0.U
    diffArchState.io.exceptionPC := Mux(intrJumpTypeReg === csrJumpType, mepc, 0.U)
    // diffArchState.io.exceptionInst := 0.U // FIXME: current version don't have this api, maybe need to update the difftest

    val diffCsrState = Module(new DifftestCSRState())
    diffCsrState.io.clock          := this.clock
    diffCsrState.io.coreid         := 0.U
    diffCsrState.io.mstatus        := mstatus
    diffCsrState.io.mcause         := mcause
    diffCsrState.io.mepc           := mepc
    diffCsrState.io.sstatus        := mstatus & "h80000003000DE122".U
    diffCsrState.io.scause         := 0.U
    diffCsrState.io.sepc           := 0.U
    diffCsrState.io.satp           := 0.U
    diffCsrState.io.mip            := 0.U
    diffCsrState.io.mie            := mie
    diffCsrState.io.mscratch       := mscratch
    diffCsrState.io.sscratch       := 0.U
    diffCsrState.io.mideleg        := 0.U
    diffCsrState.io.medeleg        := 0.U
    diffCsrState.io.mtval          := 0.U
    diffCsrState.io.stval          := 0.U
    diffCsrState.io.mtvec          := mtvec //exec
    diffCsrState.io.stvec          := 0.U
    diffCsrState.io.priviledgeMode := privMode
  }
}
