package treecorel2

import chisel3._
import chisel3.util._

class csr extends Bundle {
  val mcycle   = UInt(64.W)
  val mstatus  = UInt(64.W)
  val mtvec    = UInt(64.W)
  val mcause   = UInt(64.W)
  val mepc     = UInt(64.W)
  val mie      = UInt(64.W)
  val mip      = UInt(64.W)
  val mscratch = UInt(64.W)
  val medeleg  = UInt(64.W)
  val mhartid  = UInt(64.W)
}
