package treecorel2

import chisel3._
import chisel3.util._
import difftest._

class TreeCoreL2 extends Module with InstConfig {
  val io = IO(new Bundle {
    val globalEn = Input(Bool())
    val socEn    = Input(Bool())
    val fetch    = new IFIO
    val ld       = new LDIO
    val sd       = new SDIO
  })

  protected val ifu = Module(new IFU)
  protected val idu = Module(new IDU)
  protected val exu = Module(new EXU)
  protected val mau = Module(new MAU)
  protected val wbu = Module(new WBU)

  // 1. switch base addr
  // 2. switch difftest access
  ifu.io.socEn := io.socEn
  wbu.io.socEn := io.socEn

  // datapath
  ifu.io.if2id  <> idu.io.if2id
  idu.io.id2ex  <> exu.io.id2ex
  exu.io.ex2mem <> mau.io.ex2mem
  mau.io.mem2wb <> wbu.io.mem2wb
  // stall signal
  exu.io.stall <> idu.io.stall
  exu.io.stall <> ifu.io.stall
  // branch prediction
  ifu.io.branchInfo <> exu.io.branchInfo
  // bypass
  idu.io.wbdata    <> wbu.io.wbdata
  exu.io.bypassMem <> mau.io.bypassMem
  exu.io.bypassWb  <> wbu.io.wbdata
  // misc
  idu.io.gpr   <> wbu.io.gpr
  exu.io.nxtPC <> ifu.io.nxtPC
  exu.io.mtip  <> mau.io.mtip

  // stall
  protected val isStall            = exu.io.stall
  protected val (tickCnt, cntWrap) = Counter(io.globalEn && isStall, 3)
  protected val cyc1               = isStall && (tickCnt === 0.U)
  protected val cyc2               = isStall && (tickCnt === 1.U)
  protected val cyc3               = isStall && (tickCnt === 2.U)

  ifu.io.stall    := cyc1
  idu.io.stall    := cyc1
  ifu.io.globalEn := io.globalEn
  idu.io.globalEn := io.globalEn
  exu.io.globalEn := Mux(cyc1 || cyc2, false.B, io.globalEn)
  mau.io.globalEn := Mux(cyc1 || cyc2, false.B, io.globalEn)
  wbu.io.globalEn := Mux(cyc1 || cyc2, false.B, io.globalEn)
  idu.io.wbdata   := Mux(cyc1 || cyc2, 0.U.asTypeOf(new WBDATAIO), wbu.io.wbdata)
  ifu.io.nxtPC    := Mux(cyc1, exu.io.nxtPC, 0.U.asTypeOf(new NXTPCIO))

  // special judge
  protected val lsStall   = RegEnable(cyc1, false.B, io.globalEn) || RegEnable(cyc2, false.B, io.globalEn)
  protected val ldDataReg = RegInit(0.U(64.W))

  when(io.globalEn) {
    when(cyc1) {
      ldDataReg := io.ld.data
    }.elsewhen(cyc3) {
      ldDataReg := 0.U
    }
  }

  // communicate with extern io
  io.fetch       <> ifu.io.fetch
  io.ld.en       := mau.io.ld.en && ~lsStall
  io.ld.addr     := mau.io.ld.addr
  mau.io.ld.data := Mux(lsStall, ldDataReg, io.ld.data)
  io.ld.size     := mau.io.ld.size
  io.sd.en       := mau.io.sd.en && ~lsStall
  io.sd.addr     := mau.io.sd.addr
  io.sd.data     := mau.io.sd.data
  io.sd.mask     := mau.io.sd.mask
}
