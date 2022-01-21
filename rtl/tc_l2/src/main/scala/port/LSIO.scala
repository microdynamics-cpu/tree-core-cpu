package treecorel2

import chisel3._
import chisel3.util._

class LDIO extends Bundle with IOConfig {
  val en   = Output(Bool())
  val addr = Output(UInt(XLen.W))
  val data = Input(UInt(XLen.W))
  val size = Output(UInt(LDSize.W))
}

class SDIO extends Bundle with IOConfig {
  val en   = Output(Bool())
  val addr = Output(UInt(XLen.W))
  val data = Output(UInt(XLen.W))
  val mask = Output(UInt(MaskLen.W))
}
