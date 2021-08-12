package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.{getSignExtn, getZeroExtn}

object ExecutionStage {
  protected val ALUOperTypeLen = 6

  val aluADDIType  = 0.U(ALUOperTypeLen.W)
  val aluADDIWType = 1.U(ALUOperTypeLen.W)
  val aluSLTIType  = 2.U(ALUOperTypeLen.W)
  val aluSLTIUType = 3.U(ALUOperTypeLen.W)

  val aluANDIType = 4.U(ALUOperTypeLen.W)
  val aluORIType  = 5.U(ALUOperTypeLen.W)
  val aluXORIType = 6.U(ALUOperTypeLen.W)

  val aluNopType = 63.U(ALUOperTypeLen.W)
}

class ExecutionStage extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val aluOperTypeIn: UInt = Input(UInt(ALUOperTypeLen.W))
    val rsValAIn:      UInt = Input(UInt(BusWidth.W))
    val rsValBIn:      UInt = Input(UInt(BusWidth.W))

    val resOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val res: UInt = Wire(UInt(BusWidth.W))

  res := MuxLookup(
    io.aluOperTypeIn,
    0.U,
    Seq(
      ExecutionStage.aluADDIType -> (io.rsValAIn + io.rsValBIn),
      ExecutionStage.aluADDIWType -> (io.rsValAIn + io.rsValBIn),
      ExecutionStage.aluSLTIType -> Cat(0.U((BusWidth - 1).W), io.rsValAIn.asSInt < io.rsValBIn.asSInt),
      ExecutionStage.aluSLTIUType -> (io.rsValAIn < io.rsValBIn),
      ExecutionStage.aluANDIType -> (io.rsValAIn & io.rsValBIn),
      ExecutionStage.aluORIType -> (io.rsValAIn | io.rsValBIn),
      ExecutionStage.aluXORIType -> (io.rsValAIn ^ io.rsValBIn)
    )
  )

  when(io.aluOperTypeIn === ExecutionStage.aluADDIWType) {
    io.resOut := getSignExtn(BusWidth, res(31, 0))
  }.otherwise {
    io.resOut := res
  }

  //@printf(p"[ex]io.aluOperTypeIn = 0x${Hexadecimal(io.aluOperTypeIn)}\n")
  //@printf(p"[ex]io.rsValAIn = 0x${Hexadecimal(io.rsValAIn)}\n")
  //@printf(p"[ex]io.rsValBIn = 0x${Hexadecimal(io.rsValBIn)}\n")
  //@printf(p"[ex]io.resOut = 0x${Hexadecimal(io.resOut)}\n")
}
