package treecorel2

import chisel3._
import chisel3.util._

class ID2EXIO extends IF2IDIO with IOConfig {
  val isa   = Output(UInt(ISALen.W))
  val src1  = Output(UInt(XLen.W))
  val src2  = Output(UInt(XLen.W))
  val imm   = Output(UInt(XLen.W))
  val wen   = Output(Bool())
  val wdest = Output(UInt(RegfileLen.W))
}
