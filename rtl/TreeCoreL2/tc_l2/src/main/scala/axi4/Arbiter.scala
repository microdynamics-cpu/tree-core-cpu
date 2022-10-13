package treecorel2

import chisel3._
import chisel3.util._

object Arbiter {
// FSM var for read/write
  val eumIDLE :: eumStandby :: eumIDLE2 :: eumAW :: eumW :: eumB :: eumAR :: eumR :: Nil = Enum(8)
}

class Arbiter extends Module with InstConfig {
  val io = IO(new Bundle {
    val awHdShk  = Input(Bool())
    val wHdShk   = Input(Bool())
    val bHdShk   = Input(Bool())
    val arHdShk  = Input(Bool())
    val rHdShk   = Input(Bool())
    val axirdata = Input(UInt(XLen.W))
    val dxchg    = Flipped(new DXCHGIO)
    val state    = Output(UInt(3.W))
    val runEn    = Output(Bool())
  })

  // arbitrate mem and inst req
  protected val runEn = RegInit(false.B)
  io.runEn := Mux(reset.asBool(), false.B, runEn)

  protected val valid    = RegInit(false.B)
  protected val ren      = RegInit(false.B)
  protected val raddr    = RegInit(0.U(XLen.W))
  protected val rdata    = RegInit(0.U(XLen.W))
  protected val rsize    = RegInit(0.U(LDSize.W))
  protected val wen      = RegInit(false.B)
  protected val waddr    = RegInit(0.U(XLen.W))
  protected val wdata    = RegInit(0.U(XLen.W))
  protected val wmask    = RegInit(0.U(MaskLen.W))
  protected val stateReg = RegInit(Arbiter.eumIDLE)
  io.state       := stateReg
  io.dxchg.rdata := rdata

  switch(stateReg) {
    is(Arbiter.eumIDLE) {
      valid    := false.B
      ren      := io.dxchg.ren
      raddr    := io.dxchg.raddr
      rdata    := io.dxchg.rdata
      rsize    := io.dxchg.rsize
      wen      := io.dxchg.wen
      waddr    := io.dxchg.waddr
      wdata    := io.dxchg.wdata
      wmask    := io.dxchg.wmask
      stateReg := Arbiter.eumStandby
    }
    is(Arbiter.eumStandby) {
      when(valid) {
        runEn    := true.B
        stateReg := Arbiter.eumIDLE2

      }.elsewhen(wen) {
        stateReg := Arbiter.eumAW

      }.elsewhen(ren) {
        stateReg := Arbiter.eumAR

      }.otherwise {
        valid    := true.B
        stateReg := Arbiter.eumStandby
      }
    }
    is(Arbiter.eumIDLE2) {
      runEn    := false.B
      stateReg := Arbiter.eumIDLE
    }
    is(Arbiter.eumAR) {
      when(io.arHdShk) {
        stateReg := Arbiter.eumR
      }
    }
    is(Arbiter.eumR) {
      when(io.rHdShk) {
        valid    := true.B
        stateReg := Arbiter.eumStandby
        rdata    := io.axirdata
      }
    }
    is(Arbiter.eumAW) {
      when(io.awHdShk) {
        stateReg := Arbiter.eumW
      }
    }
    is(Arbiter.eumW) {
      when(io.wHdShk) {
        stateReg := Arbiter.eumB
      }
    }
    is(Arbiter.eumB) {
      when(io.bHdShk) {
        valid    := true.B
        stateReg := Arbiter.eumStandby
      }
    }
  }
}
