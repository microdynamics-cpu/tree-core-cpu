package treecorel2

import chisel3._
import chisel3.util._

class SDIO extends Bundle {
  val en   = Output(Bool())
  val addr = Output(UInt(64.W))
  val data = Output(UInt(64.W))
  val mask = Output(UInt(8.W))
}
