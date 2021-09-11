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
class MemoryAccessStage extends Module with AXI4Config with InstConfig {
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
    val axi:    AXI4USERIO = Flipped(new AXI4USERIO)
    val instIn: INSTIO     = new INSTIO

    // to regfile
    val wtDataOut: UInt = Output(UInt(BusWidth.W))
    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))

    // to ma2wb
    val ifValidOut:         Bool   = Output(Bool())
    val ifMemInstCommitOut: Bool   = Output(Bool())
    val instOut:            INSTIO = Flipped(new INSTIO)
    // to control
    val stallReqOut: Bool = Output(Bool())
  })

  protected val lwData: UInt = Mux(io.axi.addr(2), io.axi.rdata(63, 32), io.axi.rdata(31, 0))
  protected val lhData: UInt = Mux(io.axi.addr(1), lwData(31, 16), lwData(15, 0))
  protected val lbData: UInt = Mux(io.axi.addr(0), lhData(15, 8), lhData(7, 0))

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

  protected val ldWire: UInt = io.axi.rdata

  protected val loadData: UInt = MuxLookup(
    io.memFunc3In(1, 0),
    io.axi.rdata,
    Array(
      0.U -> lbWire,
      1.U -> lhWire,
      2.U -> lwWire,
      3.U -> ldWire
    )
  )

  protected val wMask =
    ListLookup(Cat(io.memFunc3In(1, 0), io.axi.addr(2, 0)), MemoryAccessStage.defMaskRes, MemoryAccessStage.wMaskTable)(0)

  protected val memValidReg:       Bool = RegInit(false.B)
  protected val memReqReg:         UInt = RegInit(AxiReqNop.U(AxiReqLen.W))
  protected val memAddrReg:        UInt = RegInit(0.U(AxiDataWidth.W))
  protected val memDataReg:        UInt = RegInit(0.U(AxiDataWidth.W))
  protected val memRegfileAddrReg: UInt = RegInit(0.U(RegAddrLen.W))
  protected val memInstAddrReg:    UInt = RegInit(0.U(BusWidth.W))
  protected val memInstDataReg:    UInt = RegInit(0.U(InstWidth.W))
  protected val isFirstReg:        Bool = RegInit(true.B)

  io.axi.valid := memValidReg
  io.axi.req   := memReqReg
  io.axi.addr  := memAddrReg
  io.axi.id    := 1.U
  io.axi.wdata := memDataReg

  // judge the write regfile data's origin
  when(io.axi.ready) {
    when(isFirstReg) {
      isFirstReg            := false.B
      io.ifValidOut         := false.B
      io.ifMemInstCommitOut := true.B
      io.wtDataOut          := loadData
      // io.wtDataOut          := io.axi.rdata
      // TODO: some bug: this use trick code to handle this bug, if not the 'ready' will triggered twice
      printf("ready!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
      printf(p"#############[ma]io.axi.rdata = 0x${Hexadecimal(io.axi.rdata)}\n")
      printf(p"#############[ma]io.wtDataOut = 0x${Hexadecimal(io.wtDataOut)}\n")
      memReqReg   := AxiReqNop.U
      memValidReg := false.B

      io.instOut.addr := memInstAddrReg
      io.instOut.data := memInstDataReg
      io.wtAddrOut    := memRegfileAddrReg
    }.otherwise {
      io.ifValidOut         := false.B
      io.ifMemInstCommitOut := false.B
      io.wtDataOut          := io.wtDataIn
      io.instOut            <> io.instIn
      io.wtAddrOut          := io.wtAddrIn
    }

  }.otherwise {
    io.ifValidOut         := false.B
    io.ifMemInstCommitOut := false.B
    io.wtDataOut          := io.wtDataIn
    io.instOut            <> io.instIn
    io.wtAddrOut          := io.wtAddrIn

    when(io.memOperTypeIn >= lsuLBType && io.memOperTypeIn <= lsuLDType) {
      isFirstReg        := true.B
      io.ifValidOut     := true.B
      memValidReg       := true.B
      memReqReg         := AxiReqRd.U
      memRegfileAddrReg := io.wtAddrIn
      memInstAddrReg    := io.instIn.addr
      memInstDataReg    := io.instIn.data
    }.elsewhen(io.memOperTypeIn >= lsuSBType && io.memOperTypeIn <= lsuSDType) {
      isFirstReg        := true.B
      io.ifValidOut     := true.B
      memValidReg       := true.B
      memReqReg         := AxiReqWt.U
      memRegfileAddrReg := io.wtAddrIn
      memInstAddrReg    := io.instIn.addr
      memInstDataReg    := io.instIn.data
    }
  }
  io.wtEnaOut := io.wtEnaIn
  // io.wtAddrOut := io.wtAddrIn

  // for load and store inst addr
  when(
    io.memOperTypeIn === lsuLBUType ||
      io.memOperTypeIn === lsuLHUType ||
      io.memOperTypeIn === lsuLWUType
  ) {
    // io.axi.addr := getZeroExtn(BusWidth, io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn))
    memAddrReg := getZeroExtn(BusWidth, io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn))
  }.elsewhen(
    (io.memOperTypeIn >= lsuSBType && io.memOperTypeIn <= lsuSDType) ||
      (io.memOperTypeIn === lsuLBType) ||
      (io.memOperTypeIn === lsuLHType) ||
      (io.memOperTypeIn === lsuLWType) ||
      (io.memOperTypeIn === lsuLDType)
  ) {
    // io.axi.addr := getSignExtn(BusWidth, io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn))
    memAddrReg := getSignExtn(BusWidth, io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn))
  }

  // prepare write data
  memDataReg := MuxLookup(
    io.memOperTypeIn,
    memDataReg,
    Seq(
      lsuSBType -> (io.memValBIn(7, 0) << (io.axi.addr(2, 0) * 8.U)),
      // lsuSBType -> io.memValBIn(7, 0),
      // lsuSBType -> io.memValBIn,
      // (0x0, 0x1)->0, (0x2, 0x3)->1, (0x4, 0x5)->2, (0x6, 0x7)->3
      // shift bits: (addr(2, 0) / 2 * 16 bit)
      lsuSHType -> (io.memValBIn(15, 0) << (io.axi.addr(2, 0) * 8.U)),
      // (0x0...0x3)->0, (0x4...0x7)->1
      // shift bits: (addr(2, 0) / 4 * 32 bit)
      lsuSWType -> (io.memValBIn(31, 0) << (io.axi.addr(2, 0) * 8.U)),
      // (0x0...0x7)->0
      lsuSDType -> io.memValBIn(63, 0)
    )
  )

  io.axi.size    := AXI4Bridge.SIZE_D
  io.stallReqOut := io.axi.valid && (~io.axi.ready)
}
