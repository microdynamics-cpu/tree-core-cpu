package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.ConstVal

class AGU extends Module {
  val io = IO(new Bundle {
    val isa   = Input(new ISAIO)
    val src1  = Input(UInt(ConstVal.AddrLen.W))
    val src2  = Input(UInt(ConstVal.AddrLen.W))
    val valid = Output(Bool())
    val res   = Output(UInt(ConstVal.AddrLen.W))
  })

  // cordic or gcd
  // https://zhuanlan.zhihu.com/p/304477416
  // https://zhuanlan.zhihu.com/p/365058686
  protected val vala   = RegInit(0.U(64.W))
  protected val valb   = RegInit(0.U(64.W))
  protected val gcdVis = io.isa.GCD

  when(gcdVis && io.valid) {
    vala := io.src1
    valb := io.src2
  }.otherwise {
    when(vala > valb) {
      vala := vala - valb
    }.otherwise {
      valb := valb - vala
    }
  }

  io.valid := valb === 0.U(64.W)
  io.res   := vala
}
