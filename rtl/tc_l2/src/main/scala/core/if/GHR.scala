package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.ConstVal

class GHR extends Module {
  val io = IO(new Bundle {
    val branch = Input(Bool())
    val taken  = Input(Bool())
    val idx    = Output(UInt(ConstVal.GHRLen.W))
  })

  protected val shiftReg = Reg(UInt(ConstVal.GHRLen.W))

  when(io.branch) {
    shiftReg := Cat(shiftReg(ConstVal.GHRLen - 2, 0), io.taken)
  }

  io.idx := shiftReg
}