package treecorel2

import chisel3._
import chisel3.util._
import treecorel2.common.ConstVal._

object MemoryAccessStage {
  protected val defMaskRes = List(BitPat.bitPatToUInt(BitPat("b" + "1" * 64)))

  protected val wMaskTable = Array(
    BitPat("b11" + "???") -> List(BitPat.bitPatToUInt(BitPat("b" + "1" * 64))),
    // lw
    BitPat("b10" + "1??") -> List(BitPat.bitPatToUInt(BitPat("b" + "1" * 32 + "0" * 32))),
    BitPat("b10" + "0??") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 32 + "1" * 32))),
    // lh
    BitPat("b01" + "11?") -> List(BitPat.bitPatToUInt(BitPat("b" + "1" * 16 + "0" * 48))),
    BitPat("b01" + "10?") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 16 + "1" * 16 + "0" * 32))),
    BitPat("b01" + "01?") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 32 + "1" * 16 + "0" * 16))),
    BitPat("b01" + "00?") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 48 + "1" * 16))),
    // lb
    BitPat("b00" + "111") -> List(BitPat.bitPatToUInt(BitPat("b" + "1" * 8 + "0" * 56))),
    BitPat("b00" + "110") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 8 + "1" * 8 + "0" * 48))),
    BitPat("b00" + "101") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 16 + "1" * 8 + "0" * 40))),
    BitPat("b00" + "100") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 24 + "1" * 8 + "0" * 32))),
    BitPat("b00" + "010") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 32 + "1" * 8 + "0" * 24))),
    BitPat("b00" + "001") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 40 + "1" * 8 + "0" * 16))),
    BitPat("b00" + "000") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 56 + "1" * 8)))
  )

}

class MemoryAccessStage extends Module with InstConfig {
  val io = IO(new Bundle {
    // wtDataIn: maybe the 64bits data to write back, or addr to load/store data
    val memFunc3In:    UInt = Input(UInt(3.W))
    val memOperTypeIn: UInt = Input(UInt(InstOperTypeLen.W))
    val memValAIn:     UInt = Input(UInt(BusWidth.W))
    val memValBIn:     UInt = Input(UInt(BusWidth.W))
    val memOffsetIn:   UInt = Input(UInt(BusWidth.W))

    val memRdDataIn: UInt = Input(UInt(BusWidth.W))

    val wtDataIn: UInt = Input(UInt(BusWidth.W))
    val wtEnaIn:  Bool = Input(Bool())
    val wtAddrIn: UInt = Input(UInt(RegAddrLen.W))

    val wtDataOut: UInt = Output(UInt(BusWidth.W))
    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))

    val memAddrOut:   UInt = Output(UInt(BusWidth.W))
    val memWtDataOut: UInt = Output(UInt(BusWidth.W))
    val memMaskOut:   UInt = Output(UInt(BusWidth.W))
    val memValidOut:  Bool = Output(Bool())
  })

  protected val lwData: UInt = Mux(io.wtDataIn(2), io.memRdDataIn(63, 32), io.memRdDataIn(31, 0))
  protected val lhData: UInt = Mux(io.wtDataIn(1), lwData(31, 16), lwData(15, 0))
  protected val lbData: UInt = Mux(io.wtDataIn(0), lhData(15, 8), lhData(7, 0))

  // select unsign or sign extension
  protected val lbWire: UInt = Cat(
    Fill(BusWidth - 8, Mux(io.memFunc3In(2), 0.U, lbData(7))),
    lbData
  )

  protected val lhWire: UInt = Cat(
    Fill(BusWidth - 16, Mux(io.memFunc3In(2), 0.U, lhData(15))),
    lhData
  )

  protected val lwWire: UInt = Cat(
    Fill(BusWidth - 32, Mux(io.memFunc3In(2), 0.U, lwData(31))),
    lwData
  )

  protected val ldWire: UInt = io.memRdDataIn

  protected val loadData: UInt = MuxLookup(
    io.memFunc3In(1, 0),
    io.memRdDataIn,
    Array(
      0.U -> lbWire,
      1.U -> lhWire,
      2.U -> lwWire,
      3.U -> ldWire
    )
  )

  protected val wMask =
    ListLookup(Cat(io.memFunc3In(1, 0), io.wtDataIn(2, 0)), MemoryAccessStage.defMaskRes, MemoryAccessStage.wMaskTable)(0)

  io.wtDataOut := io.wtDataIn
  io.wtEnaOut  := io.wtEnaIn
  io.wtAddrOut := io.wtAddrIn

  io.memAddrOut := io.memValAIn + io.memOffsetIn
  // io.wbData     := loadData

  io.memWtDataOut := MuxLookup(
    io.memOperTypeIn,
    0.U,
    Seq(
      lsuSBType -> (io.memValBIn(7, 0))
    )
  )
  // io.memValidOut   := io.exeMemValid
  io.memValidOut := false.B
  io.memMaskOut  := wMask

  //@printf(p"[ma]io.wtDataOut = 0x${Hexadecimal(io.wtDataOut)}\n")
  //@printf(p"[ma]io.wtEnaOut = 0x${Hexadecimal(io.wtEnaOut)}\n")
  //@printf(p"[ma]io.wtAddrOut = 0x${Hexadecimal(io.wtAddrOut)}\n")
}
