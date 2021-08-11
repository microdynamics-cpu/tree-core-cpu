package treecorel2

import chisel3._

class IFToID extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val ifInstAddrIn: UInt = Input(UInt(BusWidth.W))
    val ifInstDataIn: UInt = Input(UInt(InstWidth.W))

    val idInstAddrOut: UInt = Output(UInt(BusWidth.W))
    val idInstDataOut: UInt = Output(UInt(InstWidth.W))
  })

  protected val pcReg:   UInt = RegInit(0.U(BusWidth.W))
  protected val instReg: UInt = RegInit(0.U(InstWidth.W))

  pcReg   := io.ifInstAddrIn
  instReg := io.ifInstDataIn

  io.idInstAddrOut := pcReg
  io.idInstDataOut := instReg

  //@printf(p"[if2id]io.idInstAddrOut = 0x${Hexadecimal(io.idInstAddrOut)}\n")
  //@printf(p"[if2id]io.idInstDataOut = 0x${Hexadecimal(io.idInstDataOut)}\n")
}
