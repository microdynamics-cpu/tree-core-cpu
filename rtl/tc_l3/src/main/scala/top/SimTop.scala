package sim

import chisel3._
import chisel3.util._

import difftest._
import treecorel3._

class SimTop extends Module {
  val io = IO(new Bundle {
    val logCtrl  = new LogCtrlIO
    val perfInfo = new PerfInfoIO
    val uart     = new UARTIO
    val memAXI_0 = new AxiIO
  })

  protected val proc = Module(new Processor)

  io.uart.in.valid  := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch    := 0.U

  io.memAXI_0 <> proc.io.axi
}
