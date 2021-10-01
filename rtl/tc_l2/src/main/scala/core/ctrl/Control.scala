package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class Control extends Module with InstConfig {
  val io = IO(new Bundle {
    // from csr(solve jump to exception/interrupt handle func addr)
    val excpJumpInfo: JUMPIO = Flipped(new JUMPIO)
    val intrJumpInfo: JUMPIO = Flipped(new JUMPIO)
    // from beu(solve jump to func addr)
    val jumpTypeIn:    UInt = Input(UInt(JumpTypeLen.W))
    val newInstAddrIn: UInt = Input(UInt(BusWidth.W))
    // from id(solve load coop)
    val stallReqFromIDIn: Bool = Input(Bool())
    // from ma(solve axi load/store)
    val stallReqFromMaIn: Bool      = Input(Bool())
    val ctrl2pc:          CTRL2PCIO = new CTRL2PCIO // to if

    val flushIfOut: Bool = Output(Bool())
    val flushIdOut: Bool = Output(Bool())
  })

  io.ctrl2pc.jump    := false.B
  io.ctrl2pc.stall   := false.B
  io.ctrl2pc.maStall := false.B
  io.flushIfOut      := false.B
  io.flushIdOut      := false.B

  when(io.excpJumpInfo.kind === csrJumpType) {
    io.ctrl2pc.newPC := io.excpJumpInfo.addr
  }.elsewhen(io.intrJumpInfo.kind === csrJumpType) {
    io.ctrl2pc.newPC := io.intrJumpInfo.addr
  }.otherwise {
    io.ctrl2pc.newPC := io.newInstAddrIn // make all control signal come from this oen unit
  }

  // 1. if branch type is jal or jalr, flush id stage
  // because the bsu is in id stage now, so unc/cond jump is same
  // 2. when jump and load correlation genearte in one time
  // load corrleation has higher priority, so io.stallReqFromIDIn is
  // in the first check statement
  when(io.stallReqFromIDIn) {
    io.flushIfOut    := true.B
    io.flushIdOut    := true.B
    io.ctrl2pc.stall := true.B
    io.ctrl2pc.jump  := false.B // a case: when branch inst is after a load/store inst
  }.elsewhen(
    io.jumpTypeIn === uncJumpType ||
      io.jumpTypeIn === condJumpType ||
      io.excpJumpInfo.kind === csrJumpType ||
      io.intrJumpInfo.kind === csrJumpType
  ) {
    io.flushIfOut   := true.B
    io.ctrl2pc.jump := true.B
  }

  when(io.stallReqFromMaIn) {
    io.ctrl2pc.maStall := true.B
  }
}
