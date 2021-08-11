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

  protected val resReg:    UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:  Bool = RegInit(false.B)
  protected val wtAddrReg: UInt = RegInit(0.U(RegAddrLen.W))

  resReg    := io.maResIn
  wtEnaReg  := io.maWtEnaIn
  wtAddrReg := io.maWtAddrIn

  io.wbResOut    := resReg
  io.wbWtEnaOut  := wtEnaReg
  io.wbWtAddrOut := wtAddrReg

  //@printf(p"[ma2wb]io.wbResOut = 0x${Hexadecimal(io.wbResOut)}\n")
  //@printf(p"[ma2wb]io.wbWtEnaOut = 0x${Hexadecimal(io.wbWtEnaOut)}\n")
  //@printf(p"[ma2wb]io.wbWtAddrOut = 0x${Hexadecimal(io.wbWtAddrOut)}\n")
}
