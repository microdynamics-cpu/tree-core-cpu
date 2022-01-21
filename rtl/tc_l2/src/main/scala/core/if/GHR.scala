package treecorel2

import chisel3._
import chisel3.util._

class GHR extends Module with InstConfig {
  val io = IO(new Bundle {
    val branch = Input(Bool())
    val taken  = Input(Bool())
    val idx    = Output(UInt(GHRLen.W))
  })

  protected val shiftReg = Reg(UInt(GHRLen.W))

  when(io.branch) {
    shiftReg := Cat(shiftReg(GHRLen - 2, 0), io.taken)
  }

  io.idx := shiftReg
}
