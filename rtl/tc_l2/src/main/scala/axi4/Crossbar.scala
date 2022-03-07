package treecorel2

import chisel3._
import chisel3.util._

class Crossbar extends Module with InstConfig {
  val io = IO(new Bundle {
    val socEn = Input(Bool())
    val runEn = Input(Bool())
    val dxchg = new DXCHGIO
    val core  = new COREIO
  })

  protected val globalEn = RegInit(false.B)
  protected val inst     = RegInit(0.U(InstLen.W))
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
        globalEn := true.B
        stateReg := eumMem
        inst     := rdInst
      }
    }
    is(eumMem) {
      when(io.runEn) {
        globalEn := false.B
        stateReg := eumInst
        inst     := NOPInst
      }
    }
  }

  // because the difftest's logic addr is 0x000000
  protected val instSize = Mux(io.socEn, InstSoCRSize, InstDiffRSize)
  protected val baseAddr = Mux(io.socEn, SoCStartBaseAddr, DiffStartBaseAddr)
  protected val instAddr = io.core.fetch.addr - baseAddr
  protected val ldAddr   = io.core.ld.addr - baseAddr
  protected val sdAddr   = io.core.sd.addr - baseAddr
  protected val maEn     = io.core.ld.en || io.core.sd.en

  // prepare the data exchange io signals
  io.dxchg.ren   := ((stateReg === eumInst) || (stateReg === eumMem && maEn))
  io.dxchg.raddr := Mux(stateReg === eumInst, instAddr, ldAddr)
  io.dxchg.rsize := Mux(stateReg === eumMem && io.core.ld.en, io.core.ld.size, instSize)
  io.dxchg.wen   := stateReg   === eumMem   && io.core.sd.en
  io.dxchg.waddr := sdAddr
  io.dxchg.wdata := io.core.sd.data
  io.dxchg.wmask := io.core.sd.mask
}
