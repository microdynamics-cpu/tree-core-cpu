package treecorel2

import chisel3._
import chisel3.util._

class WBDATAIO extends Bundle with IOConfig {
  val wen   = Output(Bool())
  val wdest = Output(UInt(RegfileLen.W))
  val data  = Output(UInt(XLen.W))
}
