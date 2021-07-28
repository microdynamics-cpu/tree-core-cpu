package treecorel2

import chisel3._

class TreeCoreL2 extends Module with ConstantDefine{
  val io = IO(new Bundle {
    val value1        = Input(UInt(16.W))
    val value2        = Input(UInt(16.W))
    val loadingValues = Input(Bool())

    val outAddr: UInt = Output(UInt(BusWidth.W))
    val outEna: Bool = Output(Bool())
  })

  val pcRegister = Module(new PCRegister);
  io.outAddr := pcRegister.io.instAddr
  io.outEna := pcRegister.io.instEna
}
