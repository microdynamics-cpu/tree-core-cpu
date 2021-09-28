package treecorel2

import chisel3._

class RAMHelper extends BlackBox with InstConfig {
  val io = IO(new Bundle {
    val clk:   Clock = Input(Clock())
    val en:    Bool  = Input(Bool())
    val rIdx:  UInt  = Input(UInt(BusWidth.W))
    val rdata: UInt  = Output(UInt(BusWidth.W))
    val wIdx:  UInt  = Input(UInt(BusWidth.W))
    val wdata: UInt  = Input(UInt(BusWidth.W))
    val wmask: UInt  = Input(UInt(BusWidth.W))
    val wen:   Bool  = Input(Bool())
  })
}
