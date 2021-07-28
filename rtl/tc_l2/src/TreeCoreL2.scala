package treecorel2

import chisel3._

class TreeCoreL2 extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val in1:  UInt = Input(UInt(BusWidth.W))
    val out2: UInt = Output(UInt(BusWidth.W))

    val outAddr: UInt = Output(UInt(BusWidth.W))
    val outEna:  Bool = Output(Bool())
  })

  val pc    = Module(new PCRegister);
  val if2id = Module(new IFToID);

  io.outEna := pc.io.instEna

  if2id.io.ifInstAddr := pc.io.instAddr
  if2id.io.ifInstData := io.in1
  io.outAddr          := if2id.io.idInstAddr
  io.out2             := if2id.io.idInstData

}
