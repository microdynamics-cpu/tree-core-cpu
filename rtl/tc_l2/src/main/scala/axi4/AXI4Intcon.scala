package treecorel2

import chisel3._

class AXI4Intcon extends Module with InstConfig {
  val io = IO(new Bundle {
    val inst: AXI4IO = new Flipped(AXI4IO)
    val mem:  AXI4IO = new Flipped(AXI4IO)
    val out:  AXI4IO = new AXI4IO
  })

  // only mem can write
  io.out.aw <> io.mem.aw
  io.out.wt <> io.mem.wt
  io.out.b  <> io.mem.b

  // mem rd has higher priority
  when(io.mem.ar.ready) {
    io.out.ar <> io.mem.ar
    io.out.rd <> io.mem.rd
  }.otherwise {
    io.out.ar <> io.inst.ar
    io.out.rd <> io.inst.rd
  }
}
