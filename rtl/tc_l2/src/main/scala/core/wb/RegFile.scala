package treecorel2

import chisel3._
import difftest._
import treecorel2.common.ConstVal._

object RegFile {
  val abiTable = Map(
    0  -> "zero",
    1  -> "ra",
    2  -> "sp",
    3  -> "gp",
    4  -> "tp",
    5  -> "t0",
    6  -> "t1",
    7  -> "t2",
    8  -> "s0",
    9  -> "s1",
    10 -> "a0",
    11 -> "a1",
    12 -> "a2",
    13 -> "a3",
    14 -> "a4",
    15 -> "a5",
    16 -> "a6",
    17 -> "a7",
    18 -> "s2",
    19 -> "s3",
    20 -> "s4",
    21 -> "s5",
    22 -> "s6",
    23 -> "s7",
    24 -> "s8",
    25 -> "s9",
    26 -> "s10",
    27 -> "s11",
    28 -> "t3",
    29 -> "t4",
    30 -> "t5",
    31 -> "t6"
  )
}

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
    // from difftest
    val debugIn: UInt = Input(UInt(RegNum.W))
    // to id
    val rdDataAOut: UInt = Output(UInt(BusWidth.W))
    val rdDataBOut: UInt = Output(UInt(BusWidth.W))
    // to top
    val charDataOut: UInt = Output(UInt(BusWidth.W))
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

  if (ifDiffTest) {
    // for custom inst output
    io.charDataOut := regFile(10.U)
    for ((i, abi) <- RegFile.abiTable) {
      when(io.debugIn(i) === 1.U) {
        printf(p"[regfile] ${abi} = 0x${Hexadecimal(regFile(i))}\n")
      }
    }

    val diffRegState: DifftestArchIntRegState = Module(new DifftestArchIntRegState)
    diffRegState.io.clock  := this.clock
    diffRegState.io.coreid := 0.U
    // $0 is always zero!!
    diffRegState.io.gpr.zipWithIndex.foreach({
      case (v, i) => v := Mux(i.U === 0.U(RegAddrLen.W), 0.U(BusWidth.W), regFile(i.U))
    })
  } else {
    io.charDataOut := DontCare
  }
}
