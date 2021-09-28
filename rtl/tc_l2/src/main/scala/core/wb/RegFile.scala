package treecorel2

import chisel3._
import difftest._
import treecorel2.common.ConstVal._

class RegFile(val ifDiffTest: Boolean) extends Module with InstConfig {
  val io = IO(new Bundle {
    // from id
    val rdEnaAIn:  Bool = Input(Bool())
    val rdAddrAIn: UInt = Input(UInt(RegAddrLen.W))
    val rdEnaBIn:  Bool = Input(Bool())
    val rdAddrBIn: UInt = Input(UInt(RegAddrLen.W))
    val wtEnaIn:   Bool = Input(Bool())
    val wtAddrIn:  UInt = Input(UInt(RegAddrLen.W))
    val wtDataIn:  UInt = Input(UInt(BusWidth.W))
    // to id
    val rdDataAOut: UInt = Output(UInt(BusWidth.W))
    val rdDataBOut: UInt = Output(UInt(BusWidth.W))
    // to top
    val charDataOut: UInt = Output(UInt(BusWidth.W))
    val debugOutA:   UInt = Output(UInt(BusWidth.W))
    val debugOutB:   UInt = Output(UInt(BusWidth.W))
    val debugOutC:   UInt = Output(UInt(BusWidth.W))
    val debugOutD:   UInt = Output(UInt(BusWidth.W))
  })

  protected val regFile = Mem(RegNum, UInt(BusWidth.W))
  protected val wtAddr: UInt = io.wtAddrIn
  protected val wtData: UInt = io.wtDataIn

  regFile.write(io.wtAddrIn, Mux(io.wtEnaIn, Mux(io.wtAddrIn === 0.U(RegAddrLen.W), 0.U(BusWidth.W), wtData), regFile(io.wtAddrIn)))

  io.rdDataAOut := Mux(
    io.rdEnaAIn,
    Mux(io.rdAddrAIn =/= 0.U(RegAddrLen.W), Mux(io.rdAddrAIn === io.wtAddrIn, wtData, regFile(io.rdAddrAIn)), 0.U(BusWidth.W)),
    0.U(BusWidth.W)
  )
  io.rdDataBOut := Mux(
    io.rdEnaBIn,
    Mux(io.rdAddrBIn =/= 0.U(RegAddrLen.W), Mux(io.rdAddrBIn === io.wtAddrIn, wtData, regFile(io.rdAddrBIn)), 0.U(BusWidth.W)),
    0.U(BusWidth.W)
  )

  // for custom inst output
  io.charDataOut := regFile(10.U)
  io.debugOutA   := regFile(15.U)
  io.debugOutB   := regFile(2.U)
  io.debugOutC   := regFile(8.U)
  io.debugOutD   := regFile(10.U)
  if (ifDiffTest) {
    val diffRegState: DifftestArchIntRegState = Module(new DifftestArchIntRegState)
    diffRegState.io.clock  := this.clock
    diffRegState.io.coreid := 0.U
    // $0 is always zero!!
    diffRegState.io.gpr.zipWithIndex.foreach({
      case (v, i) => v := Mux(i.U === 0.U(RegAddrLen.W), 0.U(BusWidth.W), regFile(i.U))
    })
  }
}
