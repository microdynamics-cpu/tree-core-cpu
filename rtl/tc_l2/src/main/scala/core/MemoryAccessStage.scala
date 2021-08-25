package treecorel2

import chisel3._
import chisel3.util._
import treecorel2.common.ConstVal._
import treecorel2.common.{getSignExtn, getZeroExtn}

object MemoryAccessStage {
  protected val defMaskRes = List(BitPat.bitPatToUInt(BitPat("b" + "1" * 64)))

  protected val wMaskTable = Array(
    // ld
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
    BitPat("b00" + "011") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 32 + "1" * 8 + "0" * 24))),
    BitPat("b00" + "010") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 40 + "1" * 8 + "0" * 16))),
    BitPat("b00" + "001") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 48 + "1" * 8 + "0" * 8))),
    BitPat("b00" + "000") -> List(BitPat.bitPatToUInt(BitPat("b" + "0" * 56 + "1" * 8)))
  )

}

// read data addr is calc by alu
// write data addr is passed from id stage directly
class MemoryAccessStage extends Module with InstConfig {
  val io = IO(new Bundle {
    // wt mem ena signal is send from ex2ma stage
    val memFunc3In:    UInt = Input(UInt(3.W))
    val memOperTypeIn: UInt = Input(UInt(InstOperTypeLen.W))
    val memValAIn:     UInt = Input(UInt(BusWidth.W))
    val memValBIn:     UInt = Input(UInt(BusWidth.W))
    val memOffsetIn:   UInt = Input(UInt(BusWidth.W))

    val memRdDataIn: UInt = Input(UInt(BusWidth.W))

    // from alu?
    val wtDataIn: UInt = Input(UInt(BusWidth.W))
    val wtEnaIn:  Bool = Input(Bool())
    val wtAddrIn: UInt = Input(UInt(RegAddrLen.W))

    // to regfile
    val wtDataOut: UInt = Output(UInt(BusWidth.W))
    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))

    // to mem
    val memAddrOut:   UInt = Output(UInt(BusWidth.W))
    val memWtDataOut: UInt = Output(UInt(BusWidth.W))
    val memMaskOut:   UInt = Output(UInt(BusWidth.W))
    val memValidOut:  Bool = Output(Bool())
  })

  protected val lwData: UInt = Mux(io.memAddrOut(2), io.memRdDataIn(63, 32), io.memRdDataIn(31, 0))
  protected val lhData: UInt = Mux(io.memAddrOut(1), lwData(31, 16), lwData(15, 0))
  protected val lbData: UInt = Mux(io.memAddrOut(0), lhData(15, 8), lhData(7, 0))

  // select zero or sign extension
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
    ListLookup(Cat(io.memFunc3In(1, 0), io.memAddrOut(2, 0)), MemoryAccessStage.defMaskRes, MemoryAccessStage.wMaskTable)(0)

  when(io.memOperTypeIn >= lsuLBType && io.memOperTypeIn <= lsuLDType) {
    io.wtDataOut := loadData
  }.otherwise {
    io.wtDataOut := io.wtDataIn
  }

  // io.wtDataOut := io.wtDataIn
  io.wtEnaOut  := io.wtEnaIn
  io.wtAddrOut := io.wtAddrIn

  // for load and store inst
  when(
    io.memOperTypeIn === lsuLBUType ||
      io.memOperTypeIn === lsuLHUType ||
      io.memOperTypeIn === lsuLWUType
  ) {
    io.memAddrOut := getZeroExtn(BusWidth, io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn))
  }.otherwise {
    io.memAddrOut := getSignExtn(BusWidth, io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn))
  }

  io.memWtDataOut := MuxLookup(
    io.memOperTypeIn,
    0.U,
    Seq(
      lsuSBType -> (io.memValBIn(7, 0) << (io.memAddrOut(2, 0) * 8.U)),
      // (0x0, 0x1)->0, (0x2, 0x3)->1, (0x4, 0x5)->2, (0x6, 0x7)->3
      // shift bits: (addr(2, 0) / 2 * 16 bit)
      lsuSHType -> (io.memValBIn(15, 0) << (io.memAddrOut(2, 0) * 8.U)),
      // (0x0...0x3)->0, (0x4...0x7)->1
      // shift bits: (addr(2, 0) / 4 * 32 bit)
      lsuSWType -> (io.memValBIn(31, 0) << (io.memAddrOut(2, 0) * 8.U)),
      // (0x0...0x7)->0
      lsuSDType -> io.memValBIn(63, 0)
    )
  )

  io.memValidOut := true.B
  io.memMaskOut  := wMask

  // printf(p"[ma] io.memMaskOut = 0x${Hexadecimal(io.memMaskOut)}\n")
  // printf(p"[ma] io.memRdDataIn = 0x${Hexadecimal(io.memRdDataIn)}\n")
  // printf(p"[ma] loadData = 0x${Hexadecimal(loadData)}\n")
  // printf(p"[ma]io.wtDataIn = 0x${Hexadecimal(io.wtDataIn)}\n")

  // printf(p"[ma] io.memOperTypeIn = 0x${Hexadecimal(io.memOperTypeIn)}\n")
  // printf(p"[ma] io.memValAIn = 0x${Hexadecimal(io.memValAIn)}\n")
  // printf(p"[ma] io.memValBIn = 0x${Hexadecimal(io.memValBIn)}\n")
  // printf(p"[ma] io.memOffsetIn = 0x${Hexadecimal(io.memOffsetIn)}\n")
  // printf(p"[ma] io.memAddrOut = 0x${Hexadecimal(io.memAddrOut)}\n")
  // printf(p"[ma] io.memWtDataOut = 0x${Hexadecimal(io.memWtDataOut)}\n")

  // printf(p"[ma]io.wtDataOut = 0x${Hexadecimal(io.wtDataOut)}\n")
  // printf(p"[ma]io.wtEnaOut = 0x${Hexadecimal(io.wtEnaOut)}\n")
  // printf(p"[ma]io.wtAddrOut = 0x${Hexadecimal(io.wtAddrOut)}\n")
  // printf("\n")
}
