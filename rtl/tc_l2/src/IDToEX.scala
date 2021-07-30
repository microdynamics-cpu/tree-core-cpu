package treecorel2

import chisel3._

class IDToEX extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val idAluOperTypeIn: UInt = Input(UInt(ALUOperTypeLen.W))
    val idRsValAIn:      UInt = Input(UInt(BusWidth.W))
    val idRsValBIn:      UInt = Input(UInt(BusWidth.W))

    val exAluOperTypeOut: UInt = Output(UInt(ALUOperTypeLen.W))
    val exRsValAOut:      UInt = Output(UInt(BusWidth.W))
    val exRsValBOut:      UInt = Output(UInt(BusWidth.W))
  })

  private val aluOperTypeRegister: UInt = RegInit(0.U(BusWidth.W))
  private val rsValARegister:      UInt = RegInit(0.U(BusWidth.W))
  private val rsValBRegister:      UInt = RegInit(0.U(BusWidth.W))

  aluOperTypeRegister := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.idAluOperTypeIn)
  rsValARegister      := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.idRsValAIn)
  rsValBRegister      := Mux(this.reset.asBool(), 0.U(BusWidth.W), io.idRsValBIn)

  io.exAluOperTypeOut := aluOperTypeRegister
  io.exRsValAOut      := rsValARegister
  io.exRsValBOut      := rsValBRegister
}
