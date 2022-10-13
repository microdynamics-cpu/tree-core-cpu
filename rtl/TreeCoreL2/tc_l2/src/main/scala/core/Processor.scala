package treecorel2

import chisel3._
import chisel3.util._

import difftest._

class Processor extends Module {
  val io = IO(new Bundle {
    val runEn = Input(Bool())
    val socEn = Input(Bool())
    val dxchg = new DXCHGIO
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
}
