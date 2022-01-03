package treecorel2

import chisel3._
import chisel3.util._

class ID2EXIO extends IF2IDIO {
  val isa   = Output(new ISAIO)
  val src1  = Output(UInt(64.W))
  val src2  = Output(UInt(64.W))
  val imm   = Output(new IMMIO)
  val wen   = Output(Bool())
  val wdest = Output(UInt(5.W))
}