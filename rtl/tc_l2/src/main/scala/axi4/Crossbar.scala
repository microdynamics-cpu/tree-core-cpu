package treecorel2

import chisel3._
import chisel3.util._

class Crossbar extends Module {
  val io = IO(new Bundle {
    val socEn = Input(Bool())
    val runEn = Input(Bool())
    val dxchg = new DXCHGIO
    val core  = new COREIO
  })

  protected val globalEn = RegInit(false.B)
  protected val inst     = RegInit(0.U(32.W))
  protected val rdInst   = Mux(io.core.fetch.addr(2).asBool(), io.dxchg.rdata(63, 32), io.dxchg.rdata(31, 0))

  io.core.globalEn   := Mux(io.runEn, globalEn, false.B)
  io.core.fetch.data := inst
  io.core.ld.data    := io.dxchg.rdata

  // FSM for inst or mem data xform
  protected val eumInst :: eumMem :: Nil = Enum(2)
  protected val stateReg                 = RegInit(eumInst)

  switch(stateReg) {
    is(eumInst) {
      when(io.runEn) {
        stateReg := eumMem
        globalEn := true.B
        inst     := rdInst
      }
    }
    is(eumMem) {
      when(io.runEn) {
        stateReg := eumInst
        globalEn := false.B
        inst     := 0x13.U
      }
    }
  }

  protected val instSize = Mux(io.socEn, 2.U, 3.U)
  // because the difftest's logic addr is 0x000000
  protected val addrOffset = Mux(io.socEn, "h0000000000000000".U(64.W), "h0000000080000000".U(64.W))

  protected val instAddr  = io.core.fetch.addr - addrOffset
  protected val loadAddr  = io.core.ld.addr - addrOffset
  protected val storeAddr = io.core.sd.addr - addrOffset
  protected val maEn      = io.core.ld.en || io.core.sd.en

  io.dxchg.clk   := clock
  io.dxchg.ren   := ((stateReg === eumInst) || (stateReg === eumMem && maEn))
  io.dxchg.raddr := Mux(stateReg === eumInst, instAddr, loadAddr)
  io.dxchg.rsize := Mux(stateReg === eumMem && io.core.ld.en, io.core.ld.size, instSize)
  io.dxchg.waddr := storeAddr
  io.dxchg.wdata := io.core.sd.data
  io.dxchg.wmask := io.core.sd.mask
  io.dxchg.wen   := stateReg === eumMem && io.core.sd.en
}
