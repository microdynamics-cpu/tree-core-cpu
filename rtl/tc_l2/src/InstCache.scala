package treecorel2

import chisel3._
import chisel3.util.Cat
import chisel3.util.experimental.loadMemoryFromFile

class InstCache extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instAddrIn:  UInt = Input(UInt(BusWidth.W))
    val instEnaIn:   Bool = Input(Bool())
    val instDataOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val cache = Mem(InstCacheLen, UInt(BusWidth.W))
  loadMemoryFromFile(cache, "data/InstcacheInit.txt")

  io.instDataOut := Mux(
    io.instEnaIn,
    Cat(cache(io.instAddrIn), cache(io.instAddrIn + 1.U), cache(io.instAddrIn + 2.U), cache(io.instAddrIn + 3.U)),
    0.U
  )
}
