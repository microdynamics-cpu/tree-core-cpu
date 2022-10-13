package treecorel2

import chisel3._
import chisel3.util._

class MulDivIO(val len: Int) extends Bundle {
  val in  = Flipped(DecoupledIO(Vec(2, Output(UInt(len.W)))))
  val res = DecoupledIO(Output(UInt((len * 2).W)))
}
