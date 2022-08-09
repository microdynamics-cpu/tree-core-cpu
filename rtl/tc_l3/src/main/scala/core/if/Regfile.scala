package treecorel3

import chisel._
import chisel.uitl._

class RegFileIO extends Bundle with IOConfig {
  val raddr1 = Input(UInt(RegfileLen.W))
  val raddr2 = Input(UInt(RegfileLen.W))
  val rdata1 = Output(UInt(XLen.W))
  val rdata2 = Output(UInt(XLen.W))
  val wen    = Input(Bool())
  val waddr  = Input(UInt(RegfileLen.W))
  val wdata  = Input(UInt(XLen.W))
}

class RegFile extends Module with InstConfig {
  val io             = IO(new RegFileIO)
  protected val regs = Mem(RegfileNum, UInt(XLen.W))
  io.rdata1 := Mux(io.raddr1.orR, regs(io.raddr1), 0.U(XLen.W))
  io.rdata2 := Mux(io.raddr2.orR, regs(io.raddr2), 0.U(XLen.W))
  when(io.wen & io.waddr.orR) {
    regs(io.waddr) := io.wdata
  }
}
