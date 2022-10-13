package treecorel2

import chisel3._
import chisel3.util._

class StallControl extends Module with InstConfig {
  val io = IO(new Bundle {
    val globalEn = Input(Bool())
    val stall    = Input(Bool())
    val st1      = Output(Bool())
    val st2      = Output(Bool())
    val st3      = Output(Bool())
  })

  protected val (tickCnt, cntWrap) = Counter(io.globalEn && io.stall, 3)
  protected val cyc1               = io.stall && (tickCnt === 0.U)
  protected val cyc2               = io.stall && (tickCnt === 1.U)
  protected val cyc3               = io.stall && (tickCnt === 2.U)

  io.st1 := cyc1
  io.st2 := cyc2
  io.st3 := cyc3
}
