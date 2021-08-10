package treecorel2

import chisel3._
import chisel3.util.MuxLookup

object ExecutionStage {
  // TODO: some code 4 is needed to be in ConstDefine
  // TODO: with ALUOperTypeLen
  val aluADDIType = 0.U(4.W)
//   val ALU_SUB     = 1.U(4.W)
//   val ALU_AND     = 2.U(4.W)
//   val ALU_OR      = 3.U(4.W)
  val aluNopType = 15.U(4.W)
}

class ExecutionStage extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val aluOperTypeIn: UInt = Input(UInt(ALUOperTypeLen.W))
    val rsValAIn:      UInt = Input(UInt(BusWidth.W))
    val rsValBIn:      UInt = Input(UInt(BusWidth.W))

    val resOut: UInt = Output(UInt(BusWidth.W))
  })

  io.resOut := MuxLookup(
    io.aluOperTypeIn,
    0.U,
    Seq(
      ExecutionStage.aluADDIType -> (io.rsValAIn + io.rsValBIn)
    )
  )

  //@printf(p"[ex]io.aluOperTypeIn = 0x${Hexadecimal(io.aluOperTypeIn)}\n")
  //@printf(p"[ex]io.rsValAIn = 0x${Hexadecimal(io.rsValAIn)}\n")
  //@printf(p"[ex]io.rsValBIn = 0x${Hexadecimal(io.rsValBIn)}\n")
  //@printf(p"[ex]io.resOut = 0x${Hexadecimal(io.resOut)}\n")
}
