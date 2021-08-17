package treecorel2

import chisel3._

class ForWard extends Module with InstConfig {
  val io = IO(new Bundle {
    val exDataIn:   UInt = Input(UInt(BusWidth.W))
    val exWtEnaIn:  Bool = Input(Bool())
    val exWtAddrIn: UInt = Input(UInt(RegAddrLen.W))

    val maDataIn:   UInt = Input(UInt(BusWidth.W))
    val maWtEnaIn:  Bool = Input(Bool())
    val maWtAddrIn: UInt = Input(UInt(RegAddrLen.W))

    val idRdEnaAIn:  Bool = Input(Bool())
    val idRdAddrAIn: UInt = Input(UInt(RegAddrLen.W))
    val idRdEnaBIn:  Bool = Input(Bool())
    val idRdAddrBIn: UInt = Input(UInt(RegAddrLen.W))

    val fwRsEnaAOut: Bool = Output(Bool())
    val fwRsValAOut: UInt = Output(UInt(BusWidth.W))
    val fwRsEnaBOut: Bool = Output(Bool())
    val fwRsValBOut: UInt = Output(UInt(BusWidth.W))
  })

  //  =/= for addi x0, x1, 0x30 bypass can cause error
  when(io.idRdAddrAIn === io.exWtAddrIn && io.idRdAddrAIn =/= 0.U && io.idRdEnaAIn && io.exWtEnaIn) {
    io.fwRsValAOut := io.exDataIn
    io.fwRsEnaAOut := true.B
  }.elsewhen(io.idRdAddrAIn === io.maWtAddrIn && io.idRdAddrAIn =/= 0.U && io.idRdEnaAIn && io.maWtEnaIn) {
    io.fwRsValAOut := io.maDataIn
    io.fwRsEnaAOut := true.B
  }.otherwise {
    io.fwRsValAOut := 0.U
    io.fwRsEnaAOut := false.B
  }

  when(io.idRdAddrBIn === io.exWtAddrIn && io.idRdAddrBIn =/= 0.U && io.idRdEnaBIn && io.exWtEnaIn) {
    io.fwRsValBOut := io.exDataIn
    io.fwRsEnaBOut := true.B
  }.elsewhen(io.idRdAddrBIn === io.maWtAddrIn && io.idRdAddrBIn =/= 0.U && io.idRdEnaBIn && io.maWtEnaIn) {
    io.fwRsValBOut := io.maDataIn
    io.fwRsEnaBOut := true.B
  }.otherwise {
    io.fwRsValBOut := 0.U
    io.fwRsEnaBOut := false.B
  }

}
