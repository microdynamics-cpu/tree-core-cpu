package treecorel2

import chisel3._
import chisel3.util._

class MEM2WBIO extends EX2MEMIO with IOConfig {
  val ldData = Output(UInt(XLen.W))
  val cvalid = Output(Bool())
}
