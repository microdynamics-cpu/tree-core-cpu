package treecorel2

import chisel3._
import AXI4Bridge._

class PCReg extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    // from control
    val ifJumpIn:  Bool = Input(Bool())
    val stallIfIn: Bool = Input(Bool())
    // from id
    val newInstAddrIn: UInt = Input(UInt(BusWidth.W))
    // from axi
    val axi: AXI4USERIO = Flipped(new AXI4USERIO)
    // to id
    val instDataOut: UInt = Output(UInt(InstWidth.W))
    val instEnaOut:  Bool = Output(Bool())
  })
  // tmp
  io.axi.req   := 0.U // 0: read 1: write
  io.axi.wdata := DontCare

  protected val hdShkDone: Bool = WireDefault(io.axi.ready && io.axi.valid)
  protected val pc:        UInt = RegInit(PcRegStartAddr.U(BusWidth.W))
  protected val dirty:     Bool = RegInit(false.B)

  // now we dont handle this resp info to check if the read oper is right
  io.axi.resp := DontCare
  io.axi.addr := pc
  // io.axi.valid := true.B // TODO: need to judge when mem need to read
  io.axi.valid := ~io.stallIfIn
  io.axi.size  := AXI4Bridge.SIZE_W

  when(io.ifJumpIn) {
    pc    := io.newInstAddrIn
    dirty := true.B
  }.elsewhen(io.stallIfIn) {
    pc    := pc - 4.U(BusWidth.W) // because the stallIFin is come from ex stage
    dirty := true.B
  }

  when(hdShkDone) {
    when(!dirty) {
      pc             := pc + 4.U(BusWidth.W)
      io.instEnaOut  := true.B
      io.instDataOut := io.axi.rdata(31, 0)
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
