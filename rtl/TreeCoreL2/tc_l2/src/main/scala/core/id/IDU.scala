package treecorel2

import chisel3._
import chisel3.util._

class IDU extends Module with InstConfig {
  val io = IO(new Bundle {
    val globalEn = Input(Bool())
    val stall    = Input(Bool())
    val if2id    = Flipped(new IF2IDIO)
    val wbdata   = Flipped(new WBDATAIO)
    val id2ex    = new ID2EXIO
    val gpr      = Output(Vec(RegfileNum, UInt(XLen.W)))
  })

  protected val idReg = RegEnable(io.if2id, WireInit(0.U.asTypeOf(new IF2IDIO())), io.globalEn)
  protected val inst  = idReg.inst
  protected val rs1   = inst(19, 15)
  protected val rs2   = inst(24, 20)
  protected val wdest = inst(11, 7)

  protected val decoder = Module(new ISADecoder)
  decoder.io.inst := inst

  protected val regfile = new RegFile
  protected val src1En  = io.wbdata.wen && (rs1 === io.wbdata.wdest) && (rs1 =/= 0.U)
  protected val src2En  = io.wbdata.wen && (rs2 === io.wbdata.wdest) && (rs2 =/= 0.U)
  protected val src1    = Mux(src1En, io.wbdata.data, regfile.read(rs1))
  protected val src2    = Mux(src2En, io.wbdata.data, regfile.read(rs2))

  when(io.globalEn) {
    regfile.write(io.wbdata.wen, io.wbdata.wdest, io.wbdata.data)
  }

  io.id2ex.valid     := Mux(io.stall, false.B, idReg.valid)
  io.id2ex.inst      := inst
  io.id2ex.pc        := idReg.pc
  io.id2ex.branIdx   := idReg.branIdx
  io.id2ex.predTaken := idReg.predTaken
  io.id2ex.src1      := src1
  io.id2ex.src2      := src2
  io.id2ex.isa       := decoder.io.isa
  io.id2ex.imm       := decoder.io.imm
  io.id2ex.wen       := decoder.io.wen
  io.id2ex.wdest     := wdest
  io.gpr             := regfile.gpr
}
