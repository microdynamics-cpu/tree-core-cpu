package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._

class CSRReg extends Module with InstConfig {
  val io = IO(new Bundle {
    // from id
    val rdAddrIn: UInt = Input(UInt(CSRAddrLen.W))
    // from ex's out
    val wtEnaIn:  Bool = Input(Bool())
    val wtDataIn: UInt = Input(UInt(BusWidth.W))
    // to ex's in
    val rdDataOut: UInt = Output(UInt(BusWidth.W))
    // to difftest
    val ifNeedSkip: Bool = Output(Bool())
  })

  protected val cycleReg: UInt = RegInit(0.U(BusWidth.W))

  //TODO: maybe some bug? the right value after wtena sig trigger
  when(io.wtEnaIn) {
    cycleReg := io.wtDataIn
  }.otherwise {
    cycleReg := cycleReg + 1.U(BusWidth.W)
  }

  io.rdDataOut := MuxLookup(
    io.rdAddrIn,
    0.U(BusWidth.W),
    Seq(
      mCycleAddr -> cycleReg
    )
  )

  when(io.rdAddrIn === mCycleAddr) {
    io.ifNeedSkip := true.B
  }.otherwise {
    io.ifNeedSkip := false.B
  }

  // printf(p"[csr]io.rdAddrIn = 0x${Hexadecimal(io.rdAddrIn)}\n")
  // printf(p"[csr]io.rdDataOut = 0x${Hexadecimal(io.rdDataOut)}\n")
  // printf("\n")
}
