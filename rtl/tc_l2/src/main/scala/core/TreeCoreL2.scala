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

  protected val ifu = Module(new IFU)
  protected val idu = Module(new IDU)
  protected val exu = Module(new EXU)
  protected val mau = Module(new Memory)
  protected val wbu = Module(new WriteBack)

  ifu.io.socEn := io.socEn
  wbu.io.socEn := io.socEn

  // datapath
  ifu.io.if2id  <> idu.io.if2id
  idu.io.id2ex  <> exu.io.id2ex
  exu.io.ex2mem <> mau.io.ex2mem
  mau.io.mem2wb <> wbu.io.mem2wb

  exu.io.stall <> idu.io.stall
  exu.io.stall <> ifu.io.stall

  // bypass
  idu.io.wbdata    <> wbu.io.wbdata
  exu.io.bypassMem <> mau.io.bypassMem
  exu.io.bypassWb  <> wbu.io.wbdata
  exu.io.nxtPC     <> ifu.io.nxtPC
  exu.io.mtip      <> mau.io.mtip

  protected val isStall            = exu.io.stall
  protected val (tickCnt, cntWrap) = Counter(io.globalEn && isStall, 3)
  protected val stallCycle1        = isStall && (tickCnt === 0.U)
  protected val stallCycle2        = isStall && (tickCnt === 1.U)
  protected val stallCycle3        = isStall && (tickCnt === 2.U)

  ifu.io.stall := stallCycle1
  idu.io.stall := stallCycle1

  ifu.io.globalEn := io.globalEn
  idu.io.globalEn := io.globalEn
  exu.io.globalEn := Mux(stallCycle1 || stallCycle2, false.B, io.globalEn)
  mau.io.globalEn := Mux(stallCycle1 || stallCycle2, false.B, io.globalEn)
  wbu.io.globalEn := Mux(stallCycle1 || stallCycle2, false.B, io.globalEn)

  idu.io.wbdata := Mux(stallCycle1 || stallCycle2, 0.U.asTypeOf(new WBDATAIO), wbu.io.wbdata)
  ifu.io.nxtPC  := Mux(stallCycle1, exu.io.nxtPC, 0.U.asTypeOf(new NXTPCIO))

  protected val ldDataInStall = RegInit(0.U(64.W))

  when(io.globalEn) {
    when(stallCycle1) {
      ldDataInStall := io.ld.data
    }.elsewhen(stallCycle3) {
      ldDataInStall := 0.U
    }
  }

  // communicate with extern io
  io.fetch <> ifu.io.fetch

  //Even load can change machine state
  protected val lsStall = RegEnable(stallCycle1, false.B, io.globalEn) || RegEnable(stallCycle2, false.B, io.globalEn)
  io.ld.en       := mau.io.ld.en && ~lsStall
  io.ld.addr     := mau.io.ld.addr
  mau.io.ld.data := Mux(lsStall, ldDataInStall, io.ld.data)
  io.ld.size     := mau.io.ld.size

  io.sd.en   := mau.io.sd.en && ~lsStall
  io.sd.addr := mau.io.sd.addr
  io.sd.data := mau.io.sd.data
  io.sd.mask := mau.io.sd.mask

  idu.io.gpr         <> wbu.io.gpr
  io.instComm        <> wbu.io.instComm
  io.archIntRegState <> wbu.io.archIntRegState
  io.csrState        <> wbu.io.csrState
  io.trapEvt         <> wbu.io.trapEvt
  io.archFpRegState  <> wbu.io.archFpRegState
  io.archEvt         <> wbu.io.archEvt
}
