package treecorel2

import chisel3._
import chisel3.util._

class BPU extends Module with InstConfig {
  // 2BP 2BC
  // Two-level adaptive predictor
  val io = IO(new Bundle {
    val branchInfo = Flipped(new BRANCHIO)
    // predictor interface
    val lookupPc  = Input(UInt(XLen.W))
    val predTaken = Output(Bool())
    val predTgt   = Output(UInt(XLen.W))
    val predIdx   = Output(UInt(GHRLen.W))
  })

  protected val ghr = Module(new GHR)
  protected val pht = Module(new PHT)
  protected val btb = Module(new BTB)

  ghr.io.branch := io.branchInfo.branch
  ghr.io.taken  := io.branchInfo.taken

  // G-share
  protected val idx = io.lookupPc(GHRLen - 1, 0) ^ ghr.io.idx
  pht.io.prevBranch := io.branchInfo.branch
  pht.io.prevTaken  := io.branchInfo.taken
  pht.io.prevIdx    := io.branchInfo.idx
  pht.io.idx        := idx

  // wire BTB
  btb.io.branch   := io.branchInfo.branch
  btb.io.jump     := io.branchInfo.jump
  btb.io.pc       := io.branchInfo.pc
  btb.io.tgt      := io.branchInfo.tgt
  btb.io.lookupPc := io.lookupPc

  // wire output signals
  // now uncond jump is always no-taken to solve 'ret' inst return 'taken'
  // when multiple sections call same function
  when(btb.io.lookupJump) {
    io.predTaken := false.B
  }.otherwise {
    io.predTaken := btb.io.lookupBranch && pht.io.taken
  }
  // io.predTaken := btb.io.lookupBranch && (pht.io.taken || btb.io.lookupJump)
  io.predTgt := btb.io.lookupTgt
  io.predIdx := idx
}
