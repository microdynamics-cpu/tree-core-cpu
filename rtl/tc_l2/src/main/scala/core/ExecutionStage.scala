package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}

class ExecutionStage extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val aluOperTypeIn: UInt = Input(UInt(ALUOperTypeLen.W))
    val rsValAIn:      UInt = Input(UInt(BusWidth.W))
    val rsValBIn:      UInt = Input(UInt(BusWidth.W))

    val resOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val alu = Module(new ALU)
  alu.io.aluOperTypeIn := io.aluOperTypeIn
  alu.io.rsValAIn      := io.rsValAIn
  alu.io.rsValBIn      := io.rsValBIn
  io.resOut            := alu.io.resOut

}
