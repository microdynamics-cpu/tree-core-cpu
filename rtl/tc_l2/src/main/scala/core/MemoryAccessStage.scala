package treecorel2

import chisel3._
import chisel3.util._

class MemoryAccessStage extends Module with InstConfig {
  val io = IO(new Bundle {
    // resIn: maybe the 64bits data to write back, or addr to load/store data
    val func3:    UInt = Input(UInt(3.W))
    val resIn:    UInt = Input(UInt(BusWidth.W))
    val wtEnaIn:  Bool = Input(Bool())
    val wtAddrIn: UInt = Input(UInt(RegAddrLen.W))

    val memRdDataIn: UInt = Input(UInt(BusWidth.W))

    val resOut:    UInt = Output(UInt(BusWidth.W))
    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))

    val memAddrOut:   UInt = Output(UInt(BusWidth.W))
    val memWtEnaOut:  Bool = Output(Bool())
    val memWtDataOut: UInt = Output(UInt(BusWidth.W))
    val memMaskOut:   UInt = Output(UInt(BusWidth.W))
    val memValidOut:  Bool = Output(Bool())
  })

  protected val lwData: UInt = Mux(io.resIn(2), io.memRdDataIn(63, 32), io.memRdDataIn(31, 0))
  protected val lhData: UInt = Mux(io.resIn(1), lwData(31, 16), lwData(15, 0))
  protected val lbData: UInt = Mux(io.resIn(0), lhData(15, 8), lhData(7, 0))

  // select unsign or sign extension
  protected val lbWire: UInt = Cat(
    Fill(BusWidth - 8, Mux(io.func3(2), 0.U, lbData(7))),
    lbData
  )

  protected val lhWire: UInt = Cat(
    Fill(BusWidth - 16, Mux(io.func3(2), 0.U, lhData(15))),
    lhData
  )

  protected val lwWire: UInt = Cat(
    Fill(BusWidth - 32, Mux(io.func3(2), 0.U, lwData(31))),
    lwData
  )

  protected val ldWire: UInt = io.memRdDataIn
  protected val loadData: UInt = MuxLookup(
    io.func3(1, 0),
    io.memRdDataIn,
    Array(
      0.U -> lbWire,
      1.U -> lhWire,
      2.U -> lwWire,
      3.U -> ldWire
    )
  )

  // protected val wMask: UInt = decoder(
  //   minimizer = EspressoMinimizer,
  //   input     = Cat(io.func3(1, 0), io.resIn(2, 0)),
  //   truthTable = TruthTable(
  //     Map(
  //       BitPat("b11" + "???") -> BitPat("b" + "1" * 64),
  //       BitPat("b10" + "1??") -> BitPat("b" + "1" * 32 + "0" * 32),
  //       BitPat("b10" + "0??") -> BitPat("b" + "0" * 32 + "1" * 32),
  //       BitPat("b01" + "11?") -> BitPat("b" + "1" * 16 + "0" * 48),
  //       BitPat("b01" + "10?") -> BitPat("b" + "0" * 16 + "1" * 16 + "0" * 32),
  //       BitPat("b01" + "01?") -> BitPat("b" + "0" * 32 + "1" * 16 + "0" * 16),
  //       BitPat("b01" + "00?") -> BitPat("b" + "0" * 48 + "1" * 16),
  //       BitPat("b00" + "111") -> BitPat("b" + "1" * 8 + "0" * 56),
  //       BitPat("b00" + "110") -> BitPat("b" + "0" * 8 + "1" * 8 + "0" * 48),
  //       BitPat("b00" + "101") -> BitPat("b" + "0" * 16 + "1" * 8 + "0" * 40),
  //       BitPat("b00" + "100") -> BitPat("b" + "0" * 24 + "1" * 8 + "0" * 32),
  //       BitPat("b00" + "010") -> BitPat("b" + "0" * 32 + "1" * 8 + "0" * 24),
  //       BitPat("b00" + "001") -> BitPat("b" + "0" * 40 + "1" * 8 + "0" * 16),
  //       BitPat("b00" + "000") -> BitPat("b" + "0" * 56 + "1" * 8)
  //     ),
  //     BitPat("b" + "1" * BusWidth)
  //   )
  // )

  io.resOut    := io.resIn
  io.wtEnaOut  := io.wtEnaIn
  io.wtAddrOut := io.wtAddrIn

  io.memAddrOut := io.resIn
  // io.wbData     := loadData
  // io.memWtEnaOut := io.memCmd === ("b" + M_XWR).U
  io.memWtEnaOut  := false.B
  io.memWtDataOut := 0.U
  // io.memValidOut   := io.exeMemValid
  io.memValidOut := false.B
  // io.memMaskOut  := wMask
  io.memMaskOut := 0.U

  //@printf(p"[ma]io.resOut = 0x${Hexadecimal(io.resOut)}\n")
  //@printf(p"[ma]io.wtEnaOut = 0x${Hexadecimal(io.wtEnaOut)}\n")
  //@printf(p"[ma]io.wtAddrOut = 0x${Hexadecimal(io.wtAddrOut)}\n")
}
