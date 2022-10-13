package treecorel2

import chisel3._
import chisel3.util._

class IFU extends Module with InstConfig {
  val io = IO(new Bundle {
    val globalEn   = Input(Bool())
    val stall      = Input(Bool())
    val socEn      = Input(Bool())
    val branchInfo = Flipped(new BRANCHIO)
    val nxtPC      = Flipped(new NXTPCIO)
    val fetch      = new IFIO
    val if2id      = new IF2IDIO
  })

  protected val startAddr = Mux(io.socEn, FlashStartAddr, SimStartAddr)
  protected val pc        = RegInit(startAddr)
  protected val valid     = Mux(reset.asBool(), false.B, true.B)

  protected val bpu = Module(new BPU)
  bpu.io.branchInfo <> io.branchInfo
  bpu.io.lookupPc   := pc

  when(io.globalEn) {
    when(io.nxtPC.trap) {
      pc := io.nxtPC.mtvec
    }.elsewhen(io.nxtPC.mret) {
      pc := io.nxtPC.mepc
    }.elsewhen(io.nxtPC.branch) {
      pc := io.nxtPC.tgt
    }.elsewhen(bpu.io.predTaken) {
      pc := bpu.io.predTgt
    }.otherwise {
      pc := pc + 4.U
    }
  }

  io.if2id.valid     := Mux(io.stall, false.B, valid)
  io.if2id.inst      := io.fetch.data
  io.if2id.pc        := pc
  io.if2id.branIdx   := bpu.io.predIdx
  io.if2id.predTaken := bpu.io.predTaken
  // comm with crossbar to get inst back
  io.fetch.en   := valid
  io.fetch.addr := pc
}
