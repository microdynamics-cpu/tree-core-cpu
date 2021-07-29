package treecorel2

import chisel3._

class InstDecoderStage extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instAddrIn: UInt = Input(UInt(BusWidth.W))
    val instDataIn: UInt = Input(UInt(BusWidth.W))

    val rdDataAIn: UInt = Input(UInt(BusWidth.W))
    val rdDataBIn: UInt = Input(UInt(BusWidth.W))

    val rdEnaAIn:  UInt = Output(Bool())
    val rdAddrAIn: UInt = Output(UInt(RegAddrLen.W))
    val rdEnaBIn:  UInt = Output(Bool())
    val rdAddrBIn: UInt = Output(UInt(RegAddrLen.W))

    val aluOpcodeOut: UInt = Output(UInt(ALUOpcodeLen.W))
    val aluSelOut:    UInt = Output(UInt(ALUSelLen.W))
    val rsValAOut:    UInt = Output(UInt(BusWidth.W))
    val rsValBOut:    UInt = Output(UInt(BusWidth.W))

    val wtEnaOut:  UInt = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))
  })

}
