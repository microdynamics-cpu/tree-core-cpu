package treecorel2

import chisel3._

class InstDecoderStage extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val ifInstAddr: UInt = Input(UInt(BusWidth.W))
    val ifInstData: UInt = Input(UInt(BusWidth.W))

    val idInstAddr: UInt = Output(UInt(BusWidth.W))
    val idInstData: UInt = Output(UInt(BusWidth.W))
  })
}
