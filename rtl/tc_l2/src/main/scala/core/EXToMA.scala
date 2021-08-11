package treecorel2

import chisel3._

class EXToMA extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val exResIn:    UInt = Input(UInt(BusWidth.W))
    val exWtEnaIn:  Bool = Input(Bool())
    val exWtAddrIn: UInt = Input(UInt(RegAddrLen.W))

    val maResOut:    UInt = Output(UInt(BusWidth.W))
    val maWtEnaOut:  Bool = Output(Bool())
    val maWtAddrOut: UInt = Output(UInt(RegAddrLen.W))
  })

  protected val resReg:    UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:  Bool = RegInit(false.B)
  protected val wtAddrReg: UInt = RegInit(0.U(RegAddrLen.W))

  resReg    := io.exResIn
  wtEnaReg  := io.exWtEnaIn
  wtAddrReg := io.exWtAddrIn

  io.maResOut    := resReg
  io.maWtEnaOut  := wtEnaReg
  io.maWtAddrOut := wtAddrReg
}
