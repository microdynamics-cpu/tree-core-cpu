package treecorel3

import chisel._
import chisel.uitl._

class HostIO(implicit p: Parameters) extends Bundle {
  val fromhost = Flipped(Valid(UInt(xlen.W)))
  val tohost   = Output(UInt(xlen.W))
}

class CoreIO(implicit p: Parameters) extends Bundle {
  val host   = new HostIO
  val icache = Flipped((new CacheIO))
  val dcache = Flipped((new CacheIO))
}

class Core(implicit val p: Parameters) extends Module {
  val io    = IO(new CoreIO)
  val dpath = Module(new DataPath)
  val ctrl  = Module(new Control)

  io.icache <> dpath.io.icache
  io.dcache <> dpath.io.dcache
  ctrl.io   <> dpath.io.ctrl
}
