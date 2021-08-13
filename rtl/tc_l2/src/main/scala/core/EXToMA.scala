package treecorel2

import chisel3._

class EXToMA extends Module with InstConfig {
  val io = IO(new Bundle {
    val exDataIn:         UInt = Input(UInt(BusWidth.W))
    val exWtEnaIn:        Bool = Input(Bool())
    val exWtAddrIn:       UInt = Input(UInt(RegAddrLen.W))
    val diffIfSkipInstIn: Bool = Input(Bool())

    val maDataOut:         UInt = Output(UInt(BusWidth.W))
    val maWtEnaOut:        Bool = Output(Bool())
    val maWtAddrOut:       UInt = Output(UInt(RegAddrLen.W))
    val diffIfSkipInstOut: Bool = Output(Bool())
  })

  protected val resReg:            UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:          Bool = RegInit(false.B)
  protected val wtAddrReg:         UInt = RegInit(0.U(RegAddrLen.W))
  protected val diffIfSkipInstReg: Bool = RegInit(false.B)

  resReg            := io.exDataIn
  wtEnaReg          := io.exWtEnaIn
  wtAddrReg         := io.exWtAddrIn
  diffIfSkipInstReg := io.diffIfSkipInstIn

  io.maDataOut         := resReg
  io.maWtEnaOut        := wtEnaReg
  io.maWtAddrOut       := wtAddrReg
  io.diffIfSkipInstOut := diffIfSkipInstReg
}
