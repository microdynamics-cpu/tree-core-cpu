package treecorel2

import chisel3._
import chisel3.util._
import difftest._

import treecorel2.common.ConstVal

class CSRReg extends Module {
  val io = IO(new Bundle {
    val globalEn   = Input(Bool())
    val pc         = Input(UInt(64.W))
    val inst       = Input(UInt(64.W))
    val src        = Input(UInt(64.W))
    val data       = Output(UInt(64.W))
    val mtip       = Input(Bool())
    val timeIntrEn = Output(Bool())
    val ecallEn    = Output(Bool())

    //difftest
    val csrState = Flipped(new DiffCSRStateIO)
  })

  protected val csrrwVis  = (io.inst === BitPat("b????????????_?????_001_?????_1110011"))
  protected val csrrwiVis = (io.inst === BitPat("b????????????_?????_101_?????_1110011"))
  protected val csrrsVis  = (io.inst === BitPat("b????????????_?????_010_?????_1110011"))
  protected val csrrsiVis = (io.inst === BitPat("b????????????_?????_110_?????_1110011"))
  protected val csrrcVis  = (io.inst === BitPat("b????????????_?????_011_?????_1110011"))
  protected val csrrciVis = (io.inst === BitPat("b????????????_?????_111_?????_1110011"))
  protected val csrVis    = csrrcVis || csrrciVis || csrrsVis || csrrsiVis || csrrwVis || csrrwiVis
  protected val mretVis   = (io.inst === BitPat("b001100000010_00000_000_00000_1110011"))
  protected val ecallVis  = (io.inst === BitPat("b000000000000_00000_000_00000_1110011"))
  protected val zimm      = ZeroExt(io.inst(19, 15), 64)
  protected val addr      = io.inst(31, 20)

  protected val mcycle   = RegInit(0.U(64.W))
  protected val mstatus  = RegInit(0.U(64.W))
  protected val mtvec    = RegInit(0.U(64.W))
  protected val mcause   = RegInit(0.U(64.W))
  protected val mepc     = RegInit(0.U(64.W))
  protected val mie      = RegInit(0.U(64.W))
  protected val mip      = RegInit(0.U(64.W))
  protected val mscratch = RegInit(0.U(64.W))
  protected val medeleg  = RegInit(0.U(64.W))
  protected val mhartid  = RegInit(0.U(64.W))

  protected val mhartidVis  = addr === ConstVal.mhartidAddr
  protected val mstatusVis  = addr === ConstVal.mstatusAddr
  protected val mieVis      = addr === ConstVal.mieAddr
  protected val mtvecVis    = addr === ConstVal.mtvecAddr
  protected val mscratchVis = addr === ConstVal.mscratchAddr
  protected val mepcVis     = addr === ConstVal.mepcAddr
  protected val mcauseVis   = addr === ConstVal.mcauseAddr
  protected val mipVis      = addr === ConstVal.mipAddr
  protected val mcycleVis   = addr === ConstVal.mcycleAddr
  protected val medelegVis  = addr === ConstVal.medelegAddr

  protected val mcycleVal   = Mux(csrVis && mcycleVis, mcycle, 0.U)
  protected val mstatusVal  = Mux(csrVis && mstatusVis, mstatus, 0.U)
  protected val mtvecVal    = Mux(csrVis && mtvecVis, mtvec, 0.U)
  protected val mcauseVal   = Mux(csrVis && mcauseVis, mcause, 0.U)
  protected val mepcVal     = Mux(csrVis && mepcVis, mepc, 0.U)
  protected val mieVal      = Mux(csrVis && mieVis, mie, 0.U)
  protected val mipVal      = Mux(csrVis && mipVis, mip, 0.U)
  protected val mscratchVal = Mux(csrVis && mscratchVis, mscratch, 0.U)
  protected val medelegVal  = Mux(csrVis && medelegVis, medeleg, 0.U)
  protected val mhartidVal  = Mux(csrVis && mhartidVis, mhartid, 0.U)
  protected val rdVal = mcycleVal | mstatusVal | mtvecVal | mcauseVal | mepcVal | mieVal | mipVal |
    mscratchVal | medelegVal | mhartidVal

  protected val MIE        = mstatus(3)
  protected val MPIE       = mstatus(7)
  protected val MPP        = mstatus(12, 11)
  protected val MTIE       = mie(7)
  protected val MTIP       = mip(7)
  protected val TIV        = MIE && (MTIE && MTIP)
  protected val TIVR       = RegEnable(TIV, false.B, io.globalEn)
  protected val timeIntrEn = TIV && ~TIVR
  protected val ecallEn    = ecallVis
  io.timeIntrEn := timeIntrEn
  io.ecallEn    := ecallEn

  protected val rcData  = SignExt(csrrcVis.asUInt, 64) & (rdVal & ~io.src)
  protected val rciData = SignExt(csrrciVis.asUInt, 64) & (rdVal & ~zimm)
  protected val rsData  = SignExt(csrrsVis.asUInt, 64) & (rdVal | io.src)
  protected val rsiData = SignExt(csrrsiVis.asUInt, 64) & (rdVal | zimm)
  protected val rwData  = SignExt(csrrwVis.asUInt, 64) & (io.src)
  protected val rwiData = SignExt(csrrwiVis.asUInt, 64) & (zimm)
  protected val wData   = rcData | rciData | rsData | rsiData | rwData | rwiData

  protected val SD         = wData(16, 15) === 3.U || wData(14, 13) === 3.U
  protected val nop3       = 0.U(3.W)
  protected val trapStatus = Cat(mstatus(63, 13), 3.U(2.W), nop3, MIE, nop3, 0.U(1.W), nop3)
  protected val mretStatus = Cat(mstatus(63, 13), 0.U(2.W), nop3, 1.U(1.W), nop3, MPIE, nop3)

  when(csrVis && mcycleVis) {
    mcycle := wData
  }.otherwise {
    mcycle := mcycle + 1.U
  }

  when(io.globalEn) {
    when(timeIntrEn || ecallEn) {
      mstatus := trapStatus
    }.elsewhen(mretVis) {
      mstatus := mretStatus
    }.elsewhen(csrVis && mstatusVis) {
      mstatus := Cat(SD.asUInt, wData(62, 0))
    }

    when(csrVis && mtvecVis) { mtvec := wData }
    when(csrVis && mieVis) { mie := wData }
    when(csrVis && mipVis) {
      mip := wData
    }.otherwise {
      mip := Mux(io.mtip, "h0000_0000_0000_0080".U, 0.U)
    }

    when(timeIntrEn) {
      mcause := "h8000_0000_0000_0007".U(64.W)
    }.elsewhen(ecallEn) {
      mcause := "h0000_0000_0000_000b".U(64.W)
    }.elsewhen(csrVis && mcauseVis) {
      mcause := wData
    }

    when(timeIntrEn || ecallEn) {
      mepc := io.pc
    }.elsewhen(csrVis && mepcVis) {
      mepc := wData
    }

    when(csrVis && mscratchVis) { mscratch := wData }
    when(csrVis && medelegVis) { medeleg := wData }
    when(csrVis && mhartidVis) { mhartid := wData }
  }

  io.data := rdVal

  io.csrState.clock          := clock
  io.csrState.coreid         := 0.U
  io.csrState.mstatus        := mstatus
  io.csrState.mcause         := mcause
  io.csrState.mepc           := mepc
  io.csrState.sstatus        := mstatus & "h8000_0003_000d_e122".U
  io.csrState.scause         := 0.U
  io.csrState.sepc           := 0.U
  io.csrState.satp           := 0.U
  io.csrState.mip            := 0.U
  io.csrState.mie            := mie
  io.csrState.mscratch       := mscratch
  io.csrState.sscratch       := 0.U
  io.csrState.mideleg        := 0.U
  io.csrState.medeleg        := medeleg
  io.csrState.mtval          := 0.U
  io.csrState.stval          := 0.U
  io.csrState.mtvec          := mtvec
  io.csrState.stvec          := 0.U
  io.csrState.priviledgeMode := 3.U
}
