package treecorel2

import chisel3._
import chisel3.util._

class LDIO extends Bundle {
  val en   = Output(Bool())
  val addr = Output(UInt(64.W))
  val data = Input(UInt(64.W))
  val size = Output(UInt(3.W))
}
