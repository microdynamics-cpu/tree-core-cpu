package treecorel2

import chisel3._

class AXI4Intcon extends Module with InstConfig {
  val io = IO(new Bundle {
    val inst: AXI4IO = Flipped(new AXI4IO)
    val mem:  AXI4IO = Flipped(new AXI4IO)
    val out:  AXI4IO = new AXI4IO
  })

  // only mem can write
  io.out.aw  <> io.mem.aw
  io.out.w   <> io.mem.w
  io.out.b   <> io.mem.b
  io.inst.aw := DontCare
  io.inst.w  := DontCare
  io.inst.b  := DontCare

  // mem rd has higher priority
  when(io.mem.ar.valid) {
    io.inst.r.valid  := false.B
    io.inst.r.resp   := 0.U
    io.inst.r.data   := 0.U
    io.inst.r.last   := false.B
    io.inst.r.id     := 0.U
    io.inst.r.user   := 0.U
    io.inst.ar.ready := false.B
    io.out.ar        <> io.mem.ar
    io.out.r         <> io.mem.r
  }.otherwise {
    io.mem.r.valid  := false.B
    io.mem.r.resp   := 0.U
    io.mem.r.data   := 0.U
    io.mem.r.last   := false.B
    io.mem.r.id     := 0.U
    io.mem.r.user   := 0.U
    io.mem.ar.ready := false.B
    io.out.ar       <> io.inst.ar
    io.out.r        <> io.inst.r
  }
}
