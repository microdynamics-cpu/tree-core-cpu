package treecorel2

import chisel3._
import chisel3.util._

class DXCHGIO extends Bundle with IOConfig {
  val ren   = Output(Bool())
  val raddr = Output(UInt(XLen.W))
  val rdata = Input(UInt(XLen.W))
  val rsize = Output(UInt(LDSize.W))
  val wen   = Output(Bool())
  val waddr = Output(UInt(XLen.W))
  val wdata = Output(UInt(XLen.W))
  val wmask = Output(UInt(MaskLen.W))
}
