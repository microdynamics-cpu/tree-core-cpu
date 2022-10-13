package treecorel2

import chisel3._
import chisel3.util._

class CLINT extends Module with InstConfig {
  val io = IO(new Bundle {
    val valid  = Input(Bool())
    val mtip   = Output(Bool())
    val cvalid = Output(Bool())
    val cld    = Flipped(new LDIO)
    val csd    = Flipped(new SDIO)
    val ld     = new LDIO
    val sd     = new SDIO
  })

  // now only use the 32bit range addr
  protected val addr        = Mux(io.cld.en, io.cld.addr(31, 0), 0.U) | Mux(io.csd.en, io.csd.addr(31, 0), 0.U)
  protected val wdata       = io.csd.data
  protected val mtimeVis    = addr === ClintBaseAddr + MTimeOffset
  protected val mtimecmpVis = addr === ClintBaseAddr + MTimeCmpOffset

  // check if a mmio access
  protected val cren   = io.cld.en && (mtimecmpVis || mtimeVis) && io.valid
  protected val cwen   = io.csd.en && (mtimecmpVis || mtimeVis) && io.valid
  protected val cvalid = cren      || cwen

  // generate low speed clock
  protected val (tickCnt, cntWrap) = Counter(true.B, 5)
  protected val mtime              = RegInit(0.U(XLen.W))
  protected val mtimecmp           = RegInit(0.U(XLen.W))

  when(cwen && mtimeVis) {
    mtime := wdata
  }.otherwise {
    mtime := Mux(cntWrap, mtime + 1.U, mtime)
  }

  when(cwen && mtimecmpVis) { mtimecmp := wdata }

  protected val mtimeVal    = Mux(cren && mtimeVis, mtime, 0.U)
  protected val mtimecmpVal = Mux(cren && mtimecmpVis, mtimecmp, 0.U)
  protected val rdata       = mtimeVal | mtimecmpVal

  // stall or bypass the sig
  io.ld.en    := Mux(cvalid, false.B, io.cld.en)
  io.ld.addr  := io.cld.addr
  io.ld.size  := io.cld.size
  io.cld.data := Mux(cvalid, rdata, io.ld.data)

  io.sd.en   := Mux(cvalid, false.B, io.csd.en)
  io.sd.addr := io.csd.addr
  io.sd.data := io.csd.data
  io.sd.mask := io.csd.mask

  io.mtip   := mtime >= mtimecmp
  io.cvalid := cvalid
}
