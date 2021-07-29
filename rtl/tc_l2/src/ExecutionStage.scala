package treecorel2

import chisel3._
import chisel3.util.MuxLookup

object ExecutionStage {
  val aluADDIType = 0.U(4.W)
//   val ALU_SUB     = 1.U(4.W)
//   val ALU_AND     = 2.U(4.W)
//   val ALU_OR      = 3.U(4.W)
  val aluNopType = 15.U(4.W)
}

// class ExecutionStage extends Module with ConstantDefine {

// }
