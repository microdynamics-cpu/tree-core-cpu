package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class IDToEX extends Module with InstConfig {
  val io = IO(new Bundle {
    // from control
    val ifFlushIn: Bool = Input(Bool())
    // from id
    val idAluOperTypeIn: UInt   = Input(UInt(InstOperTypeLen.W))
    val idRsValAIn:      UInt   = Input(UInt(BusWidth.W))
    val idRsValBIn:      UInt   = Input(UInt(BusWidth.W))
    val idWtEnaIn:       Bool   = Input(Bool())
    val idWtAddrIn:      UInt   = Input(UInt(RegAddrLen.W))
    val lsuFunc3MSBIn:   UInt   = Input(UInt(1.W))
    val lsuWtEnaIn:      Bool   = Input(Bool())
    val instIn:          INSTIO = new INSTIO

    // to ex
    val exAluOperTypeOut: UInt = Output(UInt(InstOperTypeLen.W))
    val exRsValAOut:      UInt = Output(UInt(BusWidth.W))
    val exRsValBOut:      UInt = Output(UInt(BusWidth.W))
    val exWtEnaOut:       Bool = Output(Bool())
    val exWtAddrOut:      UInt = Output(UInt(RegAddrLen.W))
    // ex2ma
    val lsuFunc3MSBOut:    UInt   = Output(UInt(1.W))
    val lsuWtEnaOut:       Bool   = Output(Bool())
    val diffIdSkipInstOut: Bool   = Output(Bool())
    val instOut:           INSTIO = Flipped(new INSTIO)
  })

  protected val diffIdSkipInstReg: Bool = RegInit(false.B)
  protected val aluOperTypeReg:    UInt = RegInit(0.U(InstOperTypeLen.W))
  protected val rsValAReg:         UInt = RegInit(0.U(BusWidth.W))
  protected val rsValBReg:         UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:          Bool = RegInit(false.B)
  protected val wtAddrReg:         UInt = RegInit(0.U(RegAddrLen.W))
  protected val lsuFunc3MSBReg:    UInt = RegInit(0.U(1.W))
  protected val lsuWtEnaReg:       Bool = RegInit(false.B)

  //####################
  protected val instAddrReg: UInt = RegInit(0.U(BusWidth.W))
  protected val instDataReg: UInt = RegInit(0.U(InstWidth.W))

  instAddrReg     := io.instIn.addr
  instDataReg     := io.instIn.data
  io.instOut.addr := instAddrReg
  io.instOut.data := instDataReg
  //####################

  when(io.ifFlushIn) {
    diffIdSkipInstReg := true.B
    aluOperTypeReg    := aluNopType
    rsValAReg         := 0.U(BusWidth.W)
    rsValBReg         := 0.U(BusWidth.W)
    wtEnaReg          := false.B
    wtAddrReg         := 0.U(RegAddrLen.W)
    lsuFunc3MSBReg    := 0.U(1.W)
    lsuWtEnaReg       := false.B
  }.otherwise {
    diffIdSkipInstReg := false.B
    aluOperTypeReg    := io.idAluOperTypeIn
    rsValAReg         := io.idRsValAIn
    rsValBReg         := io.idRsValBIn
    wtEnaReg          := io.idWtEnaIn
    wtAddrReg         := io.idWtAddrIn
    lsuFunc3MSBReg    := io.lsuFunc3MSBIn
    lsuWtEnaReg       := io.lsuWtEnaIn
  }

  io.diffIdSkipInstOut := diffIdSkipInstReg
  io.exAluOperTypeOut  := aluOperTypeReg
  io.exRsValAOut       := rsValAReg
  io.exRsValBOut       := rsValBReg
  io.exWtEnaOut        := wtEnaReg
  io.exWtAddrOut       := wtAddrReg
  io.lsuFunc3MSBOut    := lsuFunc3MSBReg
  io.lsuWtEnaOut       := lsuWtEnaReg
}
