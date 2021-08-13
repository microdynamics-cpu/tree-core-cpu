package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class Control extends Module with InstConfig {
  val io = IO(new Bundle {
    val jumpTypeIn: UInt = Input(UInt(JumpTypeLen.W))

    val flushIfOut: Bool = Output(Bool())
    // val flushIdOut: Bool = Output(Bool())
    // val flushExOut: Bool = Output(Bool())
  })

  io.flushIfOut := false.B
  // io.flushIdOut := false.B
  // io.flushExOut := false.B

  // if branch type is jal or jalr, flush id stage
  when(io.jumpTypeIn === uncJumpType) {
    io.flushIfOut := true.B
  }.elsewhen(io.jumpTypeIn === condJumpType) {}.otherwise {}
}
