package treecorel3

import chisel._
import chisel.uitl._

object ALU {
  val ALUOperLen = 4
  val ALU_ADD    = 0.U(ALUOperLen.W)
  val ALU_SUB    = 1.U(ALUOperLen.W)
  val ALU_AND    = 2.U(ALUOperLen.W)
  val ALU_OR     = 3.U(ALUOperLen.W)
  val ALU_XOR    = 4.U(ALUOperLen.W)
  val ALU_SLT    = 5.U(ALUOperLen.W)
  val ALU_SLL    = 6.U(ALUOperLen.W)
  val ALU_SLTU   = 7.U(ALUOperLen.W)
  val ALU_SRL    = 8.U(ALUOperLen.W)
  val ALU_SRA    = 9.U(ALUOperLen.W)
  val ALU_COPY_A = 10.U(ALUOperLen.W)
  val ALU_COPY_B = 11.U(ALUOperLen.W)
  val ALU_XXX    = 15.U(ALUOperLen.W)
}

class ALUIO extends Bundle with IOConfig {
  val A      = Input(UInt(XLen.W))
  val B      = Input(UInt(XLen.W))
  val alu_op = Input(UInt(4.W))
  val out    = Output(UInt(XLen.W))
  val sum    = Output(UInt(XLen.W))
}

class ALU extends Module with InstConfig {
  val io               = IO(new ALUIO)
  protected val sum    = io.A + Mux(io.alu_op(0), -io.B, io.B)
  protected val cmp    = Mux(io.A(XLen - 1) === io.B(XLen - 1), sum(XLen - 1), Mux(io.alu_op(1), io.B(XLen - 1), io.A(XLen - 1)))
  protected val shamt  = io.B(4, 0).asUInt
  protected val shin   = Mux(io.alu_op(3), io.A, Reverse(io.A))
  protected val shiftr = (Cat(io.alu_op(0) && shin(XLen - 1), shin).asSInt >> shamt)(XLen - 1, 0)
  protected val shiftl = Reverse(shiftr)

  protected val out = MuxLookup(
    io.alu_op,
    io.B,
    Seq(
      ALU.ALU_ADD    -> sum,
      ALU.ALU_SUB    -> sum,
      ALU.ALU_SLT    -> cmp,
      ALU.ALU_SLTU   -> cmp,
      ALU.ALU_SRA    -> shiftr,
      ALU.ALU_SRL    -> shiftr,
      ALU.ALU_SLL    -> shiftl,
      ALU.ALU_AND    -> (io.A & io.B),
      ALU.ALU_OR     -> (io.A | io.B),
      ALU.ALU_XOR    -> (io.A ^ io.B),
      ALU.ALU_COPY_A -> io.A
    )
  )
  io.out := out
  io.sum := sum
}
