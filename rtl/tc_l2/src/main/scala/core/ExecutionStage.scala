package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._

class ExecutionStage extends Module with InstConfig {
  val io = IO(new Bundle {
    val instAddrIn:          UInt = Input(UInt(BusWidth.W))
    val exuOperTypeIn:       UInt = Input(UInt(EXUOperTypeLen.W))
    val exuOperTypeInfromId: UInt = Input(UInt(EXUOperTypeLen.W))
    val rsValAIn:            UInt = Input(UInt(BusWidth.W))
    val rsValBIn:            UInt = Input(UInt(BusWidth.W))
    val offsetIn:            UInt = Input(UInt(BusWidth.W))

    val resOut:         UInt = Output(UInt(BusWidth.W))
    val ifJumpOut:      Bool = Output(Bool())
    val newInstAddrOut: UInt = Output(UInt(BusWidth.W))
    val jumpTypeOut:    UInt = Output(UInt(JumpTypeLen.W))
  })

  protected val alu = Module(new ALU)
  alu.io.exuOperTypeIn := io.exuOperTypeIn
  alu.io.rsValAIn      := io.rsValAIn
  alu.io.rsValBIn      := io.rsValBIn
  io.resOut            := alu.io.resOut

  protected val beu = Module(new BEU)
  beu.io.instAddrIn    := io.instAddrIn
  beu.io.exuOperTypeIn := io.exuOperTypeInfromId
  // beu.io.rsValAIn      := io.rsValAIn
  // beu.io.rsValBIn      := io.rsValBIn
  beu.io.offsetIn := io.offsetIn

  io.ifJumpOut      := beu.io.ifJumpOut
  io.newInstAddrOut := beu.io.newInstAddrOut
  io.jumpTypeOut    := beu.io.jumpTypeOut
}
