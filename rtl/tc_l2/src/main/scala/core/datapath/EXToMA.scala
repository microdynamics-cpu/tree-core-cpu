package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class EXToMA extends Module with InstConfig {
  val io = IO(new Bundle {

    val wtIn:  TRANSIO = Flipped(new TRANSIO(RegAddrLen, BusWidth)) // from ex
    val wtOut: TRANSIO = new TRANSIO(RegAddrLen, BusWidth) // to ma

    val lsuFunc3MSBIn: UInt   = Input(UInt(1.W))
    val lsuWtEnaIn:    Bool   = Input(Bool())
    val lsuOperTypeIn: UInt   = Input(UInt(InstOperTypeLen.W))
    val lsuValAIn:     UInt   = Input(UInt(BusWidth.W))
    val lsuValBIn:     UInt   = Input(UInt(BusWidth.W))
    val lsuOffsetIn:   UInt   = Input(UInt(BusWidth.W))
    val instIn:        INSTIO = new INSTIO

    // to ma
    val lsuFunc3MSBOut: UInt   = Output(UInt(1.W))
    val lsuWtEnaOut:    Bool   = Output(Bool())
    val lsuOperTypeOut: UInt   = Output(UInt(InstOperTypeLen.W))
    val lsuValAOut:     UInt   = Output(UInt(BusWidth.W))
    val lsuValBOut:     UInt   = Output(UInt(BusWidth.W))
    val lsuOffsetOut:   UInt   = Output(UInt(BusWidth.W))
    val instOut:        INSTIO = Flipped(new INSTIO)
  })

  //####################
  protected val instAddrReg: UInt = RegInit(0.U(BusWidth.W))
  protected val instDataReg: UInt = RegInit(0.U(InstWidth.W))

  instAddrReg     := io.instIn.addr
  instDataReg     := io.instIn.data
  io.instOut.addr := instAddrReg
  io.instOut.data := instDataReg
  //####################

  protected val dataReg:        UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:       Bool = RegInit(false.B)
  protected val wtAddrReg:      UInt = RegInit(0.U(RegAddrLen.W))
  protected val lsuFunc3MSBReg: UInt = RegInit(0.U(1.W))
  protected val lsuWtEnaReg:    Bool = RegInit(false.B)
  protected val lsuOperTypeReg: UInt = RegInit(0.U(InstOperTypeLen.W))
  protected val lsuValAReg:     UInt = RegInit(0.U(BusWidth.W))
  protected val lsuValBReg:     UInt = RegInit(0.U(BusWidth.W))
  protected val lsuOffsetReg:   UInt = RegInit(0.U(BusWidth.W))

  dataReg        := io.wtIn.data
  wtEnaReg       := io.wtIn.ena
  wtAddrReg      := io.wtIn.addr
  lsuFunc3MSBReg := io.lsuFunc3MSBIn
  lsuWtEnaReg    := io.lsuWtEnaIn
  lsuOperTypeReg := io.lsuOperTypeIn
  lsuValAReg     := io.lsuValAIn
  lsuValBReg     := io.lsuValBIn
  lsuOffsetReg   := io.lsuOffsetIn

  io.wtOut.data     := dataReg
  io.wtOut.ena      := wtEnaReg
  io.wtOut.addr     := wtAddrReg
  io.lsuFunc3MSBOut := lsuFunc3MSBReg
  io.lsuWtEnaOut    := lsuWtEnaReg
  io.lsuOperTypeOut := lsuOperTypeReg
  io.lsuValAOut     := lsuValAReg
  io.lsuValBOut     := lsuValBReg
  io.lsuOffsetOut   := lsuOffsetReg
}
