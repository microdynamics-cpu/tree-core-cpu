package treecorel2

import chisel3._

class ForWard extends Module with InstConfig {
  val io = IO(new Bundle {
    val exIn: TRANSIO = Flipped(new TRANSIO(RegAddrLen, BusWidth)) // from ex
    val maIn: TRANSIO = Flipped(new TRANSIO(RegAddrLen, BusWidth)) // from ma

    // from regfile
    val idRdEnaAIn:  Bool = Input(Bool())
    val idRdAddrAIn: UInt = Input(UInt(RegAddrLen.W))
    val idRdEnaBIn:  Bool = Input(Bool())
    val idRdAddrBIn: UInt = Input(UInt(RegAddrLen.W))
    // to id
    val fwRsEnaAOut: Bool = Output(Bool())
    val fwRsValAOut: UInt = Output(UInt(BusWidth.W))
    val fwRsEnaBOut: Bool = Output(Bool())
    val fwRsValBOut: UInt = Output(UInt(BusWidth.W))
  })

  //  =/= for addi x0, x1, 0x30 bypass can cause error
  when(io.idRdAddrAIn === io.exIn.addr && io.idRdAddrAIn =/= 0.U(RegAddrLen.W) && io.idRdEnaAIn && io.exIn.ena) {
    io.fwRsValAOut := io.exIn.data
    io.fwRsEnaAOut := true.B
  }.elsewhen(io.idRdAddrAIn === io.maIn.addr && io.idRdAddrAIn =/= 0.U(RegAddrLen.W) && io.idRdEnaAIn && io.maIn.ena) {
    io.fwRsValAOut := io.maIn.data
    io.fwRsEnaAOut := true.B
  }.otherwise {
    io.fwRsValAOut := 0.U(BusWidth.W)
    io.fwRsEnaAOut := false.B
  }

  when(io.idRdAddrBIn === io.exIn.addr && io.idRdAddrBIn =/= 0.U(RegAddrLen.W) && io.idRdEnaBIn && io.exIn.ena) {
    io.fwRsValBOut := io.exIn.data
    io.fwRsEnaBOut := true.B
  }.elsewhen(io.idRdAddrBIn === io.maIn.addr && io.idRdAddrBIn =/= 0.U(RegAddrLen.W) && io.idRdEnaBIn && io.maIn.ena) {
    io.fwRsValBOut := io.maIn.data
    io.fwRsEnaBOut := true.B
  }.otherwise {
    io.fwRsValBOut := 0.U(BusWidth.W)
    io.fwRsEnaBOut := false.B
  }
}
