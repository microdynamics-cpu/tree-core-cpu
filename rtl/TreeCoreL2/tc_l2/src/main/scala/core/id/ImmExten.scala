package treecorel2

import chisel3._
import chisel3.util._

class ImmExten extends Module with InstConfig {
  val io = IO(new Bundle {
    val inst     = Input(UInt(InstLen.W))
    val instType = Input(UInt(InstTypeLen.W))
    val imm      = Output(UInt(XLen.W))
  })

  // construct different type immediate
  protected val rTypeImm = 0.U(XLen.W)
  protected val iTypeImm = SignExt(io.inst(31, 20), XLen)
  protected val sTypeImm = SignExt(Cat(io.inst(31, 25), io.inst(11, 7)), XLen)
  protected val bTypeImm = SignExt(Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W)), XLen)
  protected val uTypeImm = SignExt(Cat(io.inst(31, 12), 0.U(12.W)), XLen)
  protected val jTypeImm = SignExt(Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W)), XLen)

  io.imm := MuxLookup(
    io.instType,
    0.U(XLen.W),
    Seq(
      rInstType -> rTypeImm,
      iInstType -> iTypeImm,
      sInstType -> sTypeImm,
      bInstType -> bTypeImm,
      uInstType -> uTypeImm,
      jInstType -> jTypeImm
    )
  )
}
