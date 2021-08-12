package treecorel2.common

import chisel3._
import chisel3.util.{Cat, Fill}

object getMSBValue {
  def apply(data: UInt): Bool = data(data.getWidth - 1).asBool()
}

object getSignExtn {
  def apply(bitWidth: Int, data: UInt): UInt = {
    if (bitWidth - data.getWidth == 0) {
      data
    } else {
      Cat(Fill(bitWidth - data.getWidth, getMSBValue(data).asUInt), data)
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
