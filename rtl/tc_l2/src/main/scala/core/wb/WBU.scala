package treecorel2

import chisel3._
import chisel3.util._

import difftest._

import treecorel2.common.ConstVal

class WBU extends Module {
  val io = IO(new Bundle {
    val globalEn        = Input(Bool())
    val socEn           = Input(Bool())
    val mem2wb          = Flipped(new MEM2WBIO)
    val wbdata          = new WBDATAIO
    val gpr             = Input(Vec(32, UInt(64.W)))
    val instComm        = Flipped(new DiffInstrCommitIO)
    val archIntRegState = Flipped(new DiffArchIntRegStateIO)
    val csrState        = Flipped(new DiffCSRStateIO)
    val trapEvt         = Flipped(new DiffTrapEventIO)
    val archFpRegState  = Flipped(new DiffArchFpRegStateIO)
    val archEvt         = Flipped(new DiffArchEventIO)
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

  protected val cycleCnt = RegInit(0.U(64.W))
  protected val instrCnt = RegInit(0.U(64.W))
  cycleCnt := cycleCnt + 1.U
  when(io.globalEn && valid) { instrCnt := instrCnt + 1.U }

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

  io.instComm.clock    := clock
  io.instComm.coreid   := 0.U
  io.instComm.index    := 0.U
  io.instComm.valid    := diffValid && ~timeIntrEnReg
  io.instComm.pc       := RegEnable(pc, 0.U, io.globalEn)
  io.instComm.instr    := RegEnable(inst, 0.U, io.globalEn)
  io.instComm.special  := 0.U
  io.instComm.skip     := diffValid && RegEnable(printVis || mcycleVis || mmioEn || mipVis, false.B, io.globalEn)
  io.instComm.isRVC    := false.B
  io.instComm.scFailed := false.B
  io.instComm.wen      := RegEnable(wen, false.B, io.globalEn)
  io.instComm.wdata    := RegEnable(wbdata, 0.U, io.globalEn)
  io.instComm.wdest    := RegEnable(wdest, 0.U, io.globalEn)

  io.archIntRegState.clock  := clock
  io.archIntRegState.coreid := 0.U
  io.archIntRegState.gpr    := io.gpr

  io.csrState.clock          := clock
  io.csrState.coreid         := 0.U
  io.csrState.mstatus        := csr.mstatus
  io.csrState.mcause         := csr.mcause
  io.csrState.mepc           := csr.mepc
  io.csrState.sstatus        := csr.mstatus & "h8000_0003_000d_e122".U
  io.csrState.scause         := 0.U
  io.csrState.sepc           := 0.U
  io.csrState.satp           := 0.U
  io.csrState.mip            := 0.U
  io.csrState.mie            := csr.mie
  io.csrState.mscratch       := csr.mscratch
  io.csrState.sscratch       := 0.U
  io.csrState.mideleg        := 0.U
  io.csrState.medeleg        := csr.medeleg
  io.csrState.mtval          := 0.U
  io.csrState.stval          := 0.U
  io.csrState.mtvec          := csr.mtvec
  io.csrState.stvec          := 0.U
  io.csrState.priviledgeMode := 3.U

  io.archEvt.clock         := clock
  io.archEvt.coreid        := 0.U
  io.archEvt.intrNO        := Mux(diffValid && timeIntrEnReg, 7.U, 0.U)
  io.archEvt.cause         := 0.U
  io.archEvt.exceptionPC   := RegEnable(pc, 0.U, io.globalEn)
  io.archEvt.exceptionInst := RegEnable(inst, 0.U, io.globalEn)

  io.trapEvt.clock    := clock
  io.trapEvt.coreid   := 0.U
  io.trapEvt.valid    := diffValid && RegEnable(haltVis, false.B, io.globalEn)
  io.trapEvt.code     := io.gpr(10)(7, 0)
  io.trapEvt.pc       := RegEnable(pc, 0.U, io.globalEn)
  io.trapEvt.cycleCnt := cycleCnt
  io.trapEvt.instrCnt := instrCnt

  io.archFpRegState.clock  := clock
  io.archFpRegState.coreid := 0.U
  io.archFpRegState.fpr    := RegInit(VecInit(Seq.fill(32)(0.U(64.W))))
}
