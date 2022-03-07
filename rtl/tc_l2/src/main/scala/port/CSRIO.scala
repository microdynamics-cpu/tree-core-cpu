package treecorel2

import chisel3._
import chisel3.util._

class csr extends Bundle with IOConfig {
  val mcycle   = UInt(XLen.W)
  val mstatus  = UInt(XLen.W)
  val mtvec    = UInt(XLen.W)
  val mcause   = UInt(XLen.W)
  val mepc     = UInt(XLen.W)
  val mie      = UInt(XLen.W)
  val mip      = UInt(XLen.W)
  val mscratch = UInt(XLen.W)
  val medeleg  = UInt(XLen.W)
  val mhartid  = UInt(XLen.W)
}
