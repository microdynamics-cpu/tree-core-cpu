package treecorel3

import chisel._
import chisel.uitl._

class MemArbiterIO extends Bundle {
  val icache = Flipped(new AXI4IO)
  val dcache = Flipped(new AXI4IO)
  val axi    = new AXI4IO
}

class MemArbiter extends Module {
  val io = IO(new MemArbiterIO)

  val eumIDLE :: eumICacheRd :: eumDCacheRd :: eumDCacheWt :: eumDCacheAck :: Nil = Enum(5)

  protected val state = RegInit(eumIDLE)

  io.axi.aw.bits     := io.dcache.aw.bits
  io.axi.aw.valid    := io.dcache.aw.valid && state === eumIDLE
  io.dcache.aw.ready := io.axi.aw.ready && state === eumIDLE
  io.icache.aw       := DontCare

  io.axi.w.bits     := io.dcache.w.bits
  io.axi.w.valid    := io.dcache.w.valid && state === eumDCacheWt
  io.dcache.w.ready := io.axi.w.ready && state === eumDCacheWt
  io.icache.w       := DontCare

  io.dcache.b.bits  := io.axi.b.bits
  io.dcache.b.valid := io.axi.b.valid && state === eumDCacheAck
  io.axi.b.ready    := io.dcache.b.ready && state === eumDCacheAck
  io.icache.b       := DontCare

  io.axi.ar.bits := AxiReadAddressChannel(
    Mux(io.dcache.ar.valid, io.dcache.ar.bits.id, io.icache.ar.bits.id),
    Mux(io.dcache.ar.valid, io.dcache.ar.bits.addr, io.icache.ar.bits.addr),
    Mux(io.dcache.ar.valid, io.dcache.ar.bits.size, io.icache.ar.bits.size),
    Mux(io.dcache.ar.valid, io.dcache.ar.bits.len, io.icache.ar.bits.len)
  )
  io.axi.ar.valid    := (io.icache.ar.valid || io.dcache.ar.valid) && !io.axi.aw.valid && state === eumIDLE
  io.dcache.ar.ready := io.axi.ar.ready && !io.axi.aw.valid && state === eumIDLE
  io.icache.ar.ready := io.dcache.ar.ready && !io.dcache.ar.valid

  // Read Data
  io.icache.r.bits  := io.axi.r.bits
  io.dcache.r.bits  := io.axi.r.bits
  io.icache.r.valid := io.axi.r.valid && state === eumICacheRd
  io.dcache.r.valid := io.axi.r.valid && state === eumDCacheRd
  io.axi.r.ready := io.icache.r.ready && state === eumICacheRd ||
    io.dcache.r.ready && state === eumDCacheRd

  switch(state) {
    is(eumIDLE) {
      when(io.dcache.aw.fire()) {
        state := eumDCacheWt
      }.elsewhen(io.dcache.ar.fire()) {
        state := eumDCacheRd
      }.elsewhen(io.icache.ar.fire()) {
        state := eumICacheRd
      }
    }
    is(eumICacheRd) {
      when(io.axi.r.fire() && io.axi.r.bits.last) {
        state := eumIDLE
      }
    }
    is(eumDCacheRd) {
      when(io.axi.r.fire() && io.axi.r.bits.last) {
        state := eumIDLE
      }
    }
    is(eumDCacheWt) {
      when(io.dcache.w.fire() && io.dcache.w.bits.last) {
        state := eumDCacheAck
      }
    }
    is(eumDCacheAck) {
      when(io.axi.b.fire()) {
        state := eumIDLE
      }
    }
  }
}

class ProcessorIO extends Bundle {
  val host = new HostIO
  val axi  = new AXI4IO
}

class Processor extends Module {
  val io               = IO(new ProcessorIO)
  protected val core   = Module(new Core)
  protected val icache = Module(new Cache)
  protected val dcache = Module(new Cache)
  protected val arb    = Module(new MemArbiter)

  io.host        <> core.io.host
  core.io.icache <> icache.io.cpu
  core.io.dcache <> dcache.io.cpu
  arb.io.icache  <> icache.io.axi
  arb.io.dcache  <> dcache.io.axi
  io.axi         <> arb.io.axi
}
