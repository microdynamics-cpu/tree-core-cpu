package treecorel2

import chisel3._
import chisel3.util._

// be careful to call this func, need to set the right msb val!!!
// to minify the wire unused pin and bits in verilator
object SignExt {
  def apply(a: UInt, len: Int) = {
    val aLen    = a.getWidth
    val signBit = a(aLen - 1)
    if (aLen >= len) a(len - 1, 0) else Cat(Fill(len - aLen, signBit), a)
  }
}

object ZeroExt {
  def apply(a: UInt, len: Int) = {
    val aLen = a.getWidth
    if (aLen >= len) a(len - 1, 0) else Cat(0.U((len - aLen).W), a)
  }
}
