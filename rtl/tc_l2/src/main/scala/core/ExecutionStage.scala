package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._

class ExecutionStage extends Module with InstConfig {
  val io = IO(new Bundle {
    val exuOperNumIn:        UInt = Input(UInt(BusWidth.W))
    val exuOperTypeIn:       UInt = Input(UInt(InstOperTypeLen.W))
    val exuOperTypeFromIdIn: UInt = Input(UInt(InstOperTypeLen.W))
    val rsValAIn:            UInt = Input(UInt(BusWidth.W))
    val rsValBIn:            UInt = Input(UInt(BusWidth.W))
    val rsValAFromIdIn:      UInt = Input(UInt(BusWidth.W))
    val rsValBFromIdIn:      UInt = Input(UInt(BusWidth.W))
    val offsetIn:            UInt = Input(UInt(BusWidth.W))
    // from csr
    val csrRdDataIn: UInt = Input(UInt(BusWidth.W))

    val wtDataOut:      UInt = Output(UInt(BusWidth.W))
    val newInstAddrOut: UInt = Output(UInt(BusWidth.W))
    val jumpTypeOut:    UInt = Output(UInt(JumpTypeLen.W))
    // to csr
    val csrwtEnaOut:  Bool = Output(Bool())
    val csrWtDataOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val alu = Module(new ALU)
  alu.io.exuOperTypeIn := io.exuOperTypeIn
  alu.io.rsValAIn      := io.rsValAIn
  alu.io.rsValBIn      := io.rsValBIn
  alu.io.csrRdDataIn   := io.csrRdDataIn

  io.wtDataOut    := alu.io.wtDataOut
  io.csrwtEnaOut  := alu.io.csrwtEnaOut
  io.csrWtDataOut := alu.io.csrWtDataOut

  protected val beu = Module(new BEU)
  beu.io.exuOperNumIn  := io.exuOperNumIn
  beu.io.exuOperTypeIn := io.exuOperTypeFromIdIn
  beu.io.rsValAIn      := io.rsValAFromIdIn
  beu.io.rsValBIn      := io.rsValBFromIdIn
  beu.io.offsetIn      := io.offsetIn

  io.newInstAddrOut := beu.io.newInstAddrOut
  io.jumpTypeOut    := beu.io.jumpTypeOut
}
