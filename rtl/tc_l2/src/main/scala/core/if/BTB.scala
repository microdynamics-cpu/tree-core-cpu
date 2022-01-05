package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.ConstVal

class BTBLine extends Bundle {
  val pc   = UInt(ConstVal.BTBPcLen.W)
  val tgt  = UInt(ConstVal.BTBTgtLen.W)
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
  protected val idx    = io.pc(ConstVal.BTBIdxLen + ConstVal.AddrAlignLen - 1, ConstVal.AddrAlignLen)
  protected val linePc = io.pc(ConstVal.AddrLen - 1, ConstVal.BTBIdxLen + ConstVal.AddrAlignLen)

  // write to BTB lines
  when(io.branch) {
    valids(idx)     := true.B
    lines(idx).jump := io.jump
    lines(idx).pc   := linePc
    lines(idx).tgt  := io.tgt(ConstVal.AddrLen - 1, ConstVal.AddrAlignLen)
  }

  // signals about BTB lookup
  val lookupIdx   = io.lookupPc(ConstVal.BTBIdxLen + ConstVal.AddrAlignLen - 1, ConstVal.AddrAlignLen)
  val lookupPcSel = io.lookupPc(ConstVal.AddrLen - 1, ConstVal.BTBIdxLen + ConstVal.AddrAlignLen)
  val btbHit      = valids(lookupIdx) && lines(lookupIdx).pc === lookupPcSel

  // BTB lookup
  io.lookupBranch := btbHit
  io.lookupJump   := Mux(btbHit, lines(lookupIdx).jump, false.B)
  io.lookupTgt    := Cat(Mux(btbHit, lines(lookupIdx).tgt, 0.U), 0.U(ConstVal.AddrAlignLen.W))
}
