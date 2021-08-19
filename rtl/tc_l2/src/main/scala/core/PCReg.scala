package treecorel2

import chisel3._

class PCReg extends Module with InstConfig {
  val io = IO(new Bundle {
    val ifJumpIn:      Bool = Input(Bool())
    val stallIfIn:     Bool = Input(Bool())
    val newInstAddrIn: UInt = Input(UInt(BusWidth.W))

    val instAddrOut: UInt = Output(UInt(BusWidth.W))
    val instEnaOut:  Bool = Output(Bool())
  })

  protected val pc: UInt = RegInit(PcRegStartAddr.U(BusWidth.W))

  when(io.ifJumpIn) {
    pc := io.newInstAddrIn
  }.elsewhen(io.stallIfIn) {
    pc := pc - 4.U // because the stallIFin is come from ex stage
  }.otherwise {
    pc := pc + 4.U
  }

  io.instAddrOut := pc
  io.instEnaOut  := !this.reset.asBool()

  printf(p"[pc]io.instAddrOut = 0x${Hexadecimal(io.instAddrOut)}\n")
  printf(p"[pc]io.stallIfIn = 0x${Hexadecimal(io.stallIfIn)}\n")
  printf(p"[pc]io.ifJumpIn = 0x${Hexadecimal(io.ifJumpIn)}\n")

  //@printf(p"[pc]io.instEnaOut = 0x${Hexadecimal(io.instEnaOut)}\n")
}
