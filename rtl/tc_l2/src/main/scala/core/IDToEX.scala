package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class IDToEX extends Module with InstConfig {
  val io = IO(new Bundle {
    val idAluOperTypeIn:  UInt = Input(UInt(EXUOperTypeLen.W))
    val idRsValAIn:       UInt = Input(UInt(BusWidth.W))
    val idRsValBIn:       UInt = Input(UInt(BusWidth.W))
    val idWtEnaIn:        Bool = Input(Bool())
    val idWtAddrIn:       UInt = Input(UInt(RegAddrLen.W))
    val diffIfSkipInstIn: Bool = Input(Bool())

    val exAluOperTypeOut:  UInt = Output(UInt(EXUOperTypeLen.W))
    val exRsValAOut:       UInt = Output(UInt(BusWidth.W))
    val exRsValBOut:       UInt = Output(UInt(BusWidth.W))
    val exWtEnaOut:        Bool = Output(Bool())
    val exWtAddrOut:       UInt = Output(UInt(RegAddrLen.W))
    val diffIfSkipInstOut: Bool = Output(Bool())
  })

  protected val aluOperTypeReg:    UInt = RegInit(0.U(EXUOperTypeLen.W))
  protected val rsValAReg:         UInt = RegInit(0.U(BusWidth.W))
  protected val rsValBReg:         UInt = RegInit(0.U(BusWidth.W))
  protected val wtEnaReg:          Bool = RegInit(false.B)
  protected val wtAddrReg:         UInt = RegInit(0.U(RegAddrLen.W))
  protected val diffIfSkipInstReg: Bool = RegInit(false.B)

  aluOperTypeReg    := io.idAluOperTypeIn
  rsValAReg         := io.idRsValAIn
  rsValBReg         := io.idRsValBIn
  wtEnaReg          := io.idWtEnaIn
  wtAddrReg         := io.idWtAddrIn
  diffIfSkipInstReg := io.diffIfSkipInstIn

  io.exAluOperTypeOut  := aluOperTypeReg
  io.exRsValAOut       := rsValAReg
  io.exRsValBOut       := rsValBReg
  io.exWtEnaOut        := wtEnaReg
  io.exWtAddrOut       := wtAddrReg
  io.diffIfSkipInstOut := diffIfSkipInstReg

  // //@printf(p"[id2ex]this.reset = 0x${Hexadecimal(this.reset.asBool())}\n")
  // //@printf(p"[id2ex]io.idAluOperTypeIn = 0x${Hexadecimal(io.idAluOperTypeIn)}\n")

  //@printf(p"[id2ex]io.idWtEnaIn  = 0x${Hexadecimal(io.idWtEnaIn)}\n")
  //@printf(p"[id2ex]io.idWtAddrIn = 0x${Hexadecimal(io.idWtAddrIn)}\n")

  //@printf(p"[id2ex]io.exAluOperTypeOut = 0x${Hexadecimal(io.exAluOperTypeOut)}\n")
  //@printf(p"[id2ex]io.exWtEnaOut  = 0x${Hexadecimal(io.exWtEnaOut)}\n")
  //@printf(p"[id2ex]io.exWtAddrOut = 0x${Hexadecimal(io.exWtAddrOut)}\n")
}
