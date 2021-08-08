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

  protected val resRegister:    UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaRegister:  Bool = RegInit(false.B)
  protected val wtAddrRegister: UInt = RegInit(0.U(RegAddrLen.W))

  resRegister    := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.exResIn)
  wtEnaRegister  := Mux(this.reset.asBool(), false.B, io.exWtEnaIn)
  wtAddrRegister := Mux(this.reset.asBool(), 0.U(RegAddrLen.W), io.exWtAddrIn)

  io.maResOut    := resRegister
  io.maWtEnaOut  := wtEnaRegister
  io.maWtAddrOut := wtAddrRegister
}
