package treecorel2

import chisel3._
import difftest._

class TreeCoreL2(val ifDiffTest: Boolean = false) extends Module with InstConfig {
  val io = IO(new Bundle {
    val instDataIn:  UInt = Input(UInt(InstWidth.W))
    val memRdDataIn: UInt = Input(UInt(BusWidth.W))

    val instAddrOut: UInt = Output(UInt(BusWidth.W))
    val instEnaOut:  Bool = Output(Bool())

    val memAddrOut:   UInt = Output(UInt(BusWidth.W))
    val memWtEnaOut:  Bool = Output(Bool())
    val memValidOut:  Bool = Output(Bool())
    val memMaskOut:   UInt = Output(UInt(BusWidth.W))
    val memWtDataOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val pcUnit = Module(new PCReg)
  // protected val instCacheUnit = Module(new InstCache)
  protected val if2idUnit   = Module(new IFToID)
  protected val regFile     = Module(new RegFile(ifDiffTest))
  protected val instDecoder = Module(new InstDecoderStage)
  protected val id2exUnit   = Module(new IDToEX)
  protected val execUnit    = Module(new ExecutionStage)
  protected val ex2maUnit   = Module(new EXToMA)
  protected val memAccess   = Module(new MemoryAccessStage)
  protected val ma2wbUnit   = Module(new MAToWB)
  protected val forwardUnit = Module(new ForWard)
  protected val controlUnit = Module(new Control)
  //@printf(p"[TreeCoreL2]this.reset = ${Hexadecimal(this.reset.asBool())}\n\n\n")

  io.instAddrOut := pcUnit.io.instAddrOut
  io.instEnaOut  := pcUnit.io.instEnaOut
  // TODO: need to pass extra instAddr to the next stage?
  // if to id
  if2idUnit.io.ifInstAddrIn := pcUnit.io.instAddrOut
  if2idUnit.io.ifInstDataIn := io.instDataIn
  if2idUnit.io.ifFlushIn    := controlUnit.io.flushIfOut

  // id
  instDecoder.io.instAddrIn := if2idUnit.io.idInstAddrOut
  instDecoder.io.instDataIn := if2idUnit.io.idInstDataOut
  instDecoder.io.rdDataAIn  := regFile.io.rdDataAOut
  instDecoder.io.rdDataBIn  := regFile.io.rdDataBOut

  regFile.io.rdEnaAIn  := instDecoder.io.rdEnaAOut
  regFile.io.rdAddrAIn := instDecoder.io.rdAddrAOut
  regFile.io.rdEnaBIn  := instDecoder.io.rdEnaBOut
  regFile.io.rdAddrBIn := instDecoder.io.rdAddrBOut

  // id to ex
  id2exUnit.io.idAluOperTypeIn  := instDecoder.io.exuOperTypeOut
  id2exUnit.io.idRsValAIn       := instDecoder.io.rsValAOut
  id2exUnit.io.idRsValBIn       := instDecoder.io.rsValBOut
  id2exUnit.io.idWtEnaIn        := instDecoder.io.wtEnaOut
  id2exUnit.io.idWtAddrIn       := instDecoder.io.wtAddrOut
  id2exUnit.io.diffIfSkipInstIn := if2idUnit.io.diffIfSkipInstOut
  // ex
  // for jal and jalr inst(in execUnit's beu)
  execUnit.io.exuOperNumIn          := instDecoder.io.exuOperNumOut
  execUnit.io.exuOperTypeInfromId := instDecoder.io.exuOperTypeOut
  execUnit.io.offsetIn            := instDecoder.io.exuOffsetOut // important!!!

  execUnit.io.exuOperTypeIn := id2exUnit.io.exAluOperTypeOut
  execUnit.io.rsValAIn      := id2exUnit.io.exRsValAOut
  execUnit.io.rsValBIn      := id2exUnit.io.exRsValBOut
  // ex to ma
  ex2maUnit.io.exDataIn         := execUnit.io.resOut
  ex2maUnit.io.exWtEnaIn        := id2exUnit.io.exWtEnaOut
  ex2maUnit.io.exWtAddrIn       := id2exUnit.io.exWtAddrOut
  ex2maUnit.io.diffIfSkipInstIn := id2exUnit.io.diffIfSkipInstOut
  // ex to pc
  pcUnit.io.ifJumpIn      := execUnit.io.ifJumpOut
  pcUnit.io.newInstAddrIn := execUnit.io.newInstAddrOut

  // ma
  memAccess.io.func3       := 0.U
  memAccess.io.resIn       := ex2maUnit.io.maDataOut
  memAccess.io.wtEnaIn     := ex2maUnit.io.maWtEnaOut
  memAccess.io.wtAddrIn    := ex2maUnit.io.maWtAddrOut
  memAccess.io.memRdDataIn := io.memRdDataIn

  io.memAddrOut   := memAccess.io.memAddrOut
  io.memWtEnaOut  := memAccess.io.memWtEnaOut
  io.memWtDataOut := memAccess.io.memWtDataOut
  io.memMaskOut   := memAccess.io.memMaskOut
  io.memValidOut  := memAccess.io.memValidOut
  // ma to wb
  ma2wbUnit.io.maDataIn         := memAccess.io.resOut
  ma2wbUnit.io.maWtEnaIn        := memAccess.io.wtEnaOut
  ma2wbUnit.io.maWtAddrIn       := memAccess.io.wtAddrOut
  ma2wbUnit.io.diffIfSkipInstIn := ex2maUnit.io.diffIfSkipInstOut
  // wb
  regFile.io.wtDataIn := ma2wbUnit.io.wbDataOut
  regFile.io.wtEnaIn  := ma2wbUnit.io.wbWtEnaOut
  regFile.io.wtAddrIn := ma2wbUnit.io.wbWtAddrOut

  // forward control unit
  forwardUnit.io.exDataIn   := ex2maUnit.io.exDataIn
  forwardUnit.io.exWtEnaIn  := ex2maUnit.io.exWtEnaIn
  forwardUnit.io.exWtAddrIn := ex2maUnit.io.exWtAddrIn

  forwardUnit.io.maDataIn   := ma2wbUnit.io.maDataIn
  forwardUnit.io.maWtEnaIn  := ma2wbUnit.io.maWtEnaIn
  forwardUnit.io.maWtAddrIn := ma2wbUnit.io.maWtAddrIn

  forwardUnit.io.idRdEnaAIn  := instDecoder.io.rdEnaAOut
  forwardUnit.io.idRdAddrAIn := instDecoder.io.rdAddrAOut
  forwardUnit.io.idRdEnaBIn  := instDecoder.io.rdEnaBOut
  forwardUnit.io.idRdAddrBIn := instDecoder.io.rdAddrBOut

  instDecoder.io.fwRsEnaAIn := forwardUnit.io.fwRsEnaAOut
  instDecoder.io.fwRsValAIn := forwardUnit.io.fwRsValAOut
  instDecoder.io.fwRsEnaBIn := forwardUnit.io.fwRsEnaBOut
  instDecoder.io.fwRsValBIn := forwardUnit.io.fwRsValBOut

  // branch control
  controlUnit.io.jumpTypeIn := execUnit.io.jumpTypeOut

  if (ifDiffTest) {
    // commit
    val diffCommitState: DifftestInstrCommit = Module(new DifftestInstrCommit())
    val instValidWire = pcUnit.io.instEnaOut && !this.reset.asBool() && (io.instDataIn =/= 0.U)

    diffCommitState.io.clock  := this.clock
    diffCommitState.io.coreid := 0.U
    diffCommitState.io.index  := 0.U
    // skip the flush inst(nop)
    diffCommitState.io.skip     := ma2wbUnit.io.diffIfSkipInstOut
    diffCommitState.io.isRVC    := false.B
    diffCommitState.io.scFailed := false.B

    diffCommitState.io.valid := RegNext(RegNext(RegNext(RegNext(RegNext(instValidWire)))))
    diffCommitState.io.pc    := RegNext(RegNext(RegNext(RegNext(RegNext(pcUnit.io.instAddrOut)))))
    diffCommitState.io.instr := RegNext(RegNext(RegNext(RegNext(RegNext(io.instDataIn)))))
    diffCommitState.io.wen   := RegNext(RegNext(RegNext(RegNext(RegNext(ma2wbUnit.io.wbWtEnaOut)))))
    diffCommitState.io.wdata := RegNext(RegNext(RegNext(RegNext(RegNext(ma2wbUnit.io.wbDataOut)))))
    diffCommitState.io.wdest := RegNext(RegNext(RegNext(RegNext(RegNext(ma2wbUnit.io.wbWtAddrOut)))))

    // CSR State
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
    diffCsrState.io.priviledgeMode := 0.U

    // trap event
    val diffTrapState = Module(new DifftestTrapEvent)
    val instCnt       = RegInit(0.U(BusWidth.W))
    val cycleCnt      = RegInit(0.U(BusWidth.W))
    val trapReg       = RegNext(RegNext(RegNext(RegNext(RegNext(io.instDataIn === TrapInst.U, false.B)))))

    instCnt  := instCnt + instValidWire
    cycleCnt := Mux(trapReg, 0.U, cycleCnt + 1.U)

    diffTrapState.io.clock    := this.clock
    diffTrapState.io.coreid   := 0.U
    diffTrapState.io.valid    := trapReg
    diffTrapState.io.code     := 0.U // GoodTrap
    diffTrapState.io.pc       := RegNext(RegNext(RegNext(RegNext(RegNext(pcUnit.io.instAddrOut)))))
    diffTrapState.io.cycleCnt := cycleCnt
    diffTrapState.io.instrCnt := instCnt
  } // ifDiffTest
}
