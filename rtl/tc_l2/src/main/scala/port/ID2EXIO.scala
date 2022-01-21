package treecorel2

import chisel3._
import chisel3.util._

class ID2EXIO extends IF2IDIO {
  val isa   = Output(UInt(6.W))
  val src1  = Output(UInt(64.W))
  val src2  = Output(UInt(64.W))
  val imm   = Output(UInt(64.W))
  val wen   = Output(Bool())
  val wdest = Output(UInt(5.W))
}
