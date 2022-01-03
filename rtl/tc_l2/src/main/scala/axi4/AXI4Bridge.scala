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

  protected val runEn = RegInit(false.B)
  io.runEn := Mux(reset.asBool(), false.B, runEn)

  // handshake
  protected val awHdShk = io.axi.aw.valid && io.axi.aw.ready
  protected val wHdShk  = io.axi.w.valid && io.axi.w.ready
  protected val bHdShk  = io.axi.b.valid && io.axi.b.ready
  protected val arHdShk = io.axi.ar.valid && io.axi.ar.ready
  protected val rHdShk  = io.axi.r.valid && io.axi.r.ready
  protected val arbiter = new Arbiter

  // FSM for read/write
  protected val eumIDLE :: eumStandby :: eumIDLE2 :: eumAW :: eumW :: eumB :: eumAR :: eumR :: Nil = Enum(8)

  protected val stateReg = RegInit(eumIDLE)

  switch(stateReg) {
    is(eumIDLE) {
      arbiter.finished := false.B
      arbiter.ren      := io.dxchg.ren
      arbiter.raddr    := io.dxchg.raddr
      arbiter.rdata    := io.dxchg.rdata
      arbiter.rsize    := io.dxchg.rsize
      arbiter.wen      := io.dxchg.wen
      arbiter.waddr    := io.dxchg.waddr
      arbiter.wdata    := io.dxchg.wdata
      arbiter.wmask    := io.dxchg.wmask
      stateReg         := eumStandby
    }
    is(eumStandby) {
      when(arbiter.finished) {
        runEn    := true.B
        stateReg := eumIDLE2
      }.elsewhen(arbiter.wen) {
        stateReg := eumAW
      }.elsewhen(arbiter.ren) {
        stateReg := eumAR
      }.otherwise {
        arbiter.finished := true.B
        stateReg         := eumStandby
      }
    }
    is(eumIDLE2) {
      runEn    := false.B
      stateReg := eumIDLE
    }
    is(eumAR) {
      when(io.axi.ar.ready) {
        stateReg := eumR
      }
    }
    is(eumR) {
      when(rHdShk) {
        arbiter.rdata    := io.axi.r.bits.data
        arbiter.finished := true.B
        stateReg         := eumStandby
      }
    }
    is(eumAW) {
      when(awHdShk) {
        stateReg := eumW
      }
    }
    is(eumW) {
      when(wHdShk) {
        stateReg := eumB
      }
    }
    is(eumB) {
      when(bHdShk) {
        arbiter.finished := true.B
        stateReg         := eumStandby
      }
    }
  }

  protected val wMask  = arbiter.wmask
  protected val bitCnt = wMask(7) + wMask(6) + wMask(5) + wMask(4) + wMask(3) + wMask(2) + wMask(1) + wMask(0)

  protected val socARSize = arbiter.rsize
  protected val socAWSize = MuxLookup(
    bitCnt,
    0.U,
    Array(
      8.U -> 3.U,
      4.U -> 2.U,
      2.U -> 1.U,
      1.U -> 0.U
    )
  )

  protected val arSize   = Mux(io.socEn, socARSize, 3.U)
  protected val awSize   = Mux(io.socEn, socAWSize, 3.U)
  protected val addrMask = Mux(io.socEn, "hffffffffffffffff".U(64.W), "hfffffffffffffff8".U(64.W))
  when(stateReg === eumAR) {
    io.axi.ar.valid     := true.B
    io.axi.ar.bits.size := arSize
    io.axi.ar.bits.addr := arbiter.raddr & addrMask
    io.axi.r.ready      := false.B
    io.axi.aw.valid     := false.B
    io.axi.aw.bits.size := 0.U
    io.axi.aw.bits.addr := 0.U
    io.axi.w.valid      := false.B
    io.axi.w.bits.strb  := 0.U
    io.axi.w.bits.data  := 0.U
    io.axi.b.ready      := false.B

  }.elsewhen(stateReg === eumR) {
    io.axi.ar.valid     := false.B
    io.axi.ar.bits.size := arSize
    io.axi.ar.bits.addr := arbiter.raddr & addrMask
    io.axi.r.ready      := true.B
    io.axi.aw.valid     := false.B
    io.axi.aw.bits.size := 0.U
    io.axi.aw.bits.addr := 0.U
    io.axi.w.valid      := false.B
    io.axi.w.bits.strb  := 0.U
    io.axi.w.bits.data  := 0.U
    io.axi.b.ready      := false.B

  }.elsewhen(stateReg === eumAW) {
    io.axi.ar.valid     := false.B
    io.axi.ar.bits.size := 0.U
    io.axi.ar.bits.addr := 0.U
    io.axi.r.ready      := false.B
    io.axi.aw.valid     := true.B
    io.axi.aw.bits.size := awSize
    io.axi.aw.bits.addr := arbiter.waddr & addrMask
    io.axi.w.valid      := false.B
    io.axi.w.bits.strb  := 0.U
    io.axi.w.bits.data  := 0.U
    io.axi.b.ready      := false.B

  }.elsewhen(stateReg === eumW) {
    io.axi.ar.valid     := false.B
    io.axi.ar.bits.size := 0.U
    io.axi.ar.bits.addr := 0.U
    io.axi.r.ready      := false.B
    io.axi.aw.valid     := false.B
    io.axi.aw.bits.size := awSize
    io.axi.aw.bits.addr := arbiter.waddr & addrMask
    io.axi.w.valid      := true.B
    io.axi.w.bits.strb  := wMask
    io.axi.w.bits.data  := arbiter.wdata
    io.axi.b.ready      := false.B

  }.elsewhen(stateReg === eumB) {
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

  io.dxchg.rdata := arbiter.rdata
}
