package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class MAToWB extends Module with InstConfig {
  val io = IO(new Bundle {
    // from ma
    val maDataIn:          UInt   = Input(UInt(BusWidth.W))
    val maWtEnaIn:         Bool   = Input(Bool())
    val maWtAddrIn:        UInt   = Input(UInt(RegAddrLen.W))
    val ifValidIn:         Bool   = Input(Bool())
    val instIn:            INSTIO = new INSTIO
    val ifMemInstCommitIn: Bool   = Input(Bool())
    // from clint
    val clintWt: TRANSIO = Flipped(new TRANSIO)
    // to wb
    val wbDataOut:   UInt = Output(UInt(BusWidth.W))
    val wbWtEnaOut:  Bool = Output(Bool())
    val wbWtAddrOut: UInt = Output(UInt(RegAddrLen.W))
    // to difftest
    val diffMaSkipInstOut:  Bool   = Output(Bool())
    val instOut:            INSTIO = Flipped(new INSTIO)
    val ifMemInstCommitOut: Bool   = Output(Bool())
  })

  //####################
  protected val instAddrReg: UInt = RegInit(0.U(BusWidth.W))
  protected val instDataReg: UInt = RegInit(0.U(InstWidth.W))

  instAddrReg     := io.instIn.addr
  instDataReg     := io.instIn.data
  io.instOut.addr := instAddrReg
  io.instOut.data := instDataReg
  //####################

  protected val resReg:            UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:          Bool = RegInit(false.B)
  protected val wtAddrReg:         UInt = RegInit(0.U(RegAddrLen.W))
  protected val diffMaSkipInstReg: Bool = RegInit(false.B)
  protected val memInstCommitReg:  Bool = RegInit(false.B)

  resReg            := io.maDataIn
  wtEnaReg          := io.maWtEnaIn
  wtAddrReg         := io.maWtAddrIn
  diffMaSkipInstReg := io.ifValidIn
  memInstCommitReg  := io.ifMemInstCommitIn

  when(io.clintWt.ena) {
    io.wbDataOut := io.clintWt.data
  }.otherwise {
    io.wbDataOut := resReg
  }
  io.wbWtEnaOut  := wtEnaReg
  io.wbWtAddrOut := wtAddrReg

  io.diffMaSkipInstOut  := diffMaSkipInstReg
  io.ifMemInstCommitOut := memInstCommitReg

}
