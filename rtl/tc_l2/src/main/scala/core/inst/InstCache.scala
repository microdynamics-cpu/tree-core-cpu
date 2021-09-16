package treecorel2

import chisel3._
import chisel3.util._

class InstCache extends Module with InstConfig {
  val io = IO(new Bundle {
    val instAddrIn:  UInt = Input(UInt(BusWidth.W))
    val instEnaIn:   Bool = Input(Bool())
    val instDataOut: UInt = Output(UInt(BusWidth.W))
  })
}
