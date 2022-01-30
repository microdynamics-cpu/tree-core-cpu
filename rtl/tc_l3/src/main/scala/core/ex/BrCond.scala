package treecorel3

import chisel._
import chisel.uitl._

class BrCondIO(implicit p: Parameters) extends CoreBundle()(p) {
  val rs1     = Input(UInt(xlen.W))
  val rs2     = Input(UInt(xlen.W))
  val br_type = Input(UInt(3.W))
  val taken   = Output(Bool())
}

class BrCond(implicit val p: Parameters) extends Module {
  val io         = IO(new BrCondIO)
  val diff       = io.rs1 - io.rs2
  val neq        = diff.orR
  val eq         = !neq
  val isSameSign = io.rs1(xlen - 1) === io.rs2(xlen - 1)
  val lt         = Mux(isSameSign, diff(xlen - 1), io.rs1(xlen - 1))
  val ltu        = Mux(isSameSign, diff(xlen - 1), io.rs2(xlen - 1))
  val ge         = !lt
  val geu        = !ltu
  io.taken :=
    ((io.br_type === Control.BR_EQ) && eq) ||
      ((io.br_type === Control.BR_NE) && neq) ||
      ((io.br_type === Control.BR_LT) && lt) ||
      ((io.br_type === Control.BR_GE) && ge) ||
      ((io.br_type === Control.BR_LTU) && ltu) ||
      ((io.br_type === Control.BR_GEU) && geu)
}
