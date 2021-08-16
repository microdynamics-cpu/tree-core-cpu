package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._
import InstDecoderStage.{bInstType, iInstType, jInstType, nopInstType, rInstType, sInstType, uInstType}

class ImmExten extends Module with InstConfig {
  val io = IO(new Bundle {
    val instDataIn: UInt = Input(UInt(InstWidth.W))
    val instTypeIn: UInt = Input(UInt(InstTypeLen.W))
    val immOut:     UInt = Output(UInt(BusWidth.W))
  })

  // construct different type immediate
  protected val rTypeImm: UInt = 0.U(BusWidth.W)
  protected val iTypeImm: UInt = io.instDataIn(31, 20)
  protected val sTypeImm: UInt = Cat(io.instDataIn(31, 25), io.instDataIn(11, 7))
  protected val bTypeImm: UInt = Cat(io.instDataIn(31), io.instDataIn(7), io.instDataIn(30, 25), io.instDataIn(11, 8), 0.U(1.W))
  protected val uTypeImm: UInt = io.instDataIn(31, 12)
  protected val jTypeImm: UInt = Cat(io.instDataIn(31), io.instDataIn(19, 12), io.instDataIn(20), io.instDataIn(30, 21), 0.U(1.W))

  io.immOut := MuxLookup(
    io.instTypeIn,
    0.U(BusWidth.W),
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
