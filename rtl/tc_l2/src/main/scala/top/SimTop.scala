package sim.difftest

import chisel3._
import treecorel2._
import difftest._

class RAMHelper extends BlackBox with InstConfig {
  val io = IO(new Bundle {
    val clk:   Clock = Input(Clock())
    val en:    Bool  = Input(Bool())
    val rIdx:  UInt  = Input(UInt(BusWidth.W))
    val rdata: UInt  = Output(UInt(BusWidth.W))
    val wIdx:  UInt  = Input(UInt(BusWidth.W))
    val wdata: UInt  = Input(UInt(BusWidth.W))
    val wmask: UInt  = Input(UInt(BusWidth.W))
    val wen:   Bool  = Input(Bool())
  })
}

class SimTop(val ifDiffTest: Boolean) extends Module with InstConfig {
  val io = IO(new Bundle {
    val logCtrl  = new LogCtrlIO
    val perfInfo = new PerfInfoIO
    val uart     = new UARTIO
  })

  protected val treeCoreL2 = Module(new TreeCoreL2(ifDiffTest))
  protected val instRam: RAMHelper = Module(new RAMHelper())
  protected val dataRam: RAMHelper = Module(new RAMHelper())

  instRam.io.clk := this.clock
  instRam.io.en  := !this.reset.asBool() && treeCoreL2.io.instEnaOut
  // instRam.io.en            := true.B
  instRam.io.rIdx          := (treeCoreL2.io.instAddrOut - PcRegStartAddr.U) >> 3
  instRam.io.wIdx          := DontCare
  instRam.io.wen           := DontCare
  instRam.io.wdata         := DontCare
  instRam.io.wmask         := DontCare
  treeCoreL2.io.instDataIn := Mux(treeCoreL2.io.instAddrOut(2), instRam.io.rdata(63, 32), instRam.io.rdata(31, 0))

  // //@printf(p"[simtop]instRam.io.en = 0x${Hexadecimal(instRam.io.en)}\n")
  // //@printf(p"[simtop]treeCoreL2.io.instAddrOut = 0x${Hexadecimal(treeCoreL2.io.instAddrOut)}\n")
  // //@printf(p"[simtop]instRam.io.rIdx = 0x${Hexadecimal(instRam.io.rIdx)}\n")
  // //@printf(p"[simtop]instRam.io.rdata = 0x${Hexadecimal(instRam.io.rdata)}\n")

  dataRam.io.clk            := this.clock
  dataRam.io.en             := !this.reset.asBool() && treeCoreL2.io.memValidOut
  dataRam.io.rIdx           := (treeCoreL2.io.memAddrOut - PcRegStartAddr.U) >> 3
  treeCoreL2.io.memRdDataIn := dataRam.io.rdata
  dataRam.io.wen            := treeCoreL2.io.memWtEnaOut
  dataRam.io.wIdx           := (treeCoreL2.io.memAddrOut - PcRegStartAddr.U) >> 3
  dataRam.io.wdata          := treeCoreL2.io.memWtDataOut
  dataRam.io.wmask          := treeCoreL2.io.memMaskOut

  io.uart.in.valid  := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch    := 0.U
}

object SimTop extends App {
  (new chisel3.stage.ChiselStage).execute(
    args,
    Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new SimTop(true)))
  )
}
