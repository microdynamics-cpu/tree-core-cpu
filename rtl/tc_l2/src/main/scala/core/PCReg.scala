package treecorel2

import chisel3._
import AXI4Bridge._

class PCReg extends Module with InstConfig {
  val io = IO(new Bundle {
    val ifJumpIn:      Bool = Input(Bool())
    val stallIfIn:     Bool = Input(Bool())
    val newInstAddrIn: UInt = Input(UInt(BusWidth.W))
    // axi signal
    val instReadyIn:  Bool = Input(Bool())
    val instRespIn:   UInt = Input(UInt(AxiRespLen.W))
    val instRdDataIn: UInt = Input(UInt(AxiDataWidth.W))

    val instAddrOut: UInt = Output(UInt(BusWidth.W))
    val instDataOut: UInt = Output(UInt(InstWidth.W))
    val instEnaOut:  Bool = Output(Bool())
    // axi signal
    val instValidOut: Bool = Output(Bool())
    val instSizeOut:  UInt = Output(UInt(AxiSizeLen.W))
  })

  protected val hdShkDone = WireDefault(io.instReadyIn && io.instValidOut)
  protected val pc: UInt = RegInit(PcRegStartAddr.U(BusWidth.W))
  val dirty:        Bool = RegInit(false.B)

  io.instRespIn   := DontCare
  io.instAddrOut  := pc
  io.instValidOut := true.B
  io.instSizeOut  := AXI4Bridge.SIZE_W

  when(io.ifJumpIn) {
    pc    := io.newInstAddrIn
    dirty := true.B
  }.elsewhen(io.stallIfIn) {
    pc    := pc - 4.U // because the stallIFin is come from ex stage
    dirty := true.B
  }

  when(hdShkDone) {

    when(!dirty) {
      pc             := pc + 4.U
      io.instEnaOut  := true.B
      io.instDataOut := io.instRdDataIn(31, 0)
    }.otherwise {
      dirty          := false.B
      io.instEnaOut  := false.B
      io.instDataOut := NopInst.U
    }
  }.otherwise {
    io.instEnaOut  := false.B
    io.instDataOut := NopInst.U
  }
}
