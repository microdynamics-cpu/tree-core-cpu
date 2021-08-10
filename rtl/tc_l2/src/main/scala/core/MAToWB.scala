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

  resRegister    := io.maResIn
  wtEnaRegister  := io.maWtEnaIn
  wtAddrRegister := io.maWtAddrIn

  io.wbResOut    := resRegister
  io.wbWtEnaOut  := wtEnaRegister
  io.wbWtAddrOut := wtAddrRegister

  //@printf(p"[ma2wb]io.wbResOut = 0x${Hexadecimal(io.wbResOut)}\n")
  //@printf(p"[ma2wb]io.wbWtEnaOut = 0x${Hexadecimal(io.wbWtEnaOut)}\n")
  //@printf(p"[ma2wb]io.wbWtAddrOut = 0x${Hexadecimal(io.wbWtAddrOut)}\n")
}
