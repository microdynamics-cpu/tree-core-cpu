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

  // stall control
  protected val stallCtrl = Module(new StallControl)
  stallCtrl.io.globalEn := io.globalEn
  stallCtrl.io.stall    := exu.io.stall

  ifu.io.stall    := stallCtrl.io.st1
  idu.io.stall    := stallCtrl.io.st1
  ifu.io.globalEn := io.globalEn
  idu.io.globalEn := io.globalEn
  exu.io.globalEn := Mux(stallCtrl.io.st1 || stallCtrl.io.st2, false.B, io.globalEn)
  mau.io.globalEn := Mux(stallCtrl.io.st1 || stallCtrl.io.st2, false.B, io.globalEn)
  wbu.io.globalEn := Mux(stallCtrl.io.st1 || stallCtrl.io.st2, false.B, io.globalEn)
  idu.io.wbdata   := Mux(stallCtrl.io.st1 || stallCtrl.io.st2, 0.U.asTypeOf(new WBDATAIO), wbu.io.wbdata)
  ifu.io.nxtPC    := Mux(stallCtrl.io.st1, exu.io.nxtPC, 0.U.asTypeOf(new NXTPCIO))

  // special judge
  protected val lsStall   = RegEnable(stallCtrl.io.st1, false.B, io.globalEn) || RegEnable(stallCtrl.io.st2, false.B, io.globalEn)
  protected val ldDataReg = RegInit(0.U(XLen.W))

  when(io.globalEn) {
    when(stallCtrl.io.st1) {
      ldDataReg := io.ld.data
    }.elsewhen(stallCtrl.io.st3) {
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
