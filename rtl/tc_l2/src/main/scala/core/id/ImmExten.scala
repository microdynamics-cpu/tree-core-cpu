package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.InstConfig

class ImmExten extends Module with InstConfig {
  val io = IO(new Bundle {
    val inst = Input(UInt(InstLen.W))
    val imm  = Output(new IMMIO)
  })

  protected val I = io.inst(31, 20)
  protected val S = Cat(io.inst(31, 25), io.inst(11, 7))
  protected val B = Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W))
  protected val U = Cat(io.inst(31, 12), 0.U(12.W))
  protected val J = Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W))

  io.imm.I := SignExt(I, XLen)
  io.imm.S := SignExt(S, XLen)
  io.imm.B := SignExt(B, XLen)
  io.imm.U := SignExt(U, XLen)
  io.imm.J := SignExt(J, XLen)
}
