package sim

import chisel3._
import chisel3.util._

import difftest._
import treecorel2._

class Processor extends Module {
  val io = IO(new Bundle {
    val runEn           = Input(Bool())
    val socEn           = Input(Bool())
    val dxchg           = new DXCHGIO
    val instComm        = Flipped(new DiffInstrCommitIO)
    val archIntRegState = Flipped(new DiffArchIntRegStateIO)
    val csrState        = Flipped(new DiffCSRStateIO)
    val trapEvt         = Flipped(new DiffTrapEventIO)
    val archFpRegState  = Flipped(new DiffArchFpRegStateIO)
    val archEvt         = Flipped(new DiffArchEventIO)
  })

  protected val cpu      = Module(new TreeCoreL2)
  protected val crossbar = Module(new Crossbar)

  cpu.io.socEn      := io.socEn
  crossbar.io.runEn := io.runEn
  crossbar.io.socEn := io.socEn

  cpu.io.globalEn   <> crossbar.io.core.globalEn
  cpu.io.fetch      <> crossbar.io.core.fetch
  cpu.io.ld         <> crossbar.io.core.ld
  cpu.io.sd         <> crossbar.io.core.sd
  crossbar.io.dxchg <> io.dxchg

  cpu.io.instComm        <> io.instComm
  cpu.io.archIntRegState <> io.archIntRegState
  cpu.io.csrState        <> io.csrState
  cpu.io.trapEvt         <> io.trapEvt
  cpu.io.archFpRegState  <> io.archFpRegState
  cpu.io.archEvt         <> io.archEvt
}
