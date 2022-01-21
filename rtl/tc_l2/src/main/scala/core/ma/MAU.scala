package treecorel2

import chisel3._
import chisel3.util._

class MAU extends Module {
  val io = IO(new Bundle {
    val globalEn  = Input(Bool())
    val ex2mem    = Flipped(new EX2MEMIO)
    val mem2wb    = new MEM2WBIO
    val ld        = new LDIO
    val sd        = new SDIO
    val bypassMem = new WBDATAIO
    val mtip      = Output(Bool())
  })

  protected val memReg = RegEnable(io.ex2mem, WireInit(0.U.asTypeOf(new EX2MEMIO())), io.globalEn)
  protected val valid  = memReg.valid
  protected val inst   = memReg.inst
  protected val isa    = memReg.isa
  protected val imm    = memReg.imm
  protected val src1   = memReg.src1
  protected val src2   = memReg.src2
  protected val csr    = memReg.csr

  protected val lsu = Module(new LSU)
  lsu.io.valid := valid
  lsu.io.isa   := isa
  lsu.io.src1  := src1
  lsu.io.src2  := src2
  lsu.io.imm   := imm
  protected val ldData = lsu.io.ldData

  protected val clint = Module(new CLINT)
  clint.io.valid := valid
  clint.io.cld   <> lsu.io.ld
  clint.io.csd   <> lsu.io.sd
  io.ld          <> clint.io.ld
  io.sd          <> clint.io.sd

  protected val isLoad    = lsu.io.ld.en
  protected val memWbdata = memReg.aluRes | memReg.link | memReg.auipc | memReg.csrData

  // bypass path
  io.bypassMem.wen   := Mux(isLoad, true.B, memReg.wen) && valid
  io.bypassMem.wdest := Mux(isLoad, memReg.wdest, memReg.wdest)
  io.bypassMem.data  := Mux(isLoad, ldData, memWbdata)

  io.mem2wb.valid      := valid
  io.mem2wb.inst       := inst
  io.mem2wb.pc         := memReg.pc
  io.mem2wb.branIdx    := memReg.branIdx
  io.mem2wb.predTaken  := memReg.predTaken
  io.mem2wb.isa        := isa
  io.mem2wb.src1       := src1
  io.mem2wb.src2       := src2
  io.mem2wb.imm        := imm
  io.mem2wb.wen        := memReg.wen
  io.mem2wb.wdest      := memReg.wdest
  io.mem2wb.aluRes     := memReg.aluRes
  io.mem2wb.branch     := memReg.branch
  io.mem2wb.tgt        := memReg.tgt
  io.mem2wb.link       := memReg.link
  io.mem2wb.auipc      := memReg.auipc
  io.mem2wb.csrData    := memReg.csrData
  io.mem2wb.ldData     := ldData
  io.mem2wb.cvalid     := clint.io.cvalid
  io.mem2wb.timeIntrEn := memReg.timeIntrEn
  io.mem2wb.ecallEn    := memReg.ecallEn
  io.mem2wb.csr        := memReg.csr
  io.mtip              := clint.io.mtip
}
