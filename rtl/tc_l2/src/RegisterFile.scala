package treecorel2

import chisel3._

class RegisterFile extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val rdEna1:  Bool = Input(Bool())
    val rdAddr1: UInt = Input(UInt(RegAddrLen.W))

    val rdEna2:  Bool = Input(Bool())
    val rdAddr2: UInt = Input(UInt(RegAddrLen.W))

    val wtEna:  Bool = Input(Bool())
    val wtAddr: UInt = Input(UInt(RegAddrLen.W))
    val wtData: UInt = Input(UInt(BusWidth.W))

    val rdData1: UInt = Output(UInt(BusWidth.W))
    val rdData2: UInt = Output(UInt(BusWidth.W))
  })

  private val regFile = Mem(RegNum, UInt(BusWidth.W))

  // TODO: need to solve the when rdAddr* === wtAddr(the forward circuit)
  io.rdData1 := Mux(io.rdEna1 && (io.rdAddr1 =/= 0.U), regFile.read(io.rdAddr1), 0.U)
  io.rdData2 := Mux(io.rdEna2 && (io.rdAddr2 =/= 0.U), regFile.read(io.rdAddr2), 0.U)

  // regFile.write(io.wtAddr, Mux(io.wtEna, Mux(io.wtAddr === 0.U, 0.U, io.wtData), regFile(io.wtAddr)))
  regFile.write(io.wtAddr, Mux(io.wtEna && (io.wtAddr =/= 0.U), io.wtData, regFile(io.wtAddr)))
}
