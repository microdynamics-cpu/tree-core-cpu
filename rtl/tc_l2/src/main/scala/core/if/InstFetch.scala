package treecorel2

import chisel3._
import chisel3.util._

class InstFetch extends Module {
  val io = IO(new Bundle {
    val globalEn = Input(Bool())
    val stall    = Input(Bool())
    val socEn    = Input(Bool())
    val fetch    = new IFIO
    val if2id    = new IF2IDIO
    val nxtPC    = Flipped(new NXTPCIO)
  })

  protected val startAddr = Mux(io.socEn, "h0000000030000000".U(64.W), "h0000000080000000".U(64.W))
  protected val valid     = Mux(reset.asBool(), false.B, true.B)
  protected val inst      = io.fetch.data
  protected val pc        = RegInit(startAddr)

  when(io.globalEn) {
    when(io.nxtPC.trap) {
      pc := io.nxtPC.mtvec
    }.elsewhen(io.nxtPC.mret) {
      pc := io.nxtPC.mepc
    }.elsewhen(io.nxtPC.branch) {
      pc := io.nxtPC.tgt
    }.otherwise {
      pc := pc + 4.U
    }
  }

  io.if2id.valid := Mux(io.stall, false.B, valid)
  io.if2id.inst  := inst
  io.if2id.pc    := pc

  // comm with crossbar to get inst back
  io.fetch.en   := valid
  io.fetch.addr := pc
}
