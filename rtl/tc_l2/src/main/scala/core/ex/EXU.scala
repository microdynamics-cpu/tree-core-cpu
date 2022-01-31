package treecorel2

import chisel3._
import chisel3.util._

class EXU extends Module with InstConfig {
  val io = IO(new Bundle {
    val globalEn   = Input(Bool())
    val mtip       = Input(Bool())
    val stall      = Output(Bool())
    val id2ex      = Flipped(new ID2EXIO)
    val bypassMem  = Flipped(new WBDATAIO)
    val bypassWb   = Flipped(new WBDATAIO)
    val ex2mem     = new EX2MEMIO
    val nxtPC      = new NXTPCIO
    val branchInfo = new BRANCHIO
  })

  protected val exReg     = RegEnable(io.id2ex, WireInit(0.U.asTypeOf(new ID2EXIO())), io.globalEn)
  protected val valid     = exReg.valid
  protected val inst      = exReg.inst
  protected val pc        = exReg.pc
  protected val branIdx   = exReg.branIdx
  protected val predTaken = exReg.predTaken
  protected val isa       = exReg.isa
  protected val imm       = exReg.imm
  protected val wen       = exReg.wen
  protected val rs1       = exReg.inst(19, 15)
  protected val rs2       = exReg.inst(24, 20)
  protected val wdest     = exReg.wdest

  // handle bypass signal
  protected val bypassMemSrc1En = io.bypassMem.wen && (rs1 === io.bypassMem.wdest) && (rs1 =/= 0.U)
  protected val bypassMemSrc2En = io.bypassMem.wen && (rs2 === io.bypassMem.wdest) && (rs2 =/= 0.U)
  protected val bypassWbSrc1En  = io.bypassWb.wen  && (rs1 === io.bypassWb.wdest)  && (rs1 =/= 0.U)
  protected val bypassWbSrc2En  = io.bypassWb.wen  && (rs2 === io.bypassWb.wdest)  && (rs2 =/= 0.U)
  protected val src1            = Mux(bypassMemSrc1En, io.bypassMem.data, Mux(bypassWbSrc1En, io.bypassWb.data, exReg.src1))
  protected val src2            = Mux(bypassMemSrc2En, io.bypassMem.data, Mux(bypassWbSrc2En, io.bypassWb.data, exReg.src2))

  protected val alu = Module(new ALU)
  alu.io.isa  := isa
  alu.io.imm  := imm
  alu.io.src1 := src1
  alu.io.src2 := src2
  protected val aluRes = alu.io.res

  // protected val mdu = Module(new MDU)
  // mdu.io.isa  := isa
  // mdu.io.src1 := src1
  // mdu.io.src2 := src2
  // protected val mduRes = mdu.io.res

  // protected val agu = Module(new AGU)
  // agu.io.isa  := isa
  // agu.io.src1 := src1
  // agu.io.src2 := src2
  // protected val aguRes = agu.io.res

  protected val beu = Module(new BEU)
  beu.io.isa        := isa
  beu.io.imm        := imm
  beu.io.src1       := src1
  beu.io.src2       := src2
  beu.io.pc         := pc
  beu.io.branIdx    := branIdx
  beu.io.branchInfo <> io.branchInfo
  protected val branch = beu.io.branch
  protected val tgt    = beu.io.tgt

  protected val link  = SignExt(((isa === instJAL) | (isa === instJALR)).asUInt, 64) & (pc + 4.U)
  protected val auipc = SignExt((isa === instAUIPC).asUInt, 64) & (pc + imm)

  protected val csrReg     = Module(new CSRReg)
  protected val csrData    = csrReg.io.data
  protected val timeIntrEn = csrReg.io.timeIntrEn
  protected val ecallEn    = csrReg.io.ecallEn
  csrReg.io.globalEn := io.globalEn
  csrReg.io.pc       := pc
  csrReg.io.inst     := Mux(valid, inst, NOPInst)
  csrReg.io.src      := src1
  csrReg.io.mtip     := io.mtip

  io.nxtPC.trap  := valid && (timeIntrEn || ecallEn)
  io.nxtPC.mtvec := csrReg.io.csrState.mtvec
  io.nxtPC.mret  := valid && (isa       === instMRET)
  io.nxtPC.mepc  := csrReg.io.csrState.mepc
  // (pred, fact)--->(NT, T) or (T, NT)
  protected val predNTfactT = branch  && !predTaken
  protected val predTfactNT = !branch && predTaken
  io.nxtPC.branch := valid && (predNTfactT     || predTfactNT)
  io.nxtPC.tgt    := Mux(valid && predNTfactT, tgt, Mux(valid && predTfactNT, pc + 4.U, 0.U(XLen.W)))
  io.stall        := valid && (io.nxtPC.branch || timeIntrEn || ecallEn || (isa === instMRET))

  io.ex2mem.valid        := Mux(timeIntrEn, false.B, valid)
  io.ex2mem.inst         := inst
  io.ex2mem.pc           := pc
  io.ex2mem.branIdx      := branIdx
  io.ex2mem.predTaken    := predTaken
  io.ex2mem.isa          := isa
  io.ex2mem.src1         := src1
  io.ex2mem.src2         := src2
  io.ex2mem.imm          := imm
  io.ex2mem.wen          := wen
  io.ex2mem.wdest        := wdest
  io.ex2mem.aluRes       := aluRes
  io.ex2mem.branch       := branch
  io.ex2mem.tgt          := tgt
  io.ex2mem.link         := link
  io.ex2mem.auipc        := auipc
  io.ex2mem.csrData      := csrData
  io.ex2mem.timeIntrEn   := timeIntrEn
  io.ex2mem.ecallEn      := ecallEn
  io.ex2mem.csr.mstatus  := csrReg.io.csrState.mstatus
  io.ex2mem.csr.mcause   := csrReg.io.csrState.mcause
  io.ex2mem.csr.mepc     := csrReg.io.csrState.mepc
  io.ex2mem.csr.mie      := csrReg.io.csrState.mie
  io.ex2mem.csr.mscratch := csrReg.io.csrState.mscratch
  io.ex2mem.csr.medeleg  := csrReg.io.csrState.medeleg
  io.ex2mem.csr.mtvec    := csrReg.io.csrState.mtvec
  io.ex2mem.csr.mhartid  := 0.U
  io.ex2mem.csr.mcycle   := 0.U
  io.ex2mem.csr.mip      := 0.U
}
