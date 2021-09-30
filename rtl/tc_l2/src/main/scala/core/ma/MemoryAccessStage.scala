package treecorel2

import chisel3._
import chisel3.util._
import AXI4Bridge._
import treecorel2.common.ConstVal._
import treecorel2.common.{getSignExtn, getZeroExtn}

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
    // to clint
    val clintWt: TRANSIO = new TRANSIO
  })

  protected val memValidReg:       Bool = RegInit(false.B)
  protected val memReqReg:         UInt = RegInit(AxiReqNop.U(AxiReqLen.W))
  protected val memAddrReg:        UInt = RegInit(0.U(AxiDataWidth.W))
  protected val memDataReg:        UInt = RegInit(0.U(AxiDataWidth.W))
  protected val memRegfileAddrReg: UInt = RegInit(0.U(RegAddrLen.W))
  protected val memInstAddrReg:    UInt = RegInit(0.U(BusWidth.W))
  protected val memInstDataReg:    UInt = RegInit(0.U(InstWidth.W))
  protected val memInstSizeReg:    UInt = RegInit(0.U(2.W))
  protected val memOperTypeReg:    UInt = RegInit(0.U(InstOperTypeLen.W))
  protected val memFunc3Reg:       UInt = RegInit(0.U(3.W))
  protected val isFirstReg:        Bool = RegInit(true.B)

  protected val signExtnMidVal:  UInt = WireDefault(UInt(BusWidth.W), io.memValAIn + getSignExtn(BusWidth, io.memOffsetIn, io.memOffsetIn(63)))
  protected val memSignExtnAddr: UInt = WireDefault(UInt(BusWidth.W), getSignExtn(BusWidth, signExtnMidVal, signExtnMidVal(63)))
  protected val memZeroExtnAddr: UInt = WireDefault(UInt(BusWidth.W), getZeroExtn(BusWidth, signExtnMidVal))

  io.axi.valid := memValidReg
  io.axi.req   := memReqReg
  io.axi.addr  := memAddrReg
  io.axi.id    := 1.U
  io.axi.wdata := memDataReg

  io.clintWt.ena  := WireDefault(false.B)
  io.clintWt.addr := WireDefault(UInt(BusWidth.W), 0.U)
  io.clintWt.data := WireDefault(UInt(BusWidth.W), 0.U)
  // judge the write regfile data's origin
  when(io.axi.ready) {
    when(isFirstReg) {
      isFirstReg            := false.B
      io.ifValidOut         := false.B
      io.ifMemInstCommitOut := true.B

      // save the mem oper type and memFunc3 type to sign ext the read data from the axi bus
      when(memOperTypeReg === lsuLBType || memOperTypeReg === lsuLBUType) {
        io.wtDataOut := Cat(Fill(BusWidth - 8, Mux(memFunc3Reg(2), 0.U, io.axi.rdata(7))), io.axi.rdata(7, 0))
      }.elsewhen(memOperTypeReg === lsuLHType || memOperTypeReg === lsuLHUType) {
        // printf("prepare the mem wt data!!!!!!!!!\n")
        // printf(p"#############[ma]io.wtDataOut = 0x${Hexadecimal(io.wtDataOut)}\n")
        io.wtDataOut := Cat(Fill(BusWidth - 16, Mux(memFunc3Reg(2), 0.U, io.axi.rdata(15))), io.axi.rdata(15, 0))
      }.elsewhen(memOperTypeReg === lsuLWType || memOperTypeReg === lsuLWUType) {
        io.wtDataOut := Cat(Fill(BusWidth - 32, Mux(memFunc3Reg(2), 0.U, io.axi.rdata(31))), io.axi.rdata(31, 0))
      }.otherwise {
        io.wtDataOut := io.axi.rdata
      }

      // TODO: some bug: this use trick code to handle this bug, if not the 'ready' will triggered twice
      // printf("ready!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
      // printf(p"#############[ma]io.axi.rdata = 0x${Hexadecimal(io.axi.rdata)}\n")
      // printf(p"#############[ma]io.wtDataOut = 0x${Hexadecimal(io.wtDataOut)}\n")
      memReqReg   := AxiReqNop.U
      memValidReg := false.B

      io.instOut.addr := memInstAddrReg
      io.instOut.data := memInstDataReg
      io.wtEnaOut     := true.B
      io.wtAddrOut    := memRegfileAddrReg
    }.otherwise {
      io.ifValidOut         := false.B
      io.ifMemInstCommitOut := false.B
      io.wtDataOut          := io.wtDataIn
      io.instOut            <> io.instIn
      io.wtEnaOut           := false.B
      io.wtAddrOut          := io.wtAddrIn
    }
  }.otherwise {
    io.ifValidOut         := false.B
    io.ifMemInstCommitOut := false.B
    io.instOut            <> io.instIn
    io.wtEnaOut           := io.wtEnaIn
    io.wtAddrOut          := io.wtAddrIn
    io.wtDataOut          := io.wtDataIn
    when(io.memOperTypeIn >= lsuLBType && io.memOperTypeIn <= lsuLDType) {
      isFirstReg    := true.B
      io.ifValidOut := true.B
      io.wtEnaOut   := false.B
      // *((uint64_t *) CLINT_MTIMECMP) += 7000000; just use the sd inst
      when(memSignExtnAddr === ClintBaseAddr + MTimeCmpOffset) {
        memValidReg     := false.B
        io.ifValidOut   := false.B
        io.clintWt.addr := ClintBaseAddr + MTimeCmpOffset
      }.elsewhen(memSignExtnAddr === ClintBaseAddr + MTimeOffset) {
        memValidReg     := false.B
        io.ifValidOut   := false.B
        io.clintWt.addr := ClintBaseAddr + MTimeOffset
      }.otherwise {
        memValidReg := true.B
      }
      memReqReg         := AxiReqRd.U
      memRegfileAddrReg := io.wtAddrIn
      memInstAddrReg    := io.instIn.addr
      memInstDataReg    := io.instIn.data
      memOperTypeReg    := io.memOperTypeIn
      memFunc3Reg       := io.memFunc3In
    }.elsewhen(io.memOperTypeIn >= lsuSBType && io.memOperTypeIn <= lsuSDType) {
      isFirstReg    := true.B
      io.ifValidOut := true.B
      io.wtEnaOut   := false.B
      when(memSignExtnAddr === ClintBaseAddr + MTimeCmpOffset) {
        memValidReg     := false.B
        io.ifValidOut   := false.B
        io.clintWt.ena  := true.B
        io.clintWt.addr := ClintBaseAddr + MTimeCmpOffset
        io.clintWt.data := io.memValBIn
      }.elsewhen(memSignExtnAddr === ClintBaseAddr + MTimeOffset) {
        memValidReg     := false.B
        io.ifValidOut   := false.B
        io.clintWt.ena  := true.B
        io.clintWt.addr := ClintBaseAddr + MTimeOffset
        io.clintWt.data := io.memValBIn
      }.otherwise {
        memValidReg := true.B
      }
      memReqReg         := AxiReqWt.U
      memRegfileAddrReg := io.wtAddrIn
      memInstAddrReg    := io.instIn.addr
      memInstDataReg    := io.instIn.data
    }
  }

  // for load and store inst addr
  when(
    io.memOperTypeIn === lsuLBUType ||
      io.memOperTypeIn === lsuLHUType ||
      io.memOperTypeIn === lsuLWUType
  ) {
    memAddrReg := memZeroExtnAddr
  }.elsewhen(
    (io.memOperTypeIn >= lsuSBType && io.memOperTypeIn <= lsuSDType) ||
      (io.memOperTypeIn === lsuLBType) ||
      (io.memOperTypeIn === lsuLHType) ||
      (io.memOperTypeIn === lsuLWType) ||
      (io.memOperTypeIn === lsuLDType)
  ) {
    memAddrReg := memSignExtnAddr
  }

  // prepare write data
  memDataReg := MuxLookup(
    io.memOperTypeIn,
    memDataReg,
    Seq(
      // lsuSBType -> (io.memValBIn(7, 0) << (io.axi.addr(2, 0) * 8.U)),
      // lsuSBType -> io.memValBIn(7, 0),
      lsuSBType -> io.memValBIn,
      // (0x0, 0x1)->0, (0x2, 0x3)->1, (0x4, 0x5)->2, (0x6, 0x7)->3
      // shift bits: (addr(2, 0) / 2 * 16 bit)
      // lsuSHType -> (io.memValBIn(15, 0) << (io.axi.addr(2, 0) * 8.U)),
      lsuSHType -> io.memValBIn,
      // (0x0...0x3)->0, (0x4...0x7)->1
      // shift bits: (addr(2, 0) / 4 * 32 bit)
      // lsuSWType -> (io.memValBIn(31, 0) << (io.axi.addr(2, 0) * 8.U)),
      lsuSWType -> io.memValBIn,
      // (0x0...0x7)->0
      lsuSDType -> io.memValBIn
    )
  )

  // io.axi.size := AXI4Bridge.SIZE_D // ? some bug
  io.axi.size := memInstSizeReg
  when(
    io.memOperTypeIn === lsuLBType ||
      io.memOperTypeIn === lsuLBUType ||
      io.memOperTypeIn === lsuSBType
  ) {
    memInstSizeReg := AXI4Bridge.SIZE_B
  }.elsewhen(
    io.memOperTypeIn === lsuLHType ||
      io.memOperTypeIn === lsuLHUType ||
      io.memOperTypeIn === lsuSHType
  ) {
    memInstSizeReg := AXI4Bridge.SIZE_H
  }.elsewhen(
    io.memOperTypeIn === lsuLWType ||
      io.memOperTypeIn === lsuLWUType ||
      io.memOperTypeIn === lsuSWType
  ) {
    memInstSizeReg := AXI4Bridge.SIZE_W
  }.elsewhen(
    io.memOperTypeIn === lsuLDType ||
      io.memOperTypeIn === lsuSDType
  ) {
    memInstSizeReg := AXI4Bridge.SIZE_D
  }

  io.stallReqOut := io.axi.valid && (~io.axi.ready)
}
