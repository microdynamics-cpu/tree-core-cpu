package sim

import chisel3._
import chisel3.util._

class Arbiter {
  val finished = RegInit(false.B)
  val ren      = RegInit(false.B)
  val raddr    = RegInit(0.U(64.W))
  val rdata    = RegInit(0.U(64.W))
  val rsize    = RegInit(0.U(3.W))
  val wen      = RegInit(false.B)
  val waddr    = RegInit(0.U(64.W))
  val wdata    = RegInit(0.U(64.W))
  val wmask    = RegInit(0.U(8.W))
}
