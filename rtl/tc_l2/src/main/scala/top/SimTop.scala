package treecorel2

import chisel3._
import treecorel2._
import difftest._

class SimTop() extends Module with AXI4Config {
  val io = IO(new Bundle {
    val logCtrl  = new LogCtrlIO
    val perfInfo = new PerfInfoIO
    val uart     = new UARTIO
    val memAXI_0 = new AXI4IO
  })

  protected val axiBridge: AXI4Bridge = Module(new AXI4Bridge())
  io.memAXI_0 <> axiBridge.io.axi

  protected val treeCoreL2 = Module(new TreeCoreL2())
  axiBridge.io.inst <> treeCoreL2.io.inst
  axiBridge.io.mem  <> treeCoreL2.io.mem
  if (DiffEna) {
    io.uart <> treeCoreL2.io.uart
  } else {
    io.uart <> DontCare
  }
}
