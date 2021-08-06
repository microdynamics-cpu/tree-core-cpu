package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import InstDecoderStage.{bInstType, iInstType, nopInstType, rInstType, sInstType}

class ImmExtension extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instDataIn: UInt = Input(UInt(InstWidth.W))
    val instTypeIn: UInt = Input(UInt(InstTypeLen.W))
    val immOut:     UInt = Output(UInt(BusWidth.W))
  })

  // construct different type immediate
  protected val rTypeImm: UInt = 0.U(BusWidth.W)
  protected val iTypeImm: UInt = io.instDataIn(31, 20)
  protected val sTypeImm: UInt = Cat(io.instDataIn(31, 25), io.instDataIn(11, 7))
  protected val bTypeImm: UInt = Cat(io.instDataIn(31), io.instDataIn(7), io.instDataIn(30, 25), io.instDataIn(11, 8))

  io.immOut := MuxLookup(
    io.instTypeIn,
    0.U(BusWidth.W),
    Seq(
      rInstType -> rTypeImm,
      iInstType -> iTypeImm,
      sInstType -> sTypeImm,
      bInstType -> bTypeImm
    )
  )
}
