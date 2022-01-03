package treecorel2

import chisel3._
import chisel3.util._

class DXCHGIO extends Bundle {
  val clk   = Output(Clock())
  val ren   = Output(Bool())
  val raddr = Output(UInt(64.W))
  val rsize = Output(UInt(3.W))
  val rdata = Input(UInt(64.W))
  val wen   = Output(Bool())
  val waddr = Output(UInt(64.W))
  val wdata = Output(UInt(64.W))
  val wmask = Output(UInt(8.W))
}
