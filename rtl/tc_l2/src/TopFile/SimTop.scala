package sim.difftest

import treecorel2._
import difftest._

class SimTop extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val logCtrl  = new LogCtrlIO
    val perfInfo = new PerfInfoIO
    val uart     = new UARTIO
  })

  protected val treeCoreL2 = Module(new TreeCoreL2())
  protected val instRam: RAMHelper = Module(new RAMHelper())
  protected val dataRam: RAMHelper = Module(new RAMHelper())

  instRam.io.clk       := this.clock
  instRam.io.en        := !this.reset.asBool() && treeCoreL2.io.instEnaOut
  instRam.io.rIdx      := (treeCoreL2.io.instAddrOut - PcRegStartAddr.U) >> 3
  instRam.io.wIdx      := DontCare
  instRam.io.wen       := DontCare
  instRam.io.wdata     := DontCare
  instRam.io.wmask     := DontCare
  treeCoreL2.io.instDataIn   := Mux(treeCoreL2.io.instAddrOut(2), instRam.io.rdata(63, 32), instRam.io.rdata(31, 0))
  
  dataRam.io.clk       := this.clock
  dataRam.io.en        := !this.reset.asBool() && treeCoreL2.io.memEnaOut
  dataRam.io.rIdx      := (treeCoreL2.io.memAddrOut - PcRegStartAddr.U) >> 3
  treeCoreL2.io.memRDataIn := dataRam.io.rdata
  dataRam.io.wen       := treeCoreL2.io.memWtEnaOut
  dataRam.io.wIdx      := (treeCoreL2.io.memAddrOut - PcRegStartAddr.U) >> 3
  dataRam.io.wdata     := treeCoreL2.io.memWtDataOut
  dataRam.io.wmask     := treeCoreL2.io.memMaskOut

  io.uart.in.valid  := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch    := 0.U
}