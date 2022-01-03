package treecorel2

import chisel3._
import chisel3.util._

class WBDATAIO extends Bundle {
  val wen   = Output(Bool())
  val wdest = Output(UInt(5.W))
  val data  = Output(UInt(64.W))
}
