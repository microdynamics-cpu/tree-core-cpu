package treecorel2

import chisel3._
import chisel3.util._

class RegFile {
  val gpr = RegInit(VecInit(Seq.fill(32)(0.U(64.W))))
  def read(addr: UInt): UInt = { gpr(addr) }
  def write(wen: Bool, addr: UInt, data: UInt): Unit = {
    when(wen && addr =/= 0.U) { gpr(addr) := data }
  }
}
