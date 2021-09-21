package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import difftest._
import treecorel2.common.ConstVal._

class CSRReg(val ifDiffTest: Boolean) extends Module with InstConfig {
  val io = IO(new Bundle {
    // from id
    val rdAddrIn: UInt = Input(UInt(CSRAddrLen.W))
    // from ex's out
    val wtEnaIn:      Bool = Input(Bool())
    val wtInstTypeIn: UInt = Input(UInt(InstOperTypeLen.W))
    val wtDataIn:     UInt = Input(UInt(BusWidth.W))
    // to ex's in
    val rdDataOut: UInt = Output(UInt(BusWidth.W))
    // to difftest
    val ifNeedSkip: Bool = Output(Bool())
  })

  protected val privMode: UInt = RegInit(0.U(PrivModeLen.W))
  // machine mode
  protected val mstatus: UInt = RegInit(0.U(BusWidth.W))
  protected val mie:     UInt = RegInit(0.U(BusWidth.W))
  protected val mtvec:   UInt = RegInit(0.U(BusWidth.W))
  protected val mepc:    UInt = RegInit(0.U(BusWidth.W))
  protected val mcause:  UInt = RegInit(0.U(BusWidth.W))
  protected val mtval:   UInt = RegInit(0.U(BusWidth.W))
  protected val mip:     UInt = RegInit(0.U(BusWidth.W))
  protected val mcycle:  UInt = RegInit(0.U(BusWidth.W))

  //TODO: maybe some bug? the right value after wtena sig trigger
  when(io.wtEnaIn) {
    mcycle := io.wtDataIn
  }.otherwise {
    mcycle := mcycle + 1.U(BusWidth.W)
  }

  io.rdDataOut := MuxLookup(
    io.rdAddrIn,
    0.U(BusWidth.W),
    Seq(
      mStatusAddr -> mstatus,
      mIeAddr     -> mie,
      mTvecAddr   -> mtvec,
      mEpcAddr    -> mepc,
      mCauseAddr  -> mcause,
      mTvalAddr   -> mtval,
      mIpAddr     -> mip,
      mCycleAddr  -> mcycle
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
    diffArchState.io.intrNO      := 0.U
    diffArchState.io.cause       := 0.U
    diffArchState.io.exceptionPC := 0.U
    // diffArchState.io.exceptionInst := 0.U // FIXME: current version don't have this api, maybe need to update the difftest

    val diffCsrState = Module(new DifftestCSRState())
    diffCsrState.io.clock          := this.clock
    diffCsrState.io.coreid         := 0.U
    diffCsrState.io.mstatus        := 0.U
    diffCsrState.io.mcause         := 0.U
    diffCsrState.io.mepc           := 0.U
    diffCsrState.io.sstatus        := 0.U
    diffCsrState.io.scause         := 0.U
    diffCsrState.io.sepc           := 0.U
    diffCsrState.io.satp           := 0.U
    diffCsrState.io.mip            := 0.U
    diffCsrState.io.mie            := 0.U
    diffCsrState.io.mscratch       := 0.U
    diffCsrState.io.sscratch       := 0.U
    diffCsrState.io.mideleg        := 0.U
    diffCsrState.io.medeleg        := 0.U
    diffCsrState.io.mtval          := 0.U
    diffCsrState.io.stval          := 0.U
    diffCsrState.io.mtvec          := 0.U
    diffCsrState.io.stvec          := 0.U
    diffCsrState.io.priviledgeMode := privMode
  }
}
