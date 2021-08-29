package treecorel2

import chisel3._
import AXI4Bridge._

class PCReg extends Module with InstConfig {
  val io = IO(new Bundle {
    val ifJumpIn:      Bool = Input(Bool())
    val stallIfIn:     Bool = Input(Bool())
    val newInstAddrIn: UInt = Input(UInt(BusWidth.W))
    // axi signal
    val rwReadyIn: Bool = Input(Bool())
    val rwRespIn:  UInt = Input(UInt(AxiRespLen.W))
    val rdDataIn:  UInt = Input(UInt(AxiDataWidth.W))

    val instAddrOut: UInt = Output(UInt(BusWidth.W))
    val instDataOut: UInt = Output(UInt(InstWidth.W))
    val instEnaOut:  Bool = Output(Bool())
    // axi signal
    val rwValidOut: Bool = Output(Bool())
    val rwSizeOut:  UInt = Output(UInt(AxiSizeLen.W))
  })

  protected val hdShkDone = WireDefault(io.rwReadyIn && io.rwValidOut)
  protected val pc: UInt = RegInit(PcRegStartAddr.U(BusWidth.W))
  val dirty:        Bool = RegInit(false.B)

  io.rwRespIn    := DontCare
  io.instAddrOut := pc
  io.rwValidOut  := true.B
  io.rwSizeOut   := AXI4Bridge.SIZE_W

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
      io.instDataOut := io.rdDataIn(31, 0)
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
