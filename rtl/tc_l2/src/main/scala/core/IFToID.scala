package treecorel2

import chisel3._

class IFToID extends Module with InstConfig {
  val io = IO(new Bundle {
    val ifInstAddrIn: UInt = Input(UInt(BusWidth.W))
    val ifInstDataIn: UInt = Input(UInt(InstWidth.W))

    val ifFlushIn: Bool = Input(Bool())

    val idInstAddrOut:     UInt = Output(UInt(BusWidth.W))
    val idInstDataOut:     UInt = Output(UInt(InstWidth.W))
    val diffIfSkipInstOut: Bool = Output(Bool())
  })

  protected val pcReg:             UInt = RegInit(0.U(BusWidth.W))
  protected val instReg:           UInt = RegInit(0.U(InstWidth.W))
  protected val diffIfSkipInstReg: Bool = RegInit(false.B)

  pcReg := io.ifInstAddrIn

  when(io.ifFlushIn) {
    instReg           := NopInst.U
    diffIfSkipInstReg := true.B
  }.otherwise {
    instReg           := io.ifInstDataIn
    diffIfSkipInstReg := false.B
  }

  io.idInstAddrOut     := pcReg
  io.idInstDataOut     := instReg
  io.diffIfSkipInstOut := diffIfSkipInstReg // RegNext is important!!!
}
