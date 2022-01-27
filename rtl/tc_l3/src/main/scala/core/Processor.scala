package treecorel3

import chisel._
import chisel.uitl._

class MemArbiterIO(implicit val p: Parameters) extends Bundle {
  val icache = Flipped(new AxiIO)
  val dcache = Flipped(new AxiIO)
  val axi  = new AxiIO
}

class MemArbiter(implicit p: Parameters) extends Module {
  val io = IO(new MemArbiterIO)

  val s_IDLE :: s_ICACHE_READ :: s_DCACHE_READ :: s_DCACHE_WRITE :: s_DCACHE_ACK :: Nil = Enum(5)
  val state                                                                             = RegInit(s_IDLE)

  // write address
  io.axi.aw.bits   := io.dcache.aw.bits
  io.axi.aw.valid  := io.dcache.aw.valid && state === s_IDLE
  io.dcache.aw.ready := io.axi.aw.ready  && state === s_IDLE
  io.icache.aw       := DontCare

  // write data
  io.axi.w.bits   := io.dcache.w.bits
  io.axi.w.valid  := io.dcache.w.valid && state === s_DCACHE_WRITE
  io.dcache.w.ready := io.axi.w.ready  && state === s_DCACHE_WRITE
  io.icache.w       := DontCare

  // write ack
  io.dcache.b.bits  := io.axi.b.bits
  io.dcache.b.valid := io.axi.b.valid  && state === s_DCACHE_ACK
  io.axi.b.ready  := io.dcache.b.ready && state === s_DCACHE_ACK
  io.icache.b       := DontCare

  // Read Address
  io.axi.ar.bits := AxiReadAddressChannel(
    Mux(io.dcache.ar.valid, io.dcache.ar.bits.id, io.icache.ar.bits.id),
    Mux(io.dcache.ar.valid, io.dcache.ar.bits.addr, io.icache.ar.bits.addr),
    Mux(io.dcache.ar.valid, io.dcache.ar.bits.size, io.icache.ar.bits.size),
    Mux(io.dcache.ar.valid, io.dcache.ar.bits.len, io.icache.ar.bits.len)
  )
  io.axi.ar.valid    := (io.icache.ar.valid || io.dcache.ar.valid) &&
    !io.axi.aw.valid && state              === s_IDLE
  io.dcache.ar.ready   := io.axi.ar.ready   && !io.axi.aw.valid  && state === s_IDLE
  io.icache.ar.ready   := io.dcache.ar.ready  && !io.dcache.ar.valid

  // Read Data
  io.icache.r.bits  := io.axi.r.bits
  io.dcache.r.bits  := io.axi.r.bits
  io.icache.r.valid := io.axi.r.valid && state === s_ICACHE_READ
  io.dcache.r.valid := io.axi.r.valid && state === s_DCACHE_READ
  io.axi.r.ready    := io.icache.r.ready && state === s_ICACHE_READ ||
    io.dcache.r.ready && state            === s_DCACHE_READ

  switch(state) {
    is(s_IDLE) {
      when(io.dcache.aw.fire()) {
        state := s_DCACHE_WRITE
      }.elsewhen(io.dcache.ar.fire()) {
        state := s_DCACHE_READ
      }.elsewhen(io.icache.ar.fire()) {
        state := s_ICACHE_READ
      }
    }
    is(s_ICACHE_READ) {
      when(io.axi.r.fire() && io.axi.r.bits.last) {
        state := s_IDLE
      }
    }
    is(s_DCACHE_READ) {
      when(io.axi.r.fire() && io.axi.r.bits.last) {
        state := s_IDLE
      }
    }
    is(s_DCACHE_WRITE) {
      when(io.dcache.w.fire() && io.dcache.w.bits.last) {
        state := s_DCACHE_ACK
      }
    }
    is(s_DCACHE_ACK) {
      when(io.axi.b.fire()) {
        state := s_IDLE
      }
    }
  }
}

class ProcessorIO(implicit val p: Parameters) extends Bundle {
  val host  = new HostIO
  val axi = new AxiIO
}

class Processor(implicit p: Parameters) extends Module {
  val io     = IO(new ProcessorIO)
  val core   = Module(new Core)
  val icache = Module(new Cache)
  val dcache = Module(new Cache)
  val arb    = Module(new MemArbiter)

  io.host        <> core.io.host
  core.io.icache <> icache.io.cpu
  core.io.dcache <> dcache.io.cpu
  arb.io.icache  <> icache.io.axi
  arb.io.dcache  <> dcache.io.axi
  io.axi       <> arb.io.axi
}
