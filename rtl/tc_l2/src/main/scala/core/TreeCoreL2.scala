package treecorel2

import chisel3._
import difftest._

class TreeCoreL2(val ifDiffTest: Boolean = false, val ifSoC: Boolean = false) extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    val inst: AXI4USERIO = Flipped(new AXI4USERIO)
    val mem:  AXI4USERIO = Flipped(new AXI4USERIO)
    val uart: UARTIO     = new UARTIO
  })

  protected val pcUnit      = Module(new PCReg(ifSoC))
  protected val if2id       = Module(new IFToID)
  protected val regFile     = Module(new RegFile(ifDiffTest))
  protected val idUnit      = Module(new InstDecoderStage)
  protected val id2ex       = Module(new IDToEX)
  protected val execUnit    = Module(new ExecutionStage)
  protected val ex2ma       = Module(new EXToMA)
  protected val maUnit      = Module(new MemoryAccessStage)
  protected val ma2wb       = Module(new MAToWB)
  protected val forwardUnit = Module(new ForWard)
  protected val controlUnit = Module(new Control)
  protected val csrUnit     = Module(new CSRReg(ifDiffTest))
  protected val clintUnit   = Module(new CLINT)

  io.inst <> pcUnit.io.axi
  io.mem  <> maUnit.io.axi

  // ex to pc
  pcUnit.io.ifJumpIn      := controlUnit.io.ifJumpOut
  pcUnit.io.newInstAddrIn := controlUnit.io.newInstAddrOut
  pcUnit.io.stallIfIn     := controlUnit.io.stallIfOut
  pcUnit.io.maStallIfIn   := controlUnit.io.maStallIfOut
  // TODO: need to pass extra instAddr to the next stage?
  // if to id
  if2id.io.instIn.addr := pcUnit.io.axi.addr
  if2id.io.instIn.data := pcUnit.io.instDataOut
  if2id.io.ifFlushIn   := controlUnit.io.flushIfOut

  // id
  idUnit.io.inst   <> if2id.io.instOut
  id2ex.io.instIn  <> if2id.io.instOut
  ex2ma.io.instIn  <> id2ex.io.instOut
  maUnit.io.instIn <> ex2ma.io.instOut
  ma2wb.io.instIn  <> maUnit.io.instOut

  idUnit.io.rdDataAIn := regFile.io.rdDataAOut
  idUnit.io.rdDataBIn := regFile.io.rdDataBOut

  // for load correlation
  idUnit.io.exuOperTypeIn := id2ex.io.exAluOperTypeOut
  idUnit.io.exuWtAddrIn   := id2ex.io.exWtAddrOut

  regFile.io.rdEnaAIn  := idUnit.io.rdEnaAOut
  regFile.io.rdAddrAIn := idUnit.io.rdAddrAOut
  regFile.io.rdEnaBIn  := idUnit.io.rdEnaBOut
  regFile.io.rdAddrBIn := idUnit.io.rdAddrBOut

  // id to ex
  id2ex.io.idAluOperTypeIn := idUnit.io.exuOperTypeOut
  id2ex.io.idRsValAIn      := idUnit.io.rsValAOut
  id2ex.io.idRsValBIn      := idUnit.io.rsValBOut
  id2ex.io.idWtEnaIn       := idUnit.io.wtEnaOut
  id2ex.io.idWtAddrIn      := idUnit.io.wtAddrOut
  id2ex.io.lsuFunc3In      := idUnit.io.lsuFunc3Out
  id2ex.io.lsuWtEnaIn      := idUnit.io.lsuWtEnaOut
  id2ex.io.ifFlushIn       := controlUnit.io.flushIdOut

  // ex
  execUnit.io.offsetIn := idUnit.io.exuOffsetOut // important!!!

  execUnit.io.exuOperTypeIn := id2ex.io.exAluOperTypeOut
  execUnit.io.rsValAIn      := id2ex.io.exRsValAOut
  execUnit.io.rsValBIn      := id2ex.io.exRsValBOut
  execUnit.io.csrRdDataIn   := RegNext(csrUnit.io.rdDataOut) // TODO: need to refactor
  // ex to ma
  ex2ma.io.exDataIn   := execUnit.io.wtDataOut
  ex2ma.io.exWtEnaIn  := id2ex.io.exWtEnaOut
  ex2ma.io.exWtAddrIn := id2ex.io.exWtAddrOut

  ex2ma.io.lsuFunc3In    := id2ex.io.lsuFunc3Out
  ex2ma.io.lsuWtEnaIn    := id2ex.io.lsuWtEnaOut
  ex2ma.io.lsuOperTypeIn := execUnit.io.exuOperTypeIn
  ex2ma.io.lsuValAIn     := execUnit.io.rsValAIn
  ex2ma.io.lsuValBIn     := execUnit.io.rsValBIn
  ex2ma.io.lsuOffsetIn   := RegNext(execUnit.io.offsetIn) // important!!

  // ma
  maUnit.io.memFunc3In    := ex2ma.io.lsuFunc3Out
  maUnit.io.memOperTypeIn := ex2ma.io.lsuOperTypeOut
  maUnit.io.memValAIn     := ex2ma.io.lsuValAOut
  maUnit.io.memValBIn     := ex2ma.io.lsuValBOut
  maUnit.io.memOffsetIn   := ex2ma.io.lsuOffsetOut

  maUnit.io.wtDataIn   := ex2ma.io.maDataOut
  maUnit.io.wtEnaIn    := ex2ma.io.maWtEnaOut
  maUnit.io.wtAddrIn   := ex2ma.io.maWtAddrOut
  ex2ma.io.lsuWtEnaOut := DontCare

  // ma to wb
  ma2wb.io.maDataIn          := maUnit.io.wtDataOut
  ma2wb.io.maWtEnaIn         := maUnit.io.wtEnaOut
  ma2wb.io.maWtAddrIn        := maUnit.io.wtAddrOut
  ma2wb.io.ifValidIn         := maUnit.io.ifValidOut
  ma2wb.io.ifMemInstCommitIn := maUnit.io.ifMemInstCommitOut
  ma2wb.io.memIntrEnterFlag  := csrUnit.io.memIntrEnterFlag
  ma2wb.io.intrJumpInfo      <> csrUnit.io.intrJumpInfo
  // wb
  regFile.io.wtDataIn := ma2wb.io.wbDataOut
  regFile.io.wtEnaIn  := ma2wb.io.wbWtEnaOut
  regFile.io.wtAddrIn := ma2wb.io.wbWtAddrOut

  // forward control unit
  forwardUnit.io.exDataIn   := ex2ma.io.exDataIn
  forwardUnit.io.exWtEnaIn  := ex2ma.io.exWtEnaIn
  forwardUnit.io.exWtAddrIn := ex2ma.io.exWtAddrIn

  // maDataIn only come from regfile and imm
  // maDataOut have right data include load/store inst and alu calc
  forwardUnit.io.maDataIn   := ma2wb.io.maDataIn
  forwardUnit.io.maWtEnaIn  := ma2wb.io.maWtEnaIn
  forwardUnit.io.maWtAddrIn := ma2wb.io.maWtAddrIn

  forwardUnit.io.idRdEnaAIn  := idUnit.io.rdEnaAOut
  forwardUnit.io.idRdAddrAIn := idUnit.io.rdAddrAOut
  forwardUnit.io.idRdEnaBIn  := idUnit.io.rdEnaBOut
  forwardUnit.io.idRdAddrBIn := idUnit.io.rdAddrBOut

  idUnit.io.fwRsEnaAIn := forwardUnit.io.fwRsEnaAOut
  idUnit.io.fwRsValAIn := forwardUnit.io.fwRsValAOut
  idUnit.io.fwRsEnaBIn := forwardUnit.io.fwRsEnaBOut
  idUnit.io.fwRsValBIn := forwardUnit.io.fwRsValBOut

  // branch and load/store control
  controlUnit.io.excpJumpInfo     <> csrUnit.io.excpJumpInfo
  controlUnit.io.intrJumpInfo     <> csrUnit.io.intrJumpInfo
  controlUnit.io.jumpTypeIn       := idUnit.io.jumpTypeOut
  controlUnit.io.newInstAddrIn    := idUnit.io.newInstAddrOut
  controlUnit.io.stallReqFromIDIn := idUnit.io.stallReqFromIDOut
  controlUnit.io.stallReqFromMaIn := maUnit.io.stallReqOut
  // csr
  csrUnit.io.rdAddrIn          := idUnit.io.csrAddrOut
  csrUnit.io.instOperTypeIn    := idUnit.io.csrInstTypeOut
  csrUnit.io.inst              <> if2id.io.instOut
  csrUnit.io.wtEnaIn           := execUnit.io.csrwtEnaOut
  csrUnit.io.wtDataIn          := execUnit.io.csrWtDataOut
  csrUnit.io.ifMemInstCommitIn := ma2wb.io.ifMemInstCommitOut

  // clint(ma stage)
  clintUnit.io.wt       <> maUnit.io.clintWt
  clintUnit.io.rd       <> ma2wb.io.clintWt
  clintUnit.io.intrInfo <> csrUnit.io.intrInfo

  // output custom putch oper for 0x7B
  io.uart.in.valid := false.B
  when(RegNext(ma2wb.io.instOut.data) === 0x0000007b.U) {
    io.uart.out.valid := true.B
    io.uart.out.ch    := regFile.io.charDataOut
  }.otherwise {
    io.uart.out.valid := false.B
    io.uart.out.ch    := 0.U
  }
  // ouput custom gpr val
  regFile.io.debugIn := 0.U

  if (ifDiffTest) {
    // commit
    val diffCommitState: DifftestInstrCommit = Module(new DifftestInstrCommit())
    val instValidWire = pcUnit.io.instEnaOut && !this.reset.asBool() && (pcUnit.io.instDataOut =/= 0.U)

    diffCommitState.io.clock  := this.clock
    diffCommitState.io.coreid := 0.U
    diffCommitState.io.index  := 0.U
    // skip the flush inst(nop) maybe the skip cust oper only
    diffCommitState.io.skip := Mux(
      diffCommitState.io.instr === 0x0000007b.U ||
        RegNext(RegNext(RegNext(RegNext(csrUnit.io.ifNeedSkip)))) ||
        RegNext(RegNext(clintUnit.io.wt.ena || clintUnit.io.rd.ena)),
      true.B,
      false.B
    )
    diffCommitState.io.isRVC    := false.B
    diffCommitState.io.scFailed := false.B

    when(RegNext(ma2wb.io.ifMemInstCommitOut)) {
      // printf("RegNext(ma2wb.io.ifMemInstCommitOut)\n")
      when(csrUnit.io.memIntrEnterFlag) { // TODO: need to judge the inst type which trigger the interrupt
        diffCommitState.io.valid := false.B
        // printf("csrUnit.io.memIntrEnterFlag\n")
      }.otherwise {
        diffCommitState.io.valid := true.B
      }
    }.otherwise {
      diffCommitState.io.valid := RegNext(RegNext(RegNext(RegNext(RegNext(instValidWire))))) &
        (!RegNext(RegNext(RegNext(RegNext(if2id.io.diffIfSkipInstOut))))) &
        (!RegNext(RegNext(RegNext(id2ex.io.diffIdSkipInstOut)))) &
        (!(RegNext(ma2wb.io.diffMaSkipInstOut))) & (!RegNext(RegNext(RegNext(RegNext(csrUnit.io.intrJumpInfo.kind === 3.U)))))
    }

    // diffCommitState.io.pc := RegNext(RegNext(RegNext(RegNext(RegNext(pcUnit.io.axi.addr)))))
    // diffCommitState.io.instr := RegNext(RegNext(RegNext(RegNext(RegNext(pcUnit.io.instDataOut))))) // important!!!
    diffCommitState.io.pc    := RegNext(ma2wb.io.instOut.addr)
    diffCommitState.io.instr := RegNext(ma2wb.io.instOut.data)
    diffCommitState.io.wen   := RegNext(ma2wb.io.wbWtEnaOut)
    diffCommitState.io.wdata := RegNext(ma2wb.io.wbDataOut)
    diffCommitState.io.wdest := RegNext(ma2wb.io.wbWtAddrOut)

    val debugCnt: UInt = RegInit(0.U(5.W))

    when(pcUnit.io.instDataOut =/= NopInst.U) {
      // when(pcUnit.io.instDataOut === "h30051073".U) {
      debugCnt := 5.U
      // printf(p"[pc]io.inst.addr = 0x${Hexadecimal(pcUnit.io.axi.addr)}\n")
      // printf(p"[pc]pcUnit.io.instDataOut = 0x${Hexadecimal(pcUnit.io.instDataOut)}\n")
      // printf("\n")
    }

    when(debugCnt =/= 0.U) {
      debugCnt := debugCnt - 1.U
      // printf("debugCnt: %d\n", debugCnt)
      // printf(p"[pc]io.axi.valid = 0x${Hexadecimal(pcUnit.io.axi.valid)}\n")
      // printf(p"[pc]io.instDataOut = 0x${Hexadecimal(pcUnit.io.instDataOut)}\n")
      // printf(p"[pc]io.inst.addr = 0x${Hexadecimal(pcUnit.io.axi.addr)}\n")
      // printf(p"[pc]io.instEnaOut = 0x${Hexadecimal(pcUnit.io.instEnaOut)}\n")
      // printf(p"[pc]dirty = 0x${Hexadecimal(pcUnit.dirty)}\n")

      // printf(p"[if2id]io.ifFlushIn = 0x${Hexadecimal(if2id.io.ifFlushIn)}\n")
      // printf(p"[if2id]io.diffIfSkipInstOut = 0x${Hexadecimal(if2id.io.diffIfSkipInstOut)}\n")
      // printf(p"[if2id]io.instOut.addr = 0x${Hexadecimal(if2id.io.instOut.addr)}\n")
      // printf(p"[if2id]io.instOut.data = 0x${Hexadecimal(if2id.io.instOut.data)}\n")

      // printf(p"[id]io.inst.data = 0x${Hexadecimal(idUnit.io.inst.data)}\n")
      // printf(p"[id]io.rdEnaAOut = 0x${Hexadecimal(idUnit.io.rdEnaAOut)}\n")
      // printf(p"[id]io.rdAddrAOut = 0x${Hexadecimal(idUnit.io.rdAddrAOut)}\n")
      // printf(p"[id]io.rdEnaBOut = 0x${Hexadecimal(idUnit.io.rdEnaBOut)}\n")
      // printf(p"[id]io.rdAddrBOut = 0x${Hexadecimal(idUnit.io.rdAddrBOut)}\n")
      // printf(p"[id]io.exuOperTypeOut = 0x${Hexadecimal(idUnit.io.exuOperTypeOut)}\n")
      // printf(p"[id]io.lsuWtEnaOut = 0x${Hexadecimal(idUnit.io.lsuWtEnaOut)}\n")
      // printf(p"[id]io.rsValAOut = 0x${Hexadecimal(idUnit.io.rsValAOut)}\n")
      // printf(p"[id]io.rsValBOut = 0x${Hexadecimal(idUnit.io.rsValBOut)}\n")
      // printf(p"[id]io.wtEnaOut = 0x${Hexadecimal(idUnit.io.wtEnaOut)}\n")
      // printf(p"[id]io.wtAddrOut = 0x${Hexadecimal(idUnit.io.wtAddrOut)}\n")
      // printf(p"[id]io.csrInstTypeOut = 0x${Hexadecimal(idUnit.io.csrInstTypeOut)}\n")
      // printf(p"[id]io.csrAddrOut = 0x${Hexadecimal(idUnit.io.csrAddrOut)}\n")
      // printf(p"[clint]io.rd.ena = 0x${Hexadecimal(clintUnit.io.rd.ena)}\n")
      // printf(p"[clint]io.rd.addr = 0x${Hexadecimal(clintUnit.io.rd.addr)}\n")
      // printf(p"[clint]io.rd.data = 0x${Hexadecimal(clintUnit.io.rd.data)}\n")

      // printf(p"[csr]io.wtEnaIn = 0x${Hexadecimal(csrUnit.io.wtEnaIn)}\n")
      // printf(p"[csr]io.wtDataIn = 0x${Hexadecimal(csrUnit.io.wtDataIn)}\n")
      // printf(p"[csr]io.rdAddrIn = 0x${Hexadecimal(csrUnit.io.rdAddrIn)}\n")
      // printf(p"[csr]io.rdDataOut = 0x${Hexadecimal(csrUnit.io.rdDataOut)}\n")
      // printf(p"[csr]io.debugMstatus = 0x${Hexadecimal(csrUnit.io.debugMstatus)}\n")
      // printf(p"[ex]io.wtDataOut = 0x${Hexadecimal(execUnit.io.wtDataOut)}\n")

      // printf(p"[ma]io.memOperTypeIn = 0x${Hexadecimal(maUnit.io.memOperTypeIn)}\n")
      // printf(p"[ma]io.memValAIn = 0x${Hexadecimal(maUnit.io.memValAIn)}\n")
      // printf(p"[ma]io.memValBIn = 0x${Hexadecimal(maUnit.io.memValBIn)}\n")
      // printf(p"[ma]io.memOffsetIn = 0x${Hexadecimal(maUnit.io.memOffsetIn)}\n")
      // printf(p"[ma]io.axi.addr = 0x${Hexadecimal(maUnit.io.axi.addr)}\n")
      // printf(p"[ma]io.axi.wdata = 0x${Hexadecimal(maUnit.io.axi.wdata)}\n")
      // printf(p"[ma]io.axi.size = 0x${Hexadecimal(maUnit.io.axi.size)}\n")
      // printf(p"[ma]io.axi.valid = 0x${Hexadecimal(maUnit.io.axi.valid)}\n")
      // printf(p"[ma]io.axi.req = 0x${Hexadecimal(maUnit.io.axi.req)}\n")

      // printf(p"[ma]io.stallReqOut = 0x${Hexadecimal(maUnit.io.stallReqOut)}\n")
      // printf(p"[ma]io.wtDataOut = 0x${Hexadecimal(maUnit.io.wtDataOut)}\n")
      // printf(p"[ma]io.wtEnaOut = 0x${Hexadecimal(maUnit.io.wtEnaOut)}\n")
      // printf(p"[ma]io.wtAddrOut = 0x${Hexadecimal(maUnit.io.wtAddrOut)}\n")

      // printf(p"[ma2wb]io.wbDataOut   = 0x${Hexadecimal(ma2wb.io.wbDataOut)}\n")
      // printf(p"[ma2wb]io.wbWtEnaOut  = 0x${Hexadecimal(ma2wb.io.wbWtEnaOut)}\n")
      // printf(p"[ma2wb]io.wbWtAddrOut = 0x${Hexadecimal(ma2wb.io.wbWtAddrOut)}\n")

      // printf(p"[main]diffCommitState.io.pc = 0x${Hexadecimal(diffCommitState.io.pc)}\n")
      // printf(p"[main]diffCommitState.io.instr = 0x${Hexadecimal(diffCommitState.io.instr)}\n")
      // printf(p"[main]diffCommitState.io.skip = 0x${Hexadecimal(diffCommitState.io.skip)}\n")
      // printf(p"[main]diffCommitState.io.valid = 0x${Hexadecimal(diffCommitState.io.valid)}\n")
      // printf(p"[main&ma2wb]io.instOut.addr = 0x${Hexadecimal(RegNext(ma2wb.io.instOut.addr))}\n")
      // printf(p"[main&ma2wb]io.instOut.data = 0x${Hexadecimal(RegNext(ma2wb.io.instOut.data))}\n")
      // printf(p"[main&ma2wb]io.ifMemInstCommitOut = 0x${Hexadecimal(RegNext(ma2wb.io.ifMemInstCommitOut))}\n")
      // printf("\n")
    }

    val debugMemCnt: UInt = RegInit(0.U(5.W))
    when(maUnit.io.ifMemInstCommitOut) {
      debugMemCnt := 3.U
    }

    when(debugMemCnt =/= 0.U) {
      debugMemCnt := debugMemCnt - 1.U
      // printf("debugMemCnt: %d\n", debugMemCnt)

      // printf(p"[ma]io.wtDataOut = 0x${Hexadecimal(maUnit.io.wtDataOut)}\n")
      // printf(p"[ma]io.wtEnaOut = 0x${Hexadecimal(maUnit.io.wtEnaOut)}\n")
      // printf(p"[ma]io.wtAddrOut = 0x${Hexadecimal(maUnit.io.wtAddrOut)}\n")
      // printf(p"[ma2wb]io.wbDataOut = 0x${Hexadecimal(ma2wb.io.wbDataOut)}\n")
      // printf(p"[ma2wb]io.wbWtEnaOut = 0x${Hexadecimal(ma2wb.io.wbWtEnaOut)}\n")
      // printf(p"[ma2wb]io.wbWtAddrOut = 0x${Hexadecimal(ma2wb.io.wbWtAddrOut)}\n")
      // printf(p"[csr]io.debugMstatus = 0x${Hexadecimal(csrUnit.io.debugMstatus)}\n")
      // printf(p"[main]diffCommitState.io.pc = 0x${Hexadecimal(diffCommitState.io.pc)}\n")
      // printf(p"[main]diffCommitState.io.instr = 0x${Hexadecimal(diffCommitState.io.instr)}\n")
      // printf(p"[main]diffCommitState.io.skip = 0x${Hexadecimal(diffCommitState.io.skip)}\n")
      // printf(p"[main]diffCommitState.io.valid = 0x${Hexadecimal(diffCommitState.io.valid)}\n")
      // printf("\n")
    }

    // trap event
    val diffTrapState = Module(new DifftestTrapEvent)
    val instCnt       = RegInit(0.U(BusWidth.W))
    val cycleCnt      = RegInit(0.U(BusWidth.W))
    val trapReg       = RegNext(RegNext(RegNext(RegNext(RegNext(pcUnit.io.instDataOut === TrapInst.U, false.B)))))

    instCnt  := instCnt + instValidWire
    cycleCnt := Mux(trapReg, 0.U, cycleCnt + 1.U)

    diffTrapState.io.clock    := this.clock
    diffTrapState.io.coreid   := 0.U
    diffTrapState.io.valid    := trapReg
    diffTrapState.io.code     := 0.U // GoodTrap
    diffTrapState.io.pc       := RegNext(RegNext(RegNext(RegNext(RegNext(pcUnit.io.axi.addr)))))
    diffTrapState.io.cycleCnt := cycleCnt
    diffTrapState.io.instrCnt := instCnt
  } // ifDiffTest
}
