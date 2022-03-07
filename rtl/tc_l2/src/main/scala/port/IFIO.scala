package treecorel2

import chisel3._
import chisel3.util._

class IFIO extends Bundle with IOConfig {
  val en   = Output(Bool())
  val addr = Output(UInt(XLen.W))
  val data = Input(UInt(InstLen.W))
}
