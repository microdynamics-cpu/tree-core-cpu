package treecorel2

import chisel3._
import chisel3.util._

class NXTPCIO extends Bundle {
  val trap   = Output(Bool())
  val mtvec  = Output(UInt(64.W))
  val mret   = Output(Bool())
  val mepc   = Output(UInt(64.W))
  val branch = Output(Bool())
  val tgt    = Output(UInt(64.W))
}
