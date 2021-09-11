package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class EXToMA extends Module with InstConfig {
  val io = IO(new Bundle {
    // from ex
    val exDataIn:      UInt   = Input(UInt(BusWidth.W))
    val exWtEnaIn:     Bool   = Input(Bool())
    val exWtAddrIn:    UInt   = Input(UInt(RegAddrLen.W))
    val lsuFunc3In:    UInt   = Input(UInt(3.W))
    val lsuWtEnaIn:    Bool   = Input(Bool())
    val lsuOperTypeIn: UInt   = Input(UInt(InstOperTypeLen.W))
    val lsuValAIn:     UInt   = Input(UInt(BusWidth.W))
    val lsuValBIn:     UInt   = Input(UInt(BusWidth.W))
    val lsuOffsetIn:   UInt   = Input(UInt(BusWidth.W))
    val instIn:        INSTIO = new INSTIO

    // to ma
    val maDataOut:      UInt   = Output(UInt(BusWidth.W))
    val maWtEnaOut:     Bool   = Output(Bool())
    val maWtAddrOut:    UInt   = Output(UInt(RegAddrLen.W))
    val lsuFunc3Out:    UInt   = Output(UInt(3.W))
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
  protected val lsuFunc3Reg:    UInt = RegInit(0.U(3.W))
  protected val lsuWtEnaReg:    Bool = RegInit(false.B)
  protected val lsuOperTypeReg: UInt = RegInit(0.U(InstOperTypeLen.W))
  protected val lsuValAReg:     UInt = RegInit(0.U(BusWidth.W))
  protected val lsuValBReg:     UInt = RegInit(0.U(BusWidth.W))
  protected val lsuOffsetReg:   UInt = RegInit(0.U(BusWidth.W))

  dataReg        := io.exDataIn
  wtEnaReg       := io.exWtEnaIn
  wtAddrReg      := io.exWtAddrIn
  lsuFunc3Reg    := io.lsuFunc3In
  lsuWtEnaReg    := io.lsuWtEnaIn
  lsuOperTypeReg := io.lsuOperTypeIn
  lsuValAReg     := io.lsuValAIn
  lsuValBReg     := io.lsuValBIn
  lsuOffsetReg   := io.lsuOffsetIn

  io.maDataOut      := dataReg
  io.maWtEnaOut     := wtEnaReg
  io.maWtAddrOut    := wtAddrReg
  io.lsuFunc3Out    := lsuFunc3Reg
  io.lsuWtEnaOut    := lsuWtEnaReg
  io.lsuOperTypeOut := lsuOperTypeReg
  io.lsuValAOut     := lsuValAReg
  io.lsuValBOut     := lsuValBReg
  io.lsuOffsetOut   := lsuOffsetReg
}
