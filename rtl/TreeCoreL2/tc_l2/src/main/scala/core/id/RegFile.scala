package treecorel2

import chisel3._
import chisel3.util._

class RegFile extends InstConfig {
  // public registers
  val gpr = RegInit(VecInit(Seq.fill(RegfileNum)(0.U(XLen.W))))
  // io oper
  def read(addr: UInt): UInt = { gpr(addr) }
  def write(wen: Bool, addr: UInt, data: UInt): Unit = {
    when(wen && addr =/= 0.U) { gpr(addr) := data }
  }
}
