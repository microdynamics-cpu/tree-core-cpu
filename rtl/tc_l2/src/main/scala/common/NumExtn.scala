package treecorel2.common

import chisel3._
import chisel3.util._

// be careful to call this func, need to set the right msb val!!!
// to minify the wire unused pin and bits in verilator
object getSignExtn {
  def apply(bitWidth: Int, data: UInt, msb: UInt): UInt = {
    if (bitWidth - data.getWidth == 0) {
      data
    } else {
      Cat(Fill(bitWidth - data.getWidth, msb), data)
    }
  }
}

object getZeroExtn {
  def apply(bitWidth: Int, data: UInt): UInt = {
    if (bitWidth - data.getWidth == 0) {
      data
    } else {
      Cat(Fill(bitWidth - data.getWidth, 0.U(1.W)), data)
    }
  }
}
