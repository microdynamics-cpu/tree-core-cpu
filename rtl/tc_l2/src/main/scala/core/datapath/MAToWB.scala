package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class MAToWB extends Module with InstConfig {
  val io = IO(new Bundle {
    // from ma
    val maDataIn:   UInt = Input(UInt(BusWidth.W))
    val maWtEnaIn:  Bool = Input(Bool())
    val maWtAddrIn: UInt = Input(UInt(RegAddrLen.W))
    // to wb
    val wbDataOut:   UInt = Output(UInt(BusWidth.W))
    val wbWtEnaOut:  Bool = Output(Bool())
    val wbWtAddrOut: UInt = Output(UInt(RegAddrLen.W))
  })

  protected val resReg:    UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:  Bool = RegInit(false.B)
  protected val wtAddrReg: UInt = RegInit(0.U(RegAddrLen.W))

  resReg    := io.maDataIn
  wtEnaReg  := io.maWtEnaIn
  wtAddrReg := io.maWtAddrIn

  io.wbDataOut   := resReg
  io.wbWtEnaOut  := wtEnaReg
  io.wbWtAddrOut := wtAddrReg

  //@printf(p"[ma2wb]io.wbDataOut = 0x${Hexadecimal(io.wbDataOut)}\n")
  //@printf(p"[ma2wb]io.wbWtEnaOut = 0x${Hexadecimal(io.wbWtEnaOut)}\n")
  //@printf(p"[ma2wb]io.wbWtAddrOut = 0x${Hexadecimal(io.wbWtAddrOut)}\n")
}
