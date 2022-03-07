package treecorel2

import chisel3._
import chisel3.util._

class BTBLine extends Bundle with IOConfig {
  val pc   = UInt(XLen.W)
  val tgt  = UInt(XLen.W)
  val jump = Bool()
}

class BTB extends Module with InstConfig {
  val io = IO(new Bundle {
    // branch info (from idu)
    val branch = Input(Bool())
    val jump   = Input(Bool())
    val pc     = Input(UInt(XLen.W))
    val tgt    = Input(UInt(XLen.W))
    // BTB lookup interface
    val lookupBranch = Output(Bool())
    val lookupJump   = Output(Bool())
    val lookupPc     = Input(UInt(XLen.W))
    val lookupTgt    = Output(UInt(XLen.W))
  })

  // definitions of BTB lines and valid bits
  protected val valids = RegInit(VecInit(Seq.fill(BTBSize) { false.B }))
  protected val lines  = Mem(BTBSize, new BTBLine)

  // branch info for BTB lines
  protected val idx = io.pc(BTBIdxLen - 1, 0)
  // write to BTB lines
  when(io.branch) {
    valids(idx)     := true.B
    lines(idx).jump := io.jump
    lines(idx).pc   := io.pc
    lines(idx).tgt  := io.tgt
  }

  // signals about BTB lookup
  protected val lookupIdx   = io.lookupPc(BTBIdxLen - 1, 0)
  protected val lookupPcSel = io.lookupPc
  protected val btbHit      = valids(lookupIdx) && lines(lookupIdx).pc === lookupPcSel

  // BTB lookup
  io.lookupBranch := btbHit
  io.lookupJump   := Mux(btbHit, lines(lookupIdx).jump, false.B)
  io.lookupTgt    := Mux(btbHit, lines(lookupIdx).tgt, 0.U(64.W))
}
