package treecorel2

import chisel3._

class MemoryAccessStage extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val resIn:    UInt = Input(UInt(BusWidth.W))
    val wtEnaIn:  Bool = Input(Bool())
    val wtAddrIn: UInt = Input(UInt(RegAddrLen.W))

    val resOut:    UInt = Output(UInt(BusWidth.W))
    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))
  })

  io.resOut    := io.resIn
  io.wtEnaOut  := io.wtEnaIn
  io.wtAddrOut := io.wtAddrIn
}
