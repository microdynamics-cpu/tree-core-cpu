package treecorel2

import chisel3._
import treecorel2._
import difftest._

class SoCTop() extends Module with AXI4Config {
  val io = IO(new Bundle {
    val interrupt = Input(Bool())
    val master    = new SOCAXI4IO
    val slave     = Flipped(new SOCAXI4IO)
  })

  protected val axiBridge: AXI4Bridge = Module(new AXI4Bridge())
  io.master <> axiBridge.io.axi

  protected val treeCoreL2 = Module(new TreeCoreL2())
  treeCoreL2.io.uart.in.ch := DontCare
  axiBridge.io.inst        <> treeCoreL2.io.inst
  axiBridge.io.mem         <> treeCoreL2.io.mem
  io.slave.aw.ready        := false.B
  io.slave.w.ready         := false.B
  io.slave.b.valid         := false.B
  io.slave.b.bits.resp     := 0.U
  io.slave.b.bits.id       := 0.U
  io.slave.ar.ready        := false.B
  io.slave.r.valid         := false.B
  io.slave.r.bits.resp     := 0.U
  io.slave.r.bits.data     := 0.U
  io.slave.r.bits.last     := false.B
  io.slave.r.bits.id       := 0.U
}
