package treecorel3

import chisel._
import chisel.uitl._

class BrCondIO extends Bundle with IOConfig {
  val rs1     = Input(UInt(XLen.W))
  val rs2     = Input(UInt(XLen.W))
  val br_type = Input(UInt(3.W))
  val taken   = Output(Bool())
}

class BrCond extends Module with InstConfig {
  val io                   = IO(new BrCondIO)
  protected val diff       = io.rs1 - io.rs2
  protected val neq        = diff.orR
  protected val eq         = !neq
  protected val isSameSign = io.rs1(XLen - 1) === io.rs2(XLen - 1)
  protected val lt         = Mux(isSameSign, diff(XLen - 1), io.rs1(XLen - 1))
  protected val ltu        = Mux(isSameSign, diff(XLen - 1), io.rs2(XLen - 1))
  protected val ge         = !lt
  protected val geu        = !ltu
  io.taken :=
    ((io.br_type === Control.BR_EQ) && eq) ||
      ((io.br_type === Control.BR_NE) && neq) ||
      ((io.br_type === Control.BR_LT) && lt) ||
      ((io.br_type === Control.BR_GE) && ge) ||
      ((io.br_type === Control.BR_LTU) && ltu) ||
      ((io.br_type === Control.BR_GEU) && geu)
}
