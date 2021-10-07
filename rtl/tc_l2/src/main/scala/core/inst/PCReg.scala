package treecorel2

import chisel3._
import AXI4Bridge._

class PCReg() extends Module with AXI4Config {
  val io = IO(new Bundle {
    val axi:         AXI4USERIO = Flipped(new AXI4USERIO) // from axi
    val ctrl2pc:     CTRL2PCIO  = Flipped(new CTRL2PCIO) // from ctrl
    val instEnaOut:  Bool       = Output(Bool())
    val instDataOut: UInt       = Output(UInt(InstWidth.W)) // to id
  })

  protected val hdShkDone: Bool = WireDefault(io.axi.ready && io.axi.valid)
  protected val pc:        UInt = if (SoCEna) RegInit(PCFlashStartAddr.U(BusWidth.W)) else RegInit(PCLoadStartAddr.U(BusWidth.W))
  protected val dirty:     Bool = RegInit(false.B)
  protected val socRdCnt:  UInt = RegInit(0.U(1.W))
  protected val validctrl: Bool = RegInit(false.B)

  // fix the ready sig is triggered when mem access bug
  when(!io.axi.valid && io.axi.ready) {
    validctrl := true.B
  }
  // now we dont handle this resp info to check if the read oper is right
  // tmp
  protected val tmpStall = if (SoCEna) RegNext(~io.ctrl2pc.maStall) else ~io.ctrl2pc.maStall
  io.axi.req   := 0.U // 0: read 1: write
  io.axi.wdata := DontCare
  io.axi.resp  := DontCare
  io.axi.addr  := pc
  io.axi.id    := 0.U
  io.axi.valid := tmpStall // TODO: maybe this code lead to cycle
  io.axi.size  := AXI4Bridge.SIZE_W

  when(io.ctrl2pc.jump) {
    pc    := io.ctrl2pc.newPC
    dirty := true.B
  }.elsewhen(io.ctrl2pc.stall) {
    pc    := pc - 4.U(BusWidth.W) // because the stallIFin is come from ex stage
    dirty := true.B
  }.elsewhen(io.ctrl2pc.maStall) {
    pc    := pc
    dirty := true.B
  }

  when(SoCEna.B && socRdCnt === 0.U && hdShkDone && pc >= "h8000_0000".U) {
    socRdCnt       := socRdCnt + 1.U
    io.instEnaOut  := false.B
    io.instDataOut := NopInst.U
  }.otherwise {
    when(hdShkDone || (io.axi.valid && validctrl)) {
      socRdCnt := 0.U
      when(!dirty) {
        pc            := pc + 4.U(BusWidth.W)
        io.instEnaOut := true.B
        // printf("handshake done!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
        // printf(p"[pc]io.axi.rdata = 0x${Hexadecimal(io.axi.rdata)}\n")
        io.instDataOut := io.axi.rdata(31, 0)
        validctrl      := false.B
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
}
