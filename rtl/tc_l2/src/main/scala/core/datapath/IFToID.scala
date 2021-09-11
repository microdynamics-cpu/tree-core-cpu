package treecorel2

import chisel3._

class IFToID extends Module with InstConfig {
  val io = IO(new Bundle {
    // from if and control
    val instIn:    INSTIO = new INSTIO
    val ifFlushIn: Bool   = Input(Bool())

    // to id
    val instOut:           INSTIO = Flipped(new INSTIO)
    val diffIfSkipInstOut: Bool   = Output(Bool())
  })

  protected val pcReg:             UInt = RegInit(0.U(BusWidth.W))
  protected val instReg:           UInt = RegInit(0.U(InstWidth.W))
  protected val diffIfSkipInstReg: Bool = RegInit(false.B)

  pcReg := io.instIn.addr
  when(io.ifFlushIn) {
    instReg           := NopInst.U
    diffIfSkipInstReg := true.B
  }.otherwise {
    instReg           := io.instIn.data
    diffIfSkipInstReg := false.B
  }

  io.instOut.addr      := pcReg
  io.instOut.data      := instReg
  io.diffIfSkipInstOut := diffIfSkipInstReg // RegNext is important!!!
}
