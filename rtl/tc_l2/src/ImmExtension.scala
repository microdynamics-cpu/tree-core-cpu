package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import InstDecoderStage.{bInstType, iInstType, nopInstType, rInstType, sInstType}

class ImmExtension extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instDataIn: UInt = Input(UInt(BusWidth.W))
    val instTypeIn: UInt = Input(UInt(InstTypeLen.W))
    val immOut:     UInt = Output(UInt(BusWidth.W))
  })

  // construct different type immediate
  private val rTypeImm: UInt = 0.U(BusWidth.W)
  private val iTypeImm: UInt = io.instDataIn(31, 20)
  private val sTypeImm: UInt = Cat(io.instDataIn(31, 25), io.instDataIn(11, 7))
  private val bTypeImm: UInt = Cat(io.instDataIn(31), io.instDataIn(7), io.instDataIn(30, 25), io.instDataIn(11, 8))

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
//   io.imm := MuxLookup(io.imm_sel, 0.U, Seq(IMM_R -> Iimm, IMM_I -> Iimm, IMM_S -> Simm, IMM_B -> Bimm))
}
