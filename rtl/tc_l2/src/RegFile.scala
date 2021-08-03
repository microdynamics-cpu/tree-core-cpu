package treecorel2

import chisel3._

class RegFile extends Module with ConstantDefine {
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

  protected val regFile = Mem(RegNum, UInt(BusWidth.W))

  // TODO: need to solve the when rdAddr* === wtAddrIn(the forward circuit)
  io.rdDataAOut := Mux(io.rdEnaAIn && (io.rdAddrAIn =/= 0.U), regFile.read(io.rdAddrAIn), 0.U)
  io.rdDataBOut := Mux(io.rdEnaBIn && (io.rdAddrBIn =/= 0.U), regFile.read(io.rdAddrBIn), 0.U)

  // regFile.write(io.wtAddrIn, Mux(io.wtEnaIn, Mux(io.wtAddrIn === 0.U, 0.U, io.wtDataIn), regFile(io.wtAddrIn)))
  regFile.write(io.wtAddrIn, Mux(io.wtEnaIn && (io.wtAddrIn =/= 0.U), io.wtDataIn, regFile(io.wtAddrIn)))
}
