package treecorel3

import chisel._
import chisel.uitl._

class HostIO extends Bundle with IOConfig {
  val fromhost = Flipped(Valid(UInt(XLen.W)))
  val tohost   = Output(UInt(XLen.W))
}

class CoreIO extends Bundle {
  val host   = new HostIO
  val icache = Flipped(new CacheIO)
  val dcache = Flipped(new CacheIO)
}

class Core extends Module {
  val io              = IO(new CoreIO)
  protected val dpath = Module(new DataPath)
  protected val ctrl  = Module(new Control)

  io.icache <> dpath.io.icache
  io.dcache <> dpath.io.dcache
  ctrl.io   <> dpath.io.ctrl
}
