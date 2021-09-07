package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._

class ExecutionStage extends Module with InstConfig {
  val io = IO(new Bundle {
    // from id2ex
    val exuOperTypeIn: UInt = Input(UInt(InstOperTypeLen.W))
    val rsValAIn:      UInt = Input(UInt(BusWidth.W))
    val rsValBIn:      UInt = Input(UInt(BusWidth.W))
    val offsetIn:      UInt = Input(UInt(BusWidth.W))
    // from csr
    val csrRdDataIn: UInt = Input(UInt(BusWidth.W))
    // to ex2ma
    val wtDataOut: UInt = Output(UInt(BusWidth.W))
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
}
