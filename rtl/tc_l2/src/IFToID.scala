package treecorel2

import chisel3._

class IFToID extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val ifInstAddrIn: UInt = Input(UInt(BusWidth.W))
    val ifInstDataIn: UInt = Input(UInt(BusWidth.W))

    val idInstAddrOut: UInt = Output(UInt(BusWidth.W))
    val idInstDataOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val pcRegister:   UInt = RegInit(0.U(BusWidth.W))
  protected val instRegister: UInt = RegInit(0.U(BusWidth.W))

  pcRegister   := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.ifInstAddrIn)
  instRegister := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.ifInstDataIn)

  io.idInstAddrOut := pcRegister
  io.idInstDataOut := instRegister
}

// object PCRegister extends App {
//   (new chisel3.stage.ChiselStage).execute(
//     args,
//     Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new PCRegister()))
//   )
// }
