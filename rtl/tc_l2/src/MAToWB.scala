package treecorel2

import chisel3._

class MAToWB extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val maResIn:    UInt = Input(UInt(BusWidth.W))
    val maWtEnaIn:  Bool = Input(Bool())
    val maWtAddrIn: UInt = Input(UInt(RegAddrLen.W))

    val wbResOut:    UInt = Output(UInt(BusWidth.W))
    val wbWtEnaOut:  Bool = Output(Bool())
    val wbWtAddrOut: UInt = Output(UInt(RegAddrLen.W))
  })

  protected val resRegister:    UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaRegister:  Bool = RegInit(false.B)
  protected val wtAddrRegister: UInt = RegInit(0.U(RegAddrLen.W))

  resRegister    := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.maResIn)
  wtEnaRegister  := Mux(this.reset.asBool(), false.B, io.maWtEnaIn)
  wtAddrRegister := Mux(this.reset.asBool(), 0.U(RegAddrLen.W), io.maWtAddrIn)

  io.wbResOut    := resRegister
  io.wbWtEnaOut  := wtEnaRegister
  io.wbWtAddrOut := wtAddrRegister
}
