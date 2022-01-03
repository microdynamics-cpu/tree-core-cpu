package treecorel2

import chisel3._
import chisel3.util._

class IFIO extends Bundle {
  val en   = Output(Bool())
  val addr = Output(UInt(64.W))
  val data = Input(UInt(32.W))
}
