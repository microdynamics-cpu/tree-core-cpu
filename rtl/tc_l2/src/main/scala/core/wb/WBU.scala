package treecorel2

import chisel3._
import chisel3.util._

import difftest._

import treecorel2.common.ConstVal
import treecorel2.common.InstConfig

class WBU extends Module with InstConfig {
  val io = IO(new Bundle {
    val globalEn = Input(Bool())
    val socEn    = Input(Bool())
    val mem2wb   = Flipped(new MEM2WBIO)
    val wbdata   = new WBDATAIO
    val gpr      = Input(Vec(32, UInt(64.W)))
  })

  protected val wbReg      = RegEnable(io.mem2wb, WireInit(0.U.asTypeOf(new MEM2WBIO())), io.globalEn)
  protected val valid      = wbReg.valid
  protected val inst       = wbReg.inst
  protected val pc         = wbReg.pc
  protected val isa        = wbReg.isa
  protected val src1       = wbReg.src1
  protected val src2       = wbReg.src2
  protected val imm        = wbReg.imm
  protected val wen        = wbReg.wen
  protected val wdest      = wbReg.wdest
  protected val aluRes     = wbReg.aluRes
  protected val branch     = wbReg.branch
  protected val tgt        = wbReg.tgt
  protected val link       = wbReg.link
  protected val auipc      = wbReg.auipc
  protected val loadData   = wbReg.loadData
  protected val csrData    = wbReg.csrData
  protected val cvalid     = wbReg.cvalid
  protected val timeIntrEn = wbReg.timeIntrEn
  protected val ecallEn    = wbReg.ecallEn
  protected val csr        = wbReg.csr

  protected val wbdata = aluRes | link | auipc | loadData | csrData

  io.wbdata.wen   := valid && wen
  io.wbdata.wdest := wdest
  io.wbdata.data  := wbdata

  protected val printVis = inst(6, 0) === "h7b".U(7.W)
  protected val haltVis  = inst(6, 0) === "h6b".U(7.W)

  when(~io.socEn) {
    when(io.globalEn && valid && printVis) {
      printf("%c", io.gpr(10))
    }
  }

  // for difftest commit
  protected val mmioEn        = cvalid
  protected val csrVis        = isa.CSRRW || isa.CSRRS || isa.CSRRC || isa.CSRRWI || isa.CSRRSI || isa.CSRRCI
  protected val mcycleVis     = csrVis && (inst(31, 20) === ConstVal.mcycleAddr)
  protected val mipVis        = csrVis && (inst(31, 20) === ConstVal.mipAddr)
  protected val timeIntrEnReg = RegEnable(timeIntrEn, false.B, io.globalEn)
  protected val diffValid     = io.globalEn && (RegEnable(valid, false.B, io.globalEn) || timeIntrEnReg)

  if (!SoCEna) {
    val instComm        = Module(new DifftestInstrCommit)
    val archIntRegState = Module(new DifftestArchIntRegState)
    val csrState        = Module(new DifftestCSRState)
    val trapEvt         = Module(new DifftestTrapEvent)
    val archFpRegState  = Module(new DifftestArchFpRegState)
    val archEvt         = Module(new DifftestArchEvent)
    val cycleCnt        = Counter(Int.MaxValue) // NOTE: maybe overflow?
    val instrCnt        = Counter(Int.MaxValue)
    cycleCnt.inc()
    when(io.globalEn && valid) { instrCnt.inc() }

    instComm.io.clock    := clock
    instComm.io.coreid   := 0.U
    instComm.io.index    := 0.U
    instComm.io.valid    := diffValid && ~timeIntrEnReg
    instComm.io.pc       := RegEnable(pc, 0.U, io.globalEn)
    instComm.io.instr    := RegEnable(inst, 0.U, io.globalEn)
    instComm.io.special  := 0.U
    instComm.io.skip     := diffValid && RegEnable(printVis || mcycleVis || mmioEn || mipVis, false.B, io.globalEn)
    instComm.io.isRVC    := false.B
    instComm.io.scFailed := false.B
    instComm.io.wen      := RegEnable(wen, false.B, io.globalEn)
    instComm.io.wdata    := RegEnable(wbdata, 0.U, io.globalEn)
    instComm.io.wdest    := RegEnable(wdest, 0.U, io.globalEn)

    archIntRegState.io.clock  := clock
    archIntRegState.io.coreid := 0.U
    archIntRegState.io.gpr    := io.gpr

    csrState.io.clock          := clock
    csrState.io.coreid         := 0.U
    csrState.io.mstatus        := csr.mstatus
    csrState.io.mcause         := csr.mcause
    csrState.io.mepc           := csr.mepc
    csrState.io.sstatus        := csr.mstatus & "h8000_0003_000d_e122".U
    csrState.io.scause         := 0.U
    csrState.io.sepc           := 0.U
    csrState.io.satp           := 0.U
    csrState.io.mip            := 0.U
    csrState.io.mie            := csr.mie
    csrState.io.mscratch       := csr.mscratch
    csrState.io.sscratch       := 0.U
    csrState.io.mideleg        := 0.U
    csrState.io.medeleg        := csr.medeleg
    csrState.io.mtval          := 0.U
    csrState.io.stval          := 0.U
    csrState.io.mtvec          := csr.mtvec
    csrState.io.stvec          := 0.U
    csrState.io.priviledgeMode := 3.U

    archEvt.io.clock         := clock
    archEvt.io.coreid        := 0.U
    archEvt.io.intrNO        := Mux(diffValid && timeIntrEnReg, 7.U, 0.U)
    archEvt.io.cause         := 0.U
    archEvt.io.exceptionPC   := RegEnable(pc, 0.U, io.globalEn)
    archEvt.io.exceptionInst := RegEnable(inst, 0.U, io.globalEn)

    trapEvt.io.clock    := clock
    trapEvt.io.coreid   := 0.U
    trapEvt.io.valid    := diffValid && RegEnable(haltVis, false.B, io.globalEn)
    trapEvt.io.code     := io.gpr(10)(7, 0)
    trapEvt.io.pc       := RegEnable(pc, 0.U, io.globalEn)
    trapEvt.io.cycleCnt := cycleCnt.value
    trapEvt.io.instrCnt := instrCnt.value

    archFpRegState.io.clock  := clock
    archFpRegState.io.coreid := 0.U
    archFpRegState.io.fpr    := RegInit(VecInit(Seq.fill(32)(0.U(64.W))))
  }
}
