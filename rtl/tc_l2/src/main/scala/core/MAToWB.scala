package treecorel2

import chisel3._

class MAToWB extends Module with InstConfig {
  val io = IO(new Bundle {
    val maDataIn:         UInt = Input(UInt(BusWidth.W))
    val maWtEnaIn:        Bool = Input(Bool())
    val maWtAddrIn:       UInt = Input(UInt(RegAddrLen.W))
    val diffIfSkipInstIn: Bool = Input(Bool())

    val wbDataOut:         UInt = Output(UInt(BusWidth.W))
    val wbWtEnaOut:        Bool = Output(Bool())
    val wbWtAddrOut:       UInt = Output(UInt(RegAddrLen.W))
    val diffIfSkipInstOut: Bool = Output(Bool())
  })

  protected val resReg:            UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:          Bool = RegInit(false.B)
  protected val wtAddrReg:         UInt = RegInit(0.U(RegAddrLen.W))
  protected val diffIfSkipInstReg: Bool = RegInit(false.B)

  resReg            := io.maDataIn
  wtEnaReg          := io.maWtEnaIn
  wtAddrReg         := io.maWtAddrIn
  diffIfSkipInstReg := io.diffIfSkipInstIn

  io.wbDataOut         := resReg
  io.wbWtEnaOut        := wtEnaReg
  io.wbWtAddrOut       := wtAddrReg
  io.diffIfSkipInstOut := diffIfSkipInstReg

  //@printf(p"[ma2wb]io.wbDataOut = 0x${Hexadecimal(io.wbDataOut)}\n")
  //@printf(p"[ma2wb]io.wbWtEnaOut = 0x${Hexadecimal(io.wbWtEnaOut)}\n")
  //@printf(p"[ma2wb]io.wbWtAddrOut = 0x${Hexadecimal(io.wbWtAddrOut)}\n")
}
