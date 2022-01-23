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
