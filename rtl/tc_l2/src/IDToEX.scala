package treecorel2

import chisel3._

class IDToEX extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val idAluOperTypeIn: UInt = Input(UInt(ALUOperTypeLen.W))
    val idRsValAIn:      UInt = Input(UInt(BusWidth.W))
    val idRsValBIn:      UInt = Input(UInt(BusWidth.W))
    val idWtEnaIn:       Bool = Input(Bool())
    val idWtAddrIn:      UInt = Input(UInt(RegAddrLen.W))

    val exAluOperTypeOut: UInt = Output(UInt(ALUOperTypeLen.W))
    val exRsValAOut:      UInt = Output(UInt(BusWidth.W))
    val exRsValBOut:      UInt = Output(UInt(BusWidth.W))
    val exWtEnaOut:       Bool = Output(Bool())
    val exWtAddrOut:      UInt = Output(UInt(RegAddrLen.W))
  })

  protected val aluOperTypeRegister: UInt = RegInit(0.U(ALUOperTypeLen.W))
  protected val rsValARegister:      UInt = RegInit(0.U(BusWidth.W))
  protected val rsValBRegister:      UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaRegister:       Bool = RegInit(false.B)
  protected val wtAddrRegister:      UInt = RegInit(0.U(RegAddrLen.W))

  aluOperTypeRegister := Mux(this.reset.asBool(), 0.U(ALUOperTypeLen.W), io.idAluOperTypeIn)
  rsValARegister      := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.idRsValAIn)
  rsValBRegister      := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.idRsValBIn)
  wtEnaRegister       := Mux(this.reset.asBool(), false.B, io.idWtEnaIn)
  wtAddrRegister      := Mux(this.reset.asBool(), 0.U(RegAddrLen.W), io.idWtAddrIn)

  io.exAluOperTypeOut := aluOperTypeRegister
  io.exRsValAOut      := rsValARegister
  io.exRsValBOut      := rsValBRegister
  io.exWtEnaOut       := wtEnaRegister
  io.exWtAddrOut      := wtAddrRegister
}
