package treecorel2

import chisel3._
import chisel3.util._

class IF2IDIO extends Bundle with IOConfig {
  val valid     = Output(Bool())
  val inst      = Output(UInt(InstLen.W))
  val pc        = Output(UInt(XLen.W))
  val branIdx   = Output(UInt(GHRLen.W))
  val predTaken = Output(Bool())
}
