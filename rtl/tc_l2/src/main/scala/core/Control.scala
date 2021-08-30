package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class Control extends Module with InstConfig {
  val io = IO(new Bundle {
    // from beu(solve jump)
    val jumpTypeIn:    UInt = Input(UInt(JumpTypeLen.W))
    val newInstAddrIn: UInt = Input(UInt(BusWidth.W))
    // from id(solve load coop)
    val stallReqFromIDIn: Bool = Input(Bool())

    val flushIfOut: Bool = Output(Bool())
    val stallIfOut: Bool = Output(Bool())
    val flushIdOut: Bool = Output(Bool())
    val ifJumpOut:  Bool = Output(Bool())
    // to pc
    val newInstAddrOut: UInt = Output(UInt(BusWidth.W))
  })

  io.flushIfOut     := false.B
  io.stallIfOut     := false.B
  io.flushIdOut     := false.B
  io.ifJumpOut      := false.B
  io.newInstAddrOut := io.newInstAddrIn // make all control signal come from this oen unit

  // 1. if branch type is jal or jalr, flush id stage
  // because the bsu is in id stage now, so unc/cond jump is same
  // 2. when jump and load correlation genearte in one time
  // load corrleation has higher priority, so io.stallReqFromIDIn is
  // in the first check statement
  when(io.stallReqFromIDIn) {
    io.flushIfOut := true.B
    io.flushIdOut := true.B
    io.stallIfOut := true.B
    io.ifJumpOut  := false.B // a case: when branch inst is after a load/store inst
  }.elsewhen(io.jumpTypeIn === uncJumpType || io.jumpTypeIn === condJumpType) {
    io.flushIfOut := true.B
    io.ifJumpOut  := true.B
  }
}
