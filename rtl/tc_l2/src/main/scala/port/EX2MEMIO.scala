package treecorel2

import chisel3._
import chisel3.util._

class EX2MEMIO extends ID2EXIO {
  val aluRes     = Output(UInt(64.W))
  val branch     = Output(Bool())
  val tgt        = Output(UInt(64.W))
  val link       = Output(UInt(64.W))
  val auipc      = Output(UInt(64.W))
  val csrData    = Output(UInt(64.W))
  val timeIntrEn = Output(Bool())
  val ecallEn    = Output(Bool())
  val csr        = Output(new csr)
}
