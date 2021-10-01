package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class EXToMA extends Module with InstConfig {
  val io = IO(new Bundle {
    val instIn:   INSTIO   = new INSTIO
    val wtIn:     TRANSIO  = Flipped(new TRANSIO(RegAddrLen, BusWidth)) // from ex
    val lsInstIn: LSINSTIO = Flipped(new LSINSTIO) // from id&

    val instOut:   INSTIO   = Flipped(new INSTIO)
    val wtOut:     TRANSIO  = new TRANSIO(RegAddrLen, BusWidth) // to ma
    val lsInstOut: LSINSTIO = new LSINSTIO // to ma
  })

  protected val instAddrReg:    UInt = RegInit(0.U(BusWidth.W))
  protected val instDataReg:    UInt = RegInit(0.U(InstWidth.W))
  protected val dataReg:        UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:       Bool = RegInit(false.B)
  protected val wtAddrReg:      UInt = RegInit(0.U(RegAddrLen.W))
  protected val lsuFunc3MSBReg: UInt = RegInit(0.U(1.W))
  protected val lsuOperTypeReg: UInt = RegInit(0.U(InstOperTypeLen.W))
  protected val lsuValAReg:     UInt = RegInit(0.U(BusWidth.W))
  protected val lsuValBReg:     UInt = RegInit(0.U(BusWidth.W))
  protected val lsuOffsetReg:   UInt = RegInit(0.U(BusWidth.W))

  dataReg        := io.wtIn.data
  wtEnaReg       := io.wtIn.ena
  wtAddrReg      := io.wtIn.addr
  lsuFunc3MSBReg := io.lsInstIn.func3MSB
  lsuOperTypeReg := io.lsInstIn.operType
  lsuValAReg     := io.lsInstIn.valA
  lsuValBReg     := io.lsInstIn.valB
  lsuOffsetReg   := io.lsInstIn.offset
  instAddrReg    := io.instIn.addr
  instDataReg    := io.instIn.data

  io.wtOut.data         := dataReg
  io.wtOut.ena          := wtEnaReg
  io.wtOut.addr         := wtAddrReg
  io.lsInstOut.func3MSB := lsuFunc3MSBReg
  io.lsInstOut.operType := lsuOperTypeReg
  io.lsInstOut.valA     := lsuValAReg
  io.lsInstOut.valB     := lsuValBReg
  io.lsInstOut.offset   := lsuOffsetReg
  io.instOut.addr       := instAddrReg
  io.instOut.data       := instDataReg
}
