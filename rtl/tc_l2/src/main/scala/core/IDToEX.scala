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

  aluOperTypeRegister := io.idAluOperTypeIn
  rsValARegister      := io.idRsValAIn
  rsValBRegister      := io.idRsValBIn
  wtEnaRegister       := io.idWtEnaIn
  wtAddrRegister      := io.idWtAddrIn

  io.exAluOperTypeOut := aluOperTypeRegister
  io.exRsValAOut      := rsValARegister
  io.exRsValBOut      := rsValBRegister
  io.exWtEnaOut       := wtEnaRegister
  io.exWtAddrOut      := wtAddrRegister

  // //@printf(p"[id2ex]this.reset = 0x${Hexadecimal(this.reset.asBool())}\n")
  // //@printf(p"[id2ex]io.idAluOperTypeIn = 0x${Hexadecimal(io.idAluOperTypeIn)}\n")

  //@printf(p"[id2ex]io.idWtEnaIn  = 0x${Hexadecimal(io.idWtEnaIn)}\n")
  //@printf(p"[id2ex]io.idWtAddrIn = 0x${Hexadecimal(io.idWtAddrIn)}\n")

  //@printf(p"[id2ex]io.exAluOperTypeOut = 0x${Hexadecimal(io.exAluOperTypeOut)}\n")
  //@printf(p"[id2ex]io.exWtEnaOut  = 0x${Hexadecimal(io.exWtEnaOut)}\n")
  //@printf(p"[id2ex]io.exWtAddrOut = 0x${Hexadecimal(io.exWtAddrOut)}\n")
}
