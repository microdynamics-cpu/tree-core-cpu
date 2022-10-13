package treecorel2

import chisel3._
import chisel3.util._

// generate low speed clock
class Timer(div: Int = 5) extends Module {
  val io = IO(new Bundle {
    val tick = Output(Bool())
  })

  require(div > 0)
  require(div < 15)
  protected val clockCnt    = RegInit(1.U(4.W))
  protected val tickTrigger = clockCnt === div.U

  when(tickTrigger) {
    clockCnt := 1.U
  }.otherwise {
    clockCnt := clockCnt + 1.U
  }

  io.tick := tickTrigger
}
