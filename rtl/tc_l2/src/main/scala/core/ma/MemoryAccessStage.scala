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
    // example: sd -> M[x[rs1]+ sext(offset)] = x[rs2][7:0]
    // lsInstIn.valA -> x[rs1] lsInstIn.valB -> x[rs2] lsInstIn.offset -> offset
    val lsInstIn: LSINSTIO   = Flipped(new LSINSTIO) // from ex2ma
    val wtIn:     TRANSIO    = Flipped(new TRANSIO(RegAddrLen, BusWidth)) // from alu
    val wtOut:    TRANSIO    = new TRANSIO(RegAddrLen, BusWidth) // to ma2wb
    val axi:      AXI4USERIO = Flipped(new AXI4USERIO) // from axibridge
    val instIn:   INSTIO     = new INSTIO

    // to ma2wb
    val ifValidOut:         Bool   = Output(Bool())
    val ifMemInstCommitOut: Bool   = Output(Bool())
    val instOut:            INSTIO = Flipped(new INSTIO)
    // to control
    val stallReqOut: Bool = Output(Bool())
    // to clint
    val clintWt: TRANSIO = new TRANSIO(BusWidth, BusWidth)
  })

  protected val memValidReg:       Bool = RegInit(false.B)
  protected val memReqReg:         UInt = RegInit(AxiReqNop.U(AxiReqLen.W))
  protected val memAddrReg:        UInt = RegInit(0.U(AxiAddrWidth.W))
  protected val memDataReg:        UInt = RegInit(0.U(AxiDataWidth.W))
  protected val memRegfileAddrReg: UInt = RegInit(0.U(RegAddrLen.W))
  protected val memInstAddrReg:    UInt = RegInit(0.U(BusWidth.W))
  protected val memInstDataReg:    UInt = RegInit(0.U(InstWidth.W))
  protected val memInstSizeReg:    UInt = RegInit(0.U(2.W))
  protected val memOperTypeReg:    UInt = RegInit(0.U(InstOperTypeLen.W))
  protected val memFunc3MSBReg:    UInt = RegInit(0.U(1.W))
  protected val isFirstReg:        Bool = RegInit(true.B)

  protected val signExtnMidVal:  UInt = WireDefault(UInt(BusWidth.W), io.lsInstIn.valA + getSignExtn(BusWidth, io.lsInstIn.offset, io.lsInstIn.offset(63)))
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
        io.wtOut.data := Cat(Fill(BusWidth - 8, Mux(memFunc3MSBReg === 1.U, 0.U, io.axi.rdata(7))), io.axi.rdata(7, 0))
      }.elsewhen(memOperTypeReg === lsuLHType || memOperTypeReg === lsuLHUType) {
        // printf("prepare the mem wt data!!!!!!!!!\n")
        // printf(p"#############[ma]io.wtOut.data = 0x${Hexadecimal(io.wtOut.data)}\n")
        io.wtOut.data := Cat(Fill(BusWidth - 16, Mux(memFunc3MSBReg === 1.U, 0.U, io.axi.rdata(15))), io.axi.rdata(15, 0))
      }.elsewhen(memOperTypeReg === lsuLWType || memOperTypeReg === lsuLWUType) {
        io.wtOut.data := Cat(Fill(BusWidth - 32, Mux(memFunc3MSBReg === 1.U, 0.U, io.axi.rdata(31))), io.axi.rdata(31, 0))
      }.otherwise {
        io.wtOut.data := io.axi.rdata
      }

      // TODO: some bug: this use trick code to handle this bug, if not the 'ready' will triggered twice
      // printf("ready!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
      // printf(p"#############[ma]io.axi.rdata = 0x${Hexadecimal(io.axi.rdata)}\n")
      // printf(p"#############[ma]io.wtOut.data = 0x${Hexadecimal(io.wtOut.data)}\n")
      memReqReg   := AxiReqNop.U
      memValidReg := false.B

      io.instOut.addr := memInstAddrReg
      io.instOut.data := memInstDataReg
      io.wtOut.ena    := true.B
      io.wtOut.addr   := memRegfileAddrReg
    }.otherwise {
      io.ifValidOut         := false.B
      io.ifMemInstCommitOut := false.B
      io.wtOut.data         := io.wtIn.data
      io.instOut            <> io.instIn
      io.wtOut.ena          := false.B
      io.wtOut.addr         := io.wtIn.addr
    }
  }.otherwise {
    io.ifValidOut         := false.B
    io.ifMemInstCommitOut := false.B
    io.instOut            <> io.instIn
    io.wtOut.ena          := io.wtIn.ena
    io.wtOut.addr         := io.wtIn.addr
    io.wtOut.data         := io.wtIn.data
    when(io.lsInstIn.operType >= lsuLBType && io.lsInstIn.operType <= lsuLDType) {
      isFirstReg    := true.B
      io.ifValidOut := true.B
      io.wtOut.ena  := false.B
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
      memRegfileAddrReg := io.wtIn.addr
      memInstAddrReg    := io.instIn.addr
      memInstDataReg    := io.instIn.data
      memOperTypeReg    := io.lsInstIn.operType
      memFunc3MSBReg    := io.lsInstIn.func3MSB
    }.elsewhen(io.lsInstIn.operType >= lsuSBType && io.lsInstIn.operType <= lsuSDType) {
      isFirstReg    := true.B
      io.ifValidOut := true.B
      io.wtOut.ena  := false.B
      when(memSignExtnAddr === ClintBaseAddr + MTimeCmpOffset) {
        memValidReg     := false.B
        io.ifValidOut   := false.B
        io.clintWt.ena  := true.B
        io.clintWt.addr := ClintBaseAddr + MTimeCmpOffset
        io.clintWt.data := io.lsInstIn.valB
      }.elsewhen(memSignExtnAddr === ClintBaseAddr + MTimeOffset) {
        memValidReg     := false.B
        io.ifValidOut   := false.B
        io.clintWt.ena  := true.B
        io.clintWt.addr := ClintBaseAddr + MTimeOffset
        io.clintWt.data := io.lsInstIn.valB
      }.otherwise {
        memValidReg := true.B
      }
      memReqReg         := AxiReqWt.U
      memRegfileAddrReg := io.wtIn.addr
      memInstAddrReg    := io.instIn.addr
      memInstDataReg    := io.instIn.data
    }
  }

  // for load and store inst addr
  when(
    io.lsInstIn.operType === lsuLBUType ||
      io.lsInstIn.operType === lsuLHUType ||
      io.lsInstIn.operType === lsuLWUType
  ) {
    memAddrReg := memZeroExtnAddr
  }.elsewhen(
    (io.lsInstIn.operType >= lsuSBType && io.lsInstIn.operType <= lsuSDType) ||
      (io.lsInstIn.operType === lsuLBType) ||
      (io.lsInstIn.operType === lsuLHType) ||
      (io.lsInstIn.operType === lsuLWType) ||
      (io.lsInstIn.operType === lsuLDType)
  ) {
    memAddrReg := memSignExtnAddr
  }

  // prepare write data
  memDataReg := MuxLookup(
    io.lsInstIn.operType,
    memDataReg,
    Seq(
      // lsuSBType -> (io.lsInstIn.valB(7, 0) << (io.axi.addr(2, 0) * 8.U)),
      // lsuSBType -> io.lsInstIn.valB(7, 0),
      lsuSBType -> io.lsInstIn.valB,
      // (0x0, 0x1)->0, (0x2, 0x3)->1, (0x4, 0x5)->2, (0x6, 0x7)->3
      // shift bits: (addr(2, 0) / 2 * 16 bit)
      // lsuSHType -> (io.lsInstIn.valB(15, 0) << (io.axi.addr(2, 0) * 8.U)),
      lsuSHType -> io.lsInstIn.valB,
      // (0x0...0x3)->0, (0x4...0x7)->1
      // shift bits: (addr(2, 0) / 4 * 32 bit)
      // lsuSWType -> (io.lsInstIn.valB(31, 0) << (io.axi.addr(2, 0) * 8.U)),
      lsuSWType -> io.lsInstIn.valB,
      // (0x0...0x7)->0
      lsuSDType -> io.lsInstIn.valB
    )
  )

  // io.axi.size := AXI4Bridge.SIZE_D // ? some bug
  io.axi.size := memInstSizeReg
  when(
    io.lsInstIn.operType === lsuLBType ||
      io.lsInstIn.operType === lsuLBUType ||
      io.lsInstIn.operType === lsuSBType
  ) {
    memInstSizeReg := AXI4Bridge.SIZE_B
  }.elsewhen(
    io.lsInstIn.operType === lsuLHType ||
      io.lsInstIn.operType === lsuLHUType ||
      io.lsInstIn.operType === lsuSHType
  ) {
    memInstSizeReg := AXI4Bridge.SIZE_H
  }.elsewhen(
    io.lsInstIn.operType === lsuLWType ||
      io.lsInstIn.operType === lsuLWUType ||
      io.lsInstIn.operType === lsuSWType
  ) {
    memInstSizeReg := AXI4Bridge.SIZE_W
  }.elsewhen(
    io.lsInstIn.operType === lsuLDType ||
      io.lsInstIn.operType === lsuSDType
  ) {
    memInstSizeReg := AXI4Bridge.SIZE_D
  }

  io.stallReqOut := io.axi.valid && (~io.axi.ready)
}
