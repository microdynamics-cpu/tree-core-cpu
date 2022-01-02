package treecorel2

import chisel3._
import chisel3.util._

class ImmExten extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val imm  = Output(new IMMIO)
  })

  protected val I = io.inst(31, 20)
  protected val B = Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W))
  protected val S = Cat(io.inst(31, 25), io.inst(11, 7))
  protected val U = Cat(io.inst(31, 12), 0.U(12.W))
  protected val J = Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W))
  protected val Z = io.inst(19, 15)
  io.imm.I := SignExt(I, 64)
  io.imm.B := SignExt(B, 64)
  io.imm.S := SignExt(S, 64)
  io.imm.U := SignExt(U, 64)
  io.imm.J := SignExt(J, 64)
  io.imm.Z := ZeroExt(Z, 64)
}
