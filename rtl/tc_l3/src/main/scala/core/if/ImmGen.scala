package treecorel3

import chisel._
import chiesl.uitl._

class ImmGenIO extends Bundle with IOConfig {
  val inst = Input(UInt(XLen.W))
  val sel  = Input(UInt(3.W))
  val out  = Output(UInt(XLen.W))
}

class ImmGen extends Module {
  val io             = IO(new ImmGenIO)
  protected val Iimm = io.inst(31, 20).asSInt
  protected val Simm = Cat(io.inst(31, 25), io.inst(11, 7)).asSInt
  protected val Bimm = Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W)).asSInt
  protected val Uimm = Cat(io.inst(31, 12), 0.U(12.W)).asSInt
  protected val Jimm = Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 25), io.inst(24, 21), 0.U(1.W)).asSInt
  protected val Zimm = io.inst(19, 15).zext

  io.out := MuxLookup(
    io.sel,
    Iimm & -2.S,
    Seq(
      IMM_I -> Iimm,
      IMM_S -> Simm,
      IMM_B -> Bimm,
      IMM_U -> Uimm,
      IMM_J -> Jimm,
      IMM_Z -> Zimm
    )
  ).asUInt
}
