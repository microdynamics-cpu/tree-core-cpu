package treecorel2

import chisel3._

class TreeCoreL2 extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val in1:  UInt = Input(UInt(BusWidth.W))
    val out2: UInt = Output(UInt(BusWidth.W))

    val outAddr: UInt = Output(UInt(BusWidth.W))
    val outEna:  Bool = Output(Bool())
  })

  val pcUnit    = Module(new PCRegister)
  val if2idUnit = Module(new IFToID)

  io.outEna := pcUnit.io.instEnaOut

  if2idUnit.io.ifInstAddrIn := pcUnit.io.instAddrOut
  if2idUnit.io.ifInstDataIn := io.in1
  io.outAddr                := if2idUnit.io.idInstAddrOut
  io.out2                   := if2idUnit.io.idInstDataOut

}
