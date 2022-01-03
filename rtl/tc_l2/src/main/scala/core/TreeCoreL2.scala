package treecorel2

import chisel3._
import chisel3.util._
import difftest._

class TreeCoreL2 extends Module {
  val io = IO(new Bundle {
    val globalEn = Input(Bool())
    val socEn    = Input(Bool())
    val fetch    = new IFIO
    val ld       = new LDIO
    val sd       = new SDIO

    // difftest
    val instComm        = Flipped(new DiffInstrCommitIO)
    val archIntRegState = Flipped(new DiffArchIntRegStateIO)
    val csrState        = Flipped(new DiffCSRStateIO)
    val trapEvt         = Flipped(new DiffTrapEventIO)
    val archFpRegState  = Flipped(new DiffArchFpRegStateIO)
    val archEvt         = Flipped(new DiffArchEventIO)
  })

  protected val ifUnit   = Module(new InstFetch)
  protected val idUnit   = Module(new InstDecode)
  protected val execUnit = Module(new Execute)
  protected val maUnit   = Module(new Memory)
  protected val wbUnit   = Module(new WriteBack)

  ifUnit.io.socEn := io.socEn
  wbUnit.io.socEn := io.socEn

  // datapath
  ifUnit.io.if2id    <> idUnit.io.if2id
  idUnit.io.id2ex    <> execUnit.io.id2ex
  execUnit.io.ex2mem <> maUnit.io.ex2mem
  maUnit.io.mem2wb   <> wbUnit.io.mem2wb

  execUnit.io.stall <> idUnit.io.stall
  execUnit.io.stall <> ifUnit.io.stall

  // bypass
  idUnit.io.wbdata      <> wbUnit.io.wbdata
  execUnit.io.bypassMem <> maUnit.io.bypassMem
  execUnit.io.bypassWb  <> wbUnit.io.wbdata
  execUnit.io.nxtPC     <> ifUnit.io.nxtPC
  execUnit.io.mtip      <> maUnit.io.mtip

  protected val isStall            = execUnit.io.stall
  protected val (tickCnt, cntWrap) = Counter(io.globalEn && isStall, 3)
  protected val stallCycle1        = isStall && (tickCnt === 0.U)
  protected val stallCycle2        = isStall && (tickCnt === 1.U)
  protected val stallCycle3        = isStall && (tickCnt === 2.U)

  ifUnit.io.stall := stallCycle1
  idUnit.io.stall := stallCycle1

  ifUnit.io.globalEn   := io.globalEn
  idUnit.io.globalEn   := io.globalEn
  execUnit.io.globalEn := Mux(stallCycle1 || stallCycle2, false.B, io.globalEn)
  maUnit.io.globalEn   := Mux(stallCycle1 || stallCycle2, false.B, io.globalEn)
  wbUnit.io.globalEn   := Mux(stallCycle1 || stallCycle2, false.B, io.globalEn)

  idUnit.io.wbdata := Mux(stallCycle1 || stallCycle2, 0.U.asTypeOf(new WBDATAIO), wbUnit.io.wbdata)
  ifUnit.io.nxtPC  := Mux(stallCycle1, execUnit.io.nxtPC, 0.U.asTypeOf(new NXTPCIO))

  protected val ldDataInStall = RegInit(0.U(64.W))

  when(io.globalEn) {
    when(stallCycle1) {
      ldDataInStall := io.ld.data
    }.elsewhen(stallCycle3) {
      ldDataInStall := 0.U
    }
  }

  // communicate with extern io
  io.fetch <> ifUnit.io.fetch

  //Even load can change machine state
  protected val lsStall = RegEnable(stallCycle1, false.B, io.globalEn) || RegEnable(stallCycle2, false.B, io.globalEn)
  io.ld.en          := maUnit.io.ld.en && ~lsStall
  io.ld.addr        := maUnit.io.ld.addr
  maUnit.io.ld.data := Mux(lsStall, ldDataInStall, io.ld.data)
  io.ld.size        := maUnit.io.ld.size

  io.sd.en   := maUnit.io.sd.en && ~lsStall
  io.sd.addr := maUnit.io.sd.addr
  io.sd.data := maUnit.io.sd.data
  io.sd.mask := maUnit.io.sd.mask

  idUnit.io.gpr      <> wbUnit.io.gpr
  io.instComm        <> wbUnit.io.instComm
  io.archIntRegState <> wbUnit.io.archIntRegState
  io.csrState        <> wbUnit.io.csrState
  io.trapEvt         <> wbUnit.io.trapEvt
  io.archFpRegState  <> wbUnit.io.archFpRegState
  io.archEvt         <> wbUnit.io.archEvt
}
