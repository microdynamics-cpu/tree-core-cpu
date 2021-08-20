package treecorel2

import chisel3._

class CSRReg extends Module with InstConfig {
  val io = IO(new Bundle {
    val cycleOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val cycleReg: UInt = RegInit(0.U(BusWidth.W))
  cycleReg := cycleReg + 1.U
}
