package sim

import chisel3._
import chisel3.util._

import difftest._
import treecorel2._

class SimTop extends Module {
  val io = IO(new Bundle {
    val logCtrl  = new LogCtrlIO
    val perfInfo = new PerfInfoIO
    val uart     = new UARTIO
    val memAXI_0 = new AXI4IO
  })

  protected val proc      = Module(new Processor)
  protected val axiBridge = Module(new AXI4Bridge)

  io.uart.in.valid  := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch    := 0.U

  proc.io.runEn      <> axiBridge.io.runEn
  proc.io.dxchg      <> axiBridge.io.dxchg
  proc.io.socEn      := false.B
  axiBridge.io.socEn := false.B

  io.memAXI_0 <> axiBridge.io.axi
}
