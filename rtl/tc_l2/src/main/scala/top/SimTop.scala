package treecorel2

import chisel3._
import treecorel2._
import difftest._

class SimTop(val ifDiffTest: Boolean, val ifSoC: Boolean) extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    val logCtrl  = new LogCtrlIO
    val perfInfo = new PerfInfoIO
    val uart     = new UARTIO
    val memAXI_0 = new AXI4IO
    // becuase the framework, now the 'memAXI_0_w_bits_data' need to be replaced
    // by 'memAXI_0_w_bits_data[3:0]' in Makefile
    // becuase the framework, now the 'memAXI_0_r_bits_data' need to be replaced
    // by 'memAXI_0_r_bits_data[3:0]' in Makefile
  })

  protected val axiBridge: AXI4Bridge = Module(new AXI4Bridge)
  io.memAXI_0 <> axiBridge.io.axi

  protected val treeCoreL2 = Module(new TreeCoreL2(ifDiffTest, ifSoC))
  axiBridge.io.inst <> treeCoreL2.io.inst
  axiBridge.io.mem  <> treeCoreL2.io.mem
  io.uart           <> treeCoreL2.io.uart
}
