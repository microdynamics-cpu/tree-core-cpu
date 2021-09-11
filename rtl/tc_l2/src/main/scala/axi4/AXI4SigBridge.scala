package treecorel2

import chisel3._

class AXI4SigBridge extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    val rw:  AXI4USERIO = new AXI4USERIO
    val axi: AXI4IO     = new AXI4IO
  })

  io.rw  := DontCare
  io.axi := DontCare
}
