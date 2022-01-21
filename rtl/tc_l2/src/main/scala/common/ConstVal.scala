package treecorel2

import chisel3._
import chisel3.util.log2Ceil

object ConstVal {
  // addr width
  val AddrLen      = 64
  val AddrAlignLen = log2Ceil(AddrLen / 8)
}
