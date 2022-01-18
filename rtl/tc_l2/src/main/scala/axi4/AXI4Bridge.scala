package sim

import chisel3._
import chisel3.util._

import treecorel2._
import treecorel2.common.AXI4Config

class AXI4Bridge extends Module with AXI4Config {
  val io = IO(new Bundle {
    val socEn = Input(Bool())
    val runEn = Output(Bool())
    val dxchg = Flipped(new DXCHGIO)
    val axi   = if (SoCEna) new SOCAXI4IO else new AXI4IO
  })

  protected val arbiter = Module(new Arbiter)
  arbiter.io.runEn    <> io.runEn
  arbiter.io.dxchg    <> io.dxchg
  arbiter.io.axirdata := io.axi.r.bits.data
  arbiter.io.awHdShk  := io.axi.aw.fire()
  arbiter.io.wHdShk   := io.axi.w.fire()
  arbiter.io.bHdShk   := io.axi.b.fire()
  arbiter.io.arHdShk  := io.axi.ar.fire()
  arbiter.io.rHdShk   := io.axi.r.fire()

  protected val wMask     = arbiter.io.dxchg.wmask
  protected val byteSize  = wMask(7) + wMask(6) + wMask(5) + wMask(4) + wMask(3) + wMask(2) + wMask(1) + wMask(0)
  protected val socARSize = arbiter.io.dxchg.rsize
  protected val socAWSize = MuxLookup(
    byteSize,
    0.U,
    Array(
      8.U -> 3.U,
      4.U -> 2.U,
      2.U -> 1.U,
      1.U -> 0.U
    )
  )

  protected val arSize   = Mux(io.socEn, socARSize, diffRWSize)
  protected val awSize   = Mux(io.socEn, socAWSize, diffRWSize)
  protected val addrMask = Mux(io.socEn, socAddrMask, difftestAddrMask)

  when(arbiter.io.state === Arbiter.eumAR) {
    io.axi.ar.valid     := true.B
    io.axi.ar.bits.size := arSize
    io.axi.ar.bits.addr := arbiter.io.dxchg.raddr & addrMask
    io.axi.r.ready      := false.B
    io.axi.aw.valid     := false.B
    io.axi.aw.bits.size := 0.U
    io.axi.aw.bits.addr := 0.U
    io.axi.w.valid      := false.B
    io.axi.w.bits.strb  := 0.U
    io.axi.w.bits.data  := 0.U
    io.axi.b.ready      := false.B

  }.elsewhen(arbiter.io.state === Arbiter.eumR) {
    io.axi.ar.valid     := false.B
    io.axi.ar.bits.size := arSize
    io.axi.ar.bits.addr := arbiter.io.dxchg.raddr & addrMask
    io.axi.r.ready      := true.B
    io.axi.aw.valid     := false.B
    io.axi.aw.bits.size := 0.U
    io.axi.aw.bits.addr := 0.U
    io.axi.w.valid      := false.B
    io.axi.w.bits.strb  := 0.U
    io.axi.w.bits.data  := 0.U
    io.axi.b.ready      := false.B

  }.elsewhen(arbiter.io.state === Arbiter.eumAW) {
    io.axi.ar.valid     := false.B
    io.axi.ar.bits.size := 0.U
    io.axi.ar.bits.addr := 0.U
    io.axi.r.ready      := false.B
    io.axi.aw.valid     := true.B
    io.axi.aw.bits.size := awSize
    io.axi.aw.bits.addr := arbiter.io.dxchg.waddr & addrMask
    io.axi.w.valid      := false.B
    io.axi.w.bits.strb  := 0.U
    io.axi.w.bits.data  := 0.U
    io.axi.b.ready      := false.B

  }.elsewhen(arbiter.io.state === Arbiter.eumW) {
    io.axi.ar.valid     := false.B
    io.axi.ar.bits.size := 0.U
    io.axi.ar.bits.addr := 0.U
    io.axi.r.ready      := false.B
    io.axi.aw.valid     := false.B
    io.axi.aw.bits.size := awSize
    io.axi.aw.bits.addr := arbiter.io.dxchg.waddr & addrMask
    io.axi.w.valid      := true.B
    io.axi.w.bits.strb  := wMask
    io.axi.w.bits.data  := arbiter.io.dxchg.wdata
    io.axi.b.ready      := false.B

  }.elsewhen(arbiter.io.state === Arbiter.eumB) {
    io.axi.ar.valid     := false.B
    io.axi.ar.bits.size := 0.U
    io.axi.ar.bits.addr := 0.U
    io.axi.r.ready      := false.B
    io.axi.aw.valid     := false.B
    io.axi.aw.bits.size := 0.U
    io.axi.aw.bits.addr := 0.U
    io.axi.w.valid      := false.B
    io.axi.w.bits.strb  := 0.U
    io.axi.w.bits.data  := 0.U
    io.axi.b.ready      := true.B

  }.otherwise {
    io.axi.ar.valid     := false.B
    io.axi.ar.bits.size := 0.U
    io.axi.ar.bits.addr := 0.U
    io.axi.r.ready      := false.B
    io.axi.aw.valid     := false.B
    io.axi.aw.bits.size := 0.U
    io.axi.aw.bits.addr := 0.U
    io.axi.w.valid      := false.B
    io.axi.w.bits.strb  := 0.U
    io.axi.w.bits.data  := 0.U
    io.axi.b.ready      := false.B

  }

  if (!SoCEna) {
    val sim = io.axi.asInstanceOf[AXI4IO]
    sim.ar.bits.prot  := 0.U
    sim.ar.bits.id    := 0.U
    sim.ar.bits.len   := 0.U
    sim.ar.bits.burst := 1.U
    sim.ar.bits.lock  := 0.U
    sim.ar.bits.cache := 0.U
    sim.ar.bits.qos   := 0.U
    sim.ar.bits.user  := DontCare
    sim.aw.bits.prot  := 0.U
    sim.aw.bits.id    := 0.U
    sim.aw.bits.len   := 0.U
    sim.aw.bits.burst := 1.U
    sim.aw.bits.lock  := false.B
    sim.aw.bits.cache := 0.U
    sim.aw.bits.qos   := 0.U
    sim.aw.bits.user  := DontCare
    sim.w.bits.last   := 1.U

  } else {
    io.axi.ar.bits.id    := 0.U
    io.axi.ar.bits.len   := 0.U
    io.axi.ar.bits.burst := 1.U
    io.axi.aw.bits.id    := 0.U
    io.axi.aw.bits.len   := 0.U
    io.axi.aw.bits.burst := 1.U
    io.axi.w.bits.last   := 1.U
  }
}
