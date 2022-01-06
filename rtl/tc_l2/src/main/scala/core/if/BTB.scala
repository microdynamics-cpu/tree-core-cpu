package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.ConstVal

class BTBLine extends Bundle {
  val pc   = UInt(ConstVal.AddrLen.W)
  val tgt  = UInt(ConstVal.AddrLen.W)
  val jump = Bool()
}

class BTB extends Module {
  val io = IO(new Bundle {
    // branch info (from idu)
    val branch = Input(Bool())
    val jump   = Input(Bool())
    val pc     = Input(UInt(ConstVal.AddrLen.W))
    val tgt    = Input(UInt(ConstVal.AddrLen.W))
    // BTB lookup interface
    val lookupBranch = Output(Bool())
    val lookupJump   = Output(Bool())
    val lookupPc     = Input(UInt(ConstVal.AddrLen.W))
    val lookupTgt    = Output(UInt(ConstVal.AddrLen.W))
  })

  // definitions of BTB lines and valid bits
  protected val valids = RegInit(VecInit(Seq.fill(ConstVal.BTBSize) { false.B }))
  protected val lines  = Mem(ConstVal.BTBSize, new BTBLine)

  // branch info for BTB lines
  protected val idx = io.pc(ConstVal.BTBIdxLen - 1, 0)
  // write to BTB lines
  when(io.branch) {
    valids(idx)     := true.B
    lines(idx).jump := io.jump
    lines(idx).pc   := io.pc
    lines(idx).tgt  := io.tgt
  }

  // signals about BTB lookup
  val lookupIdx   = io.lookupPc(ConstVal.BTBIdxLen - 1, 0)
  val lookupPcSel = io.lookupPc
  val btbHit      = valids(lookupIdx) && lines(lookupIdx).pc === lookupPcSel

  // BTB lookup
  io.lookupBranch := btbHit
  io.lookupJump   := Mux(btbHit, lines(lookupIdx).jump, false.B)
  io.lookupTgt    := Mux(btbHit, lines(lookupIdx).tgt, 0.U(64.W))
}
