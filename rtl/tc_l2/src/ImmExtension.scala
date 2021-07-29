package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}

class ImmExtension extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instIn:     UInt = Input(UInt(BusWidth.W))
    val instTypeIn: UInt = Input(UInt(InstTypeLen.W))
    val immOut:     UInt = Output(UInt(BusWidth.W))
  })

  private val rTypeImm: UInt = 0.U(BusWidth.W)
  private val iTypeImm: UInt = io.instIn(31, 20)
  private val sTypeImm: UInt = Cat(io.instIn(31, 25), io.instIn(11, 7))
  private val bTypeImm: UInt = Cat(io.instIn(31), io.instIn(7), io.instIn(30, 25), io.instIn(11, 8))

  io.immOut := MuxLookup(
    io.instTypeIn,
    0.U,
    Seq(
      0.U -> rTypeImm,
      1.U -> iTypeImm,
      2.U -> sTypeImm,
      3.U -> bTypeImm
    )
  )
//   io.imm := MuxLookup(io.imm_sel, 0.U, Seq(IMM_R -> Iimm, IMM_I -> Iimm, IMM_S -> Simm, IMM_B -> Bimm))
}
