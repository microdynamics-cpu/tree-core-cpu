package treecorel2

import chisel3._
import chisel3.util._

class Divider(val len: Int) extends Module {
  val io = IO(new Bundle {
    val en       = Input(Bool())
    val flush    = Input(Bool())
    val divZero  = Output(Bool())
    val done     = Output(Bool())
    val divident = Input(UInt(len.W))
    val divisor  = Input(UInt(len.W))
    val quo      = Output(UInt(len.W))
    val rem      = Output(UInt(len.W))
  })

  io.divZero := false.B
  io.done    := false.B
  io.quo     := 0.U
  io.rem     := 0.U
}
