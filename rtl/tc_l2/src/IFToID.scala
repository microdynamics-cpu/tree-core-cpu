package treecorel2

import chisel3._

class IFToID extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val ifInstAddr: UInt = Input(UInt(BusWidth.W))
    val ifInstData: UInt = Input(UInt(BusWidth.W))

    val idInstAddr: UInt = Output(UInt(BusWidth.W))
    val idInstData: UInt = Output(UInt(BusWidth.W))
  })

  private val pcRegister:   UInt = RegInit(0.U(BusWidth.W))
  private val instRegister: UInt = RegInit(0.U(BusWidth.W))

  pcRegister   := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.ifInstAddr)
  instRegister := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.ifInstData)

  io.idInstAddr := pcRegister
  io.idInstData := instRegister
}

// object PCRegister extends App {
//   (new chisel3.stage.ChiselStage).execute(
//     args,
//     Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new PCRegister()))
//   )
// }
