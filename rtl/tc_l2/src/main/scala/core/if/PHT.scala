package treecorel2

import chisel3._
import chisel3.util._

class PHT extends Module with InstConfig {
  val io = IO(new Bundle {
    // branch info (from idu)
    val prevBranch = Input(Bool())
    val prevTaken  = Input(Bool())
    val prevIdx    = Input(UInt(GHRLen.W))
    // index for looking up counter table
    val idx = Input(UInt(GHRLen.W))
    // prediction result
    val taken = Output(Bool())
  })

  protected val init     = Seq.fill(PHTSize) { "b10".U(2.W) }
  protected val counters = RegInit(VecInit(init))

  // update counter
  when(io.prevBranch) {
    when(counters(io.prevIdx) === "b11".U) {
      when(!io.prevTaken) {
        counters(io.prevIdx) := counters(io.prevIdx) - 1.U
      }
    }.elsewhen(counters(io.prevIdx) === "b00".U) {
      when(io.prevTaken) {
        counters(io.prevIdx) := counters(io.prevIdx) + 1.U
      }
    }.otherwise {
      when(!io.prevTaken) {
        counters(io.prevIdx) := counters(io.prevIdx) - 1.U
      }.otherwise {
        counters(io.prevIdx) := counters(io.prevIdx) + 1.U
      }
    }
  }

  io.taken := counters(io.idx)(1)
}
