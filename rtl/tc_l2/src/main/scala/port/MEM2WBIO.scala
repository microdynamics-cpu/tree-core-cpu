package treecorel2

import chisel3._
import chisel3.util._

class MEM2WBIO extends EX2MEMIO {
  val loadData = Output(UInt(64.W))
  val cvalid   = Output(Bool())
}
