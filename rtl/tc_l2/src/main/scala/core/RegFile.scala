package treecorel2

import chisel3._
import difftest._

class RegFile(val ifDiffTest: Boolean) extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val rdEnaAIn:  Bool = Input(Bool())
    val rdAddrAIn: UInt = Input(UInt(RegAddrLen.W))

    val rdEnaBIn:  Bool = Input(Bool())
    val rdAddrBIn: UInt = Input(UInt(RegAddrLen.W))

    val wtEnaIn:  Bool = Input(Bool())
    val wtAddrIn: UInt = Input(UInt(RegAddrLen.W))
    val wtDataIn: UInt = Input(UInt(BusWidth.W))

    val rdDataAOut: UInt = Output(UInt(BusWidth.W))
    val rdDataBOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val regFile: Vec[UInt] = RegInit(VecInit(Seq.fill(RegNum)(0.U(BusWidth.W))))
  protected val rdDataA: UInt      = Wire(UInt(BusWidth.W))
  protected val rdDataB: UInt      = Wire(UInt(BusWidth.W))
  protected val wtAddr:  UInt      = io.wtAddrIn

  when(io.wtEnaIn && (io.wtAddrIn =/= 0.U)) {
    regFile(wtAddr) := io.wtDataIn
  }

  // regFile(wtAddr) := Mux(io.wtEnaIn && (io.wtAddrIn =/= 0.U), io.wtDataIn, regFile(wtAddr))

  rdDataA       := Mux(io.rdEnaAIn, regFile(io.rdAddrAIn), 0.U)
  rdDataB       := Mux(io.rdEnaBIn, regFile(io.rdAddrBIn), 0.U)
  io.rdDataAOut := rdDataA
  io.rdDataBOut := rdDataB

  // protected val regFile = Mem(RegNum, UInt(BusWidth.W))
  // TODO: need to solve the when rdAddr* === wtAddrIn(the forward circuit)
  // io.rdDataAOut := Mux(io.rdEnaAIn && (io.rdAddrAIn =/= 0.U), regFile.read(io.rdAddrAIn), 0.U)
  // io.rdDataBOut := Mux(io.rdEnaBIn && (io.rdAddrBIn =/= 0.U), regFile.read(io.rdAddrBIn), 0.U)
  // regFile.write(io.wtAddrIn, Mux(io.wtEnaIn && (io.wtAddrIn =/= 0.U), io.wtDataIn, regFile(io.wtAddrIn)))

  if (ifDiffTest) {
    val diffRegState: DifftestArchIntRegState = Module(new DifftestArchIntRegState)
    diffRegState.io.clock  := this.clock
    diffRegState.io.coreid := 0.U
    diffRegState.io.gpr.zipWithIndex.foreach({
      case (v, i) => v := regFile(i.U)
    })
  }
}
