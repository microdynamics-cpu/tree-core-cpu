package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.InstConfig

class IF2IDIO extends Bundle {
  val valid     = Output(Bool())
  val inst      = Output(UInt(32.W))
  val pc        = Output(UInt(64.W))
  val branIdx   = Output(UInt(5.W)) // (GHRLen)
  val predTaken = Output(Bool())
}
