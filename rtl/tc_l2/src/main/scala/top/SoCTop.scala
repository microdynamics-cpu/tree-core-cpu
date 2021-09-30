package treecorel2

import chisel3._
import treecorel2._
import difftest._

class SoCTop(val ifDiffTest: Boolean, val ifSoC: Boolean) extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    val interrupt = Input(Bool())
    val master    = new AXI4IO
    val slave     = Flipped(new AXI4IO)
    // becuase the framework, now the 'master_w_bits_data' need to be replaced
    // by 'master_w_bits_data[3:0]' in Makefile
    // becuase the framework, now the 'master_r_bits_data' need to be replaced
    // by 'master_r_bits_data[3:0]' in Makefile
  })

  protected val axiBridge: AXI4Bridge = Module(new AXI4Bridge(ifSoC))
  io.master <> axiBridge.io.axi

  protected val treeCoreL2 = Module(new TreeCoreL2(ifDiffTest, ifSoC))
  treeCoreL2.io.uart.in.ch := DontCare
  axiBridge.io.inst        <> treeCoreL2.io.inst
  axiBridge.io.mem         <> treeCoreL2.io.mem
  io.slave.aw.ready        := false.B
  io.slave.w.ready         := false.B
  io.slave.b.valid         := false.B
  io.slave.b.bits.resp     := 0.U
  io.slave.b.bits.id       := 0.U
  io.slave.b.bits.user     := 0.U
  io.slave.ar.ready        := false.B
  io.slave.r.valid         := false.B
  io.slave.r.bits.resp     := 0.U
  io.slave.r.bits.data     := 0.U
  io.slave.r.bits.last     := false.B
  io.slave.r.bits.id       := 0.U
  io.slave.r.bits.user     := 0.U
  if (ifDiffTest) {} else {}
}
