package treecorel2

import chisel3._

class IFToID extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val ifInstAddrIn: UInt = Input(UInt(BusWidth.W))
    val ifInstDataIn: UInt = Input(UInt(InstWidth.W))

    val idInstAddrOut: UInt = Output(UInt(BusWidth.W))
    val idInstDataOut: UInt = Output(UInt(InstWidth.W))
  })

  protected val pcRegister:   UInt = RegInit(0.U(BusWidth.W))
  protected val instRegister: UInt = RegInit(0.U(InstWidth.W))

  pcRegister   := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.ifInstAddrIn)
  instRegister := Mux(this.reset.asBool(), 0.U(InstWidth.W), io.ifInstDataIn)

  io.idInstAddrOut := pcRegister
  io.idInstDataOut := instRegister
}
