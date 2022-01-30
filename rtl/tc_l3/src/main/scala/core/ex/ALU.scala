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

class ALUIO(implicit p: Parameters) extends Bundle {
  val A      = Input(UInt(xlen.W))
  val B      = Input(UInt(xlen.W))
  val alu_op = Input(UInt(4.W))
  val out    = Output(UInt(xlen.W))
  val sum    = Output(UInt(xlen.W))
}

class ALU(implicit p: Parameters) extends Module {
  val io     = IO(new ALUIO)
  val sum    = io.A + Mux(io.alu_op(0), -io.B, io.B)
  val cmp    = Mux(io.A(xlen - 1) === io.B(xlen - 1), sum(xlen - 1), Mux(io.alu_op(1), io.B(xlen - 1), io.A(xlen - 1)))
  val shamt  = io.B(4, 0).asUInt
  val shin   = Mux(io.alu_op(3), io.A, Reverse(io.A))
  val shiftr = (Cat(io.alu_op(0) && shin(xlen - 1), shin).asSInt >> shamt)(xlen - 1, 0)
  val shiftl = Reverse(shiftr)

  val out = MuxLookup(
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
