package treecorel2

import chisel3._
import chisel3.util._
import difftest._

class CSRReg extends Module with InstConfig {
  val io = IO(new Bundle {
    val globalEn   = Input(Bool())
    val pc         = Input(UInt(XLen.W))
    val inst       = Input(UInt(XLen.W))
    val src        = Input(UInt(XLen.W))
    val data       = Output(UInt(XLen.W))
    val mtip       = Input(Bool())
    val timeIntrEn = Output(Bool())
    val ecallEn    = Output(Bool())
    val csrState   = Flipped(new DiffCSRStateIO)
  })

  protected val mcycle   = RegInit(0.U(XLen.W))
  protected val mstatus  = RegInit(0.U(XLen.W))
  protected val mtvec    = RegInit(0.U(XLen.W))
  protected val mcause   = RegInit(0.U(XLen.W))
  protected val mepc     = RegInit(0.U(XLen.W))
  protected val mie      = RegInit(0.U(XLen.W))
  protected val mip      = RegInit(0.U(XLen.W))
  protected val mscratch = RegInit(0.U(XLen.W))
  protected val medeleg  = RegInit(0.U(XLen.W))
  protected val mhartid  = RegInit(0.U(XLen.W))

  protected val csrVis = MuxLookup(
    io.inst,
    false.B,
    Seq(
      instCSRRW  -> true.B,
      instCSRRWI -> true.B,
      instCSRRS  -> true.B,
      instCSRRSI -> true.B,
      instCSRRC  -> true.B,
      instCSRRCI -> true.B
    )
  )

  protected val zimm        = ZeroExt(io.inst(19, 15), XLen)
  protected val addr        = io.inst(31, 20)
  protected val mretVis     = io.inst === instMRET
  protected val ecallVis    = io.inst === instECALL
  protected val mhartidVis  = addr    === mhartidAddr
  protected val mstatusVis  = addr    === mstatusAddr
  protected val mieVis      = addr    === mieAddr
  protected val mtvecVis    = addr    === mtvecAddr
  protected val mscratchVis = addr    === mscratchAddr
  protected val mepcVis     = addr    === mepcAddr
  protected val mcauseVis   = addr    === mcauseAddr
  protected val mipVis      = addr    === mipAddr
  protected val mcycleVis   = addr    === mcycleAddr
  protected val medelegVis  = addr    === medelegAddr

  protected val rdVal = MuxLookup(
    addr,
    0.U(XLen.W),
    Seq(
      mhartidAddr  -> Mux(csrVis, mhartid, 0.U),
      mstatusAddr  -> Mux(csrVis, mstatus, 0.U),
      mieAddr      -> Mux(csrVis, mie, 0.U),
      mtvecAddr    -> Mux(csrVis, mtvec, 0.U),
      mscratchAddr -> Mux(csrVis, mscratch, 0.U),
      mepcAddr     -> Mux(csrVis, mepc, 0.U),
      mcauseAddr   -> Mux(csrVis, mcause, 0.U),
      mipAddr      -> Mux(csrVis, mip, 0.U),
      mcycleAddr   -> Mux(csrVis, mcycle, 0.U),
      medelegAddr  -> Mux(csrVis, medeleg, 0.U)
    )
  )

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

  protected val wData = MuxLookup(
    io.inst,
    0.U(XLen.W),
    Seq(
      instCSRRC  -> (rdVal & ~io.src),
      instCSRRCI -> (rdVal & ~zimm),
      instCSRRS  -> (rdVal | io.src),
      instCSRRSI -> (rdVal | zimm),
      instCSRRW  -> (io.src),
      instCSRRWI -> (zimm)
    )
  )

  protected val sdBits     = wData(16, 15) === 3.U || wData(14, 13) === 3.U
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
      mstatus := Cat(sdBits.asUInt, wData(62, 0))
    }

    when(csrVis && mtvecVis) { mtvec := wData }
    when(csrVis && mieVis) { mie := wData }
    when(csrVis && mipVis) {
      mip := wData
    }.otherwise {
      mip := Mux(io.mtip, "h0000_0000_0000_0080".U, 0.U)
    }

    when(timeIntrEn) {
      mcause := timeCause
    }.elsewhen(ecallEn) {
      mcause := ecallCause
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
