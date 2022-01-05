package sim

import chisel3._
import chisel3.util._
import treecorel2._

class SoCTop extends Module {
  val io = IO(new Bundle {
    val interrupt = Input(Bool())
    val master    = new SOCAXI4IO
    val slave     = Flipped(new SOCAXI4IO)
  })

  val proc      = Module(new Processor)
  val axiBridge = Module(new AXI4Bridge)
  proc.io.runEn      <> axiBridge.io.runEn
  proc.io.dxchg      <> axiBridge.io.dxchg
  proc.io.socEn      := true.B
  axiBridge.io.socEn := true.B

  io.master <> axiBridge.io.axi

  io.slave.aw.ready    := false.B
  io.slave.w.ready     := false.B
  io.slave.b.valid     := false.B
  io.slave.b.bits.resp := 0.U
  io.slave.b.bits.id   := 0.U
  io.slave.ar.ready    := false.B
  io.slave.r.valid     := false.B
  io.slave.r.bits.resp := 0.U
  io.slave.r.bits.data := 0.U
  io.slave.r.bits.last := false.B
  io.slave.r.bits.id   := 0.U
}
