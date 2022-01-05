package treecorel2

import chisel3._
import chisel3.util._

class BEU extends Module {
  val io = IO(new Bundle {
    val isa    = Input(new ISAIO)
    val imm    = Input(new IMMIO)
    val src1   = Input(UInt(64.W))
    val src2   = Input(UInt(64.W))
    val pc     = Input(UInt(64.W))
    val branch = Output(Bool())
    val tgt    = Output(UInt(64.W))
  })

  protected val beq  = io.isa.BEQ && (io.src1 === io.src2)
  protected val bne  = io.isa.BNE && (io.src1 =/= io.src2)
  protected val bgeu = io.isa.BGEU && (io.src1 >= io.src2)
  protected val bltu = io.isa.BLTU && (io.src1 < io.src2)
  protected val bge  = io.isa.BGE && (io.src1.asSInt >= io.src2.asSInt)
  protected val blt  = io.isa.BLT && (io.src1.asSInt < io.src2.asSInt)
  protected val b    = beq | bne | bgeu | bltu | bge | blt

  protected val jal  = io.isa.JAL
  protected val jalr = io.isa.JALR

  protected val b_tgt    = io.pc + io.imm.B
  protected val jal_tgt  = io.pc + io.imm.J
  protected val jalr_tgt = io.src1 + io.imm.I

  io.branch := b | jal | jalr

  when(jal) {
    io.tgt := jal_tgt
  }.elsewhen(jalr) {
    io.tgt := jalr_tgt
  }.otherwise {
    io.tgt := b_tgt
  }
}
