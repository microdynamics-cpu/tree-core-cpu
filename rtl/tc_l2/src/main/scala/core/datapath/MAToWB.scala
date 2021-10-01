package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class MAToWB extends Module with InstConfig {
  val io = IO(new Bundle {
    val wtIn:  TRANSIO = Flipped(new TRANSIO(RegAddrLen, BusWidth)) // from ma
    val wtOut: TRANSIO = new TRANSIO(RegAddrLen, BusWidth) // to wb

    val instIn:            INSTIO = new INSTIO
    val ifValidIn:         Bool   = Input(Bool())
    val ifMemInstCommitIn: Bool   = Input(Bool())
    // from clint
    val clintWt: TRANSIO = Flipped(new TRANSIO(BusWidth, BusWidth))
    // from csr
    val intrJumpInfo:     JUMPIO = Flipped(new JUMPIO)
    val memIntrEnterFlag: Bool   = Input(Bool())

    // to difftest
    val instOut:            INSTIO = Flipped(new INSTIO)
    val diffMaSkipInstOut:  Bool   = Output(Bool())
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

  resReg            := io.wtIn.data
  wtEnaReg          := io.wtIn.ena
  wtAddrReg         := io.wtIn.addr
  diffMaSkipInstReg := io.ifValidIn
  memInstCommitReg  := io.ifMemInstCommitIn

  when(RegNext(io.clintWt.ena)) {
    io.wtOut.data := RegNext(io.clintWt.data) // FIXME: need to refactor
    io.wtOut.ena  := true.B
  }.elsewhen(RegNext(RegNext(RegNext(io.intrJumpInfo.kind === csrJumpType)))) { // solve the no l/d inst invalid wt when trigger interrupt
    io.wtOut.data := 0.U
    io.wtOut.ena  := false.B
  }.elsewhen(io.ifMemInstCommitOut && io.memIntrEnterFlag) { // FIXME: solve l/d inst wt reg invalid
    io.wtOut.data := 0.U
    io.wtOut.ena  := false.B
  }.otherwise {
    io.wtOut.data := resReg
    io.wtOut.ena  := wtEnaReg
  }
  // io.wtOut.ena  := wtEnaReg
  io.wtOut.addr := wtAddrReg

  io.diffMaSkipInstOut  := diffMaSkipInstReg
  io.ifMemInstCommitOut := memInstCommitReg

}
