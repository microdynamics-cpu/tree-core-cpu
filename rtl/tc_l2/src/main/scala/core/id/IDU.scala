package treecorel2

import chisel3._
import chisel3.util._

class IDU extends Module {
  val io = IO(new Bundle {
    val globalEn = Input(Bool())
    val stall    = Input(Bool())
    val if2id    = Flipped(new IF2IDIO)
    val wbdata   = Flipped(new WBDATAIO)
    val id2ex    = new ID2EXIO
    val gpr      = Output(Vec(32, UInt(64.W)))
  })

  protected val idReg = RegEnable(io.if2id, WireInit(0.U.asTypeOf(new IF2IDIO())), io.globalEn)
  protected val valid = idReg.valid
  protected val inst  = idReg.inst
  protected val pc    = idReg.pc

  protected val rs1   = inst(19, 15)
  protected val rs2   = inst(24, 20)
  protected val wdest = inst(11, 7)

  protected val decoder = Module(new ISADecoder)
  decoder.io.inst := inst
  protected val isa = decoder.io.isa
  protected val imm = decoder.io.imm
  protected val wen = decoder.io.wen

  protected val regfile = new RegFile
  protected val src1En  = io.wbdata.wen && (rs1 === io.wbdata.wdest) && (rs1 =/= 0.U)
  protected val src2En  = io.wbdata.wen && (rs2 === io.wbdata.wdest) && (rs2 =/= 0.U)
  protected val src1    = Mux(src1En, io.wbdata.data, regfile.read(rs1))
  protected val src2    = Mux(src2En, io.wbdata.data, regfile.read(rs2))

  when(io.globalEn) {
    regfile.write(io.wbdata.wen, io.wbdata.wdest, io.wbdata.data)
  }

  io.id2ex.valid := Mux(io.stall, false.B, valid)
  io.id2ex.inst  := inst
  io.id2ex.pc    := pc
  io.id2ex.isa   := isa
  io.id2ex.src1  := src1
  io.id2ex.src2  := src2
  io.id2ex.imm   := imm
  io.id2ex.wen   := wen
  io.id2ex.wdest := wdest
  io.gpr         := regfile.gpr
}
