package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.{ConstVal, InstConfig}

class BEU extends Module with InstConfig {
  val io = IO(new Bundle {
    val isa        = Input(new ISAIO)
    val imm        = Input(UInt(XLen.W))
    val src1       = Input(UInt(XLen.W))
    val src2       = Input(UInt(XLen.W))
    val pc         = Input(UInt(XLen.W))
    val branIdx    = Input(UInt(ConstVal.GHRLen.W))
    val branchInfo = new BRANCHIO
    val branch     = Output(Bool())
    val tgt        = Output(UInt(XLen.W))
  })

  protected val beq   = io.isa.BEQ && (io.src1 === io.src2)
  protected val bne   = io.isa.BNE && (io.src1 =/= io.src2)
  protected val bgeu  = io.isa.BGEU && (io.src1 >= io.src2)
  protected val bltu  = io.isa.BLTU && (io.src1 < io.src2)
  protected val bge   = io.isa.BGE && (io.src1.asSInt >= io.src2.asSInt)
  protected val blt   = io.isa.BLT && (io.src1.asSInt < io.src2.asSInt)
  protected val b     = beq | bne | bgeu | bltu | bge | blt
  protected val bInst = io.isa.BEQ || io.isa.BNE || io.isa.BGEU || io.isa.BLTU || io.isa.BGE || io.isa.BLT || io.isa.JAL || io.isa.JALR

  protected val jal  = io.isa.JAL
  protected val jalr = io.isa.JALR

  protected val b_tgt    = io.pc + io.imm
  protected val jal_tgt  = io.pc + io.imm
  protected val jalr_tgt = io.src1 + io.imm

  io.branch := b | jal | jalr

  when(jal) {
    io.tgt := jal_tgt
  }.elsewhen(jalr) {
    io.tgt := jalr_tgt
  }.otherwise {
    io.tgt := b_tgt
  }

  io.branchInfo.branch := bInst
  io.branchInfo.jump   := jal || jalr
  io.branchInfo.taken  := io.branch
  io.branchInfo.idx    := io.branIdx
  io.branchInfo.pc     := io.pc
  io.branchInfo.tgt    := io.tgt
}
