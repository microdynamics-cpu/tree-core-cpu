package treecorel2

import chisel3._
import chisel3.util._

class BRANCHIO extends Bundle with IOConfig {
  val branch = Output(Bool()) // prev inst is a b/j
  val jump   = Output(Bool()) // is 'jal' or 'jalr'
  val taken  = Output(Bool()) // is prev branch taken
  val idx    = Output(UInt(GHRLen.W)) // prev idx of PHT(GHRLen)
  val pc     = Output(UInt(XLen.W)) // prev instruction PC
  val tgt    = Output(UInt(XLen.W)) // prev branch tgt
}
