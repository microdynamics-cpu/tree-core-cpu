package treecorel2

import chisel3._
import chisel3.util._

class EX2MEMIO extends ID2EXIO with IOConfig {
  val aluRes     = Output(UInt(XLen.W))
  val branch     = Output(Bool())
  val tgt        = Output(UInt(XLen.W))
  val link       = Output(UInt(XLen.W))
  val auipc      = Output(UInt(XLen.W))
  val csrData    = Output(UInt(XLen.W))
  val timeIntrEn = Output(Bool())
  val ecallEn    = Output(Bool())
  val csr        = Output(new csr)
}
