package treecorel2

import chisel3._
import chisel3.util._

class Multiplier(val len: Int, val latency: Int = 0) extends Module {
  val io = IO(new Bundle {
    val en    = Input(Bool())
    val flush = Input(Bool())
    val done  = Output(Bool())
    val src1  = Input(UInt(len.W))
    val src2  = Input(UInt(len.W))
    val res   = Output(UInt((len * 2).W))
  })

  require(latency >= 0)

  protected val res = io.src1 * io.src2

  def generatePipe(en: Bool, data: UInt, latency: Int): (Bool, UInt) = {
    if (latency == 0) {
      (en, data)
    } else {
      val done = RegNext(Mux(io.flush, false.B, en), false.B)
      val bits = RegEnable(data, en)
      generatePipe(done, bits, latency - 1)
    }
  }

  protected val (en, data) = generatePipe(io.en, res, latency)
  io.done := en
  io.res  := data
}
