package treecorel2

import chisel3._

class PCRegister extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instAddrOut: UInt = Output(UInt(BusWidth.W))
    val instEnaOut:  Bool = Output(Bool())
  })

  protected val pc: UInt = RegInit(PcRegStartAddr.U(BusWidth.W))
  pc             := pc + 4.U
  io.instAddrOut := pc
  io.instEnaOut  := !this.reset.asBool()
}
