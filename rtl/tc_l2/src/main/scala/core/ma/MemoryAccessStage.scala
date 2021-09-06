package treecorel2

import chisel3._
import chisel3.util._
import AXI4Bridge._
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
    // example: sd -> M[x[rs1]+ sext(offset)] = x[rs2][7:0]
    // memValAin -> x[rs1]
    val memValAIn: UInt = Input(UInt(BusWidth.W))
    // memValBIn -> x[rs2]
    val memValBIn: UInt = Input(UInt(BusWidth.W))
    // memOffsetIn -> offset
    val memOffsetIn: UInt = Input(UInt(BusWidth.W))

    // from alu?
    val wtDataIn: UInt = Input(UInt(BusWidth.W))
    val wtEnaIn:  Bool = Input(Bool())
    val wtAddrIn: UInt = Input(UInt(RegAddrLen.W))
    // from axibridge
    val memReadyIn:  Bool = Input(Bool())
    val memRdDataIn: UInt = Input(UInt(BusWidth.W))
    val memRespIn:   UInt = Input(UInt(AxiRespLen.W))

    // to regfile
    val wtDataOut: UInt = Output(UInt(BusWidth.W))
    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))
    // to axibridge
    val memValidOut: Bool = Output(Bool())
    val memReqOut:   UInt = Output(UInt(2.W)) // read or write
    val memDataOut:  UInt = Output(UInt(AxiDataWidth.W)) // write to the dram
    val memAddrOut:  UInt = Output(UInt(AxiDataWidth.W))
    val memSizeOut:  UInt = Output(UInt(AxiSizeLen.W))
    // to control
    val stallReqOut: Bool = Output(Bool())
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

  protected val memValidReg: Bool = RegInit(false.B)
  io.memValidOut := memValidReg
  // judge the write regfile data's origin
  when(io.memReadyIn) {
    io.wtDataOut := loadData
    io.memReqOut := AxiReqNop.U // the is decided by valid and what's value of this is dont matter
    memValidReg  := false.B
  }.otherwise {
    io.wtDataOut := io.wtDataIn
    io.memReqOut := AxiReqNop.U
    when(io.memOperTypeIn >= lsuLBType && io.memOperTypeIn <= lsuLDType) {
      io.memReqOut := AxiReqRd.U
      memValidReg  := true.B
    }.elsewhen(io.memOperTypeIn >= lsuSBType && io.memOperTypeIn <= lsuSDType) {
      io.memReqOut := AxiReqWt.U
      memValidReg  := true.B
    }
  }
  io.wtEnaOut  := io.wtEnaIn
  io.wtAddrOut := io.wtAddrIn

  // for load and store inst addr
  when(
    io.memOperTypeIn === lsuLBUType ||
      io.memOperTypeIn === lsuLHUType ||
      io.memOperTypeIn === lsuLWUType
  ) {
    io.memAddrOut := getZeroExtn(BusWidth, io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn))
  }.otherwise {
    io.memAddrOut := getSignExtn(BusWidth, io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn))
  }

  // prepare write data
  io.memDataOut := MuxLookup(
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

  io.memSizeOut  := AXI4Bridge.SIZE_D
  io.stallReqOut := io.memValidOut && (~io.memReadyIn)
}
