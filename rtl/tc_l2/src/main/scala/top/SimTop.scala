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

  protected val proc            = Module(new Processor)
  protected val axiBridge       = Module(new AXI4Bridge)
  protected val instComm        = Module(new DifftestInstrCommit)
  protected val archIntRegState = Module(new DifftestArchIntRegState)
  protected val csrState        = Module(new DifftestCSRState)
  protected val trapEvt         = Module(new DifftestTrapEvent)
  protected val archFpRegState  = Module(new DifftestArchFpRegState)
  protected val archEvt         = Module(new DifftestArchEvent)

  io.uart.in.valid  := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch    := 0.U

  proc.io.runEn      <> axiBridge.io.runEn
  proc.io.socEn      := false.B
  proc.io.dxchg      <> axiBridge.io.dxchg
  axiBridge.io.socEn := false.B

  io.memAXI_0 <> axiBridge.io.axi

  proc.io.instComm        <> instComm.io
  proc.io.archIntRegState <> archIntRegState.io
  proc.io.csrState        <> csrState.io
  proc.io.trapEvt         <> trapEvt.io
  proc.io.archFpRegState  <> archFpRegState.io
  proc.io.archEvt         <> archEvt.io
}
