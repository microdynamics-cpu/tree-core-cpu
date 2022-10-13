package treecorel2

import chisel3._
import chisel3.util._

class NXTPCIO extends Bundle with IOConfig {
  val trap   = Output(Bool())
  val mtvec  = Output(UInt(XLen.W))
  val mret   = Output(Bool())
  val mepc   = Output(UInt(XLen.W))
  val branch = Output(Bool())
  val tgt    = Output(UInt(XLen.W))
}
