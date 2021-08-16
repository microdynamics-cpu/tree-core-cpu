package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._
import treecorel2.common.{getSignExtn, getZeroExtn}

class BEU extends Module with InstConfig {
  val io = IO(new Bundle {
    val exuOperNumIn:  UInt = Input(UInt(BusWidth.W))
    val exuOperTypeIn: UInt = Input(UInt(InstOperTypeLen.W))

    val rsValAIn: UInt = Input(UInt(BusWidth.W))
    val rsValBIn: UInt = Input(UInt(BusWidth.W))
    val offsetIn: UInt = Input(UInt(BusWidth.W))

    val ifJumpOut:      Bool = Output(Bool())
    val newInstAddrOut: UInt = Output(UInt(BusWidth.W))
    val jumpTypeOut:    UInt = Output(UInt(JumpTypeLen.W))
  })

  // pass it to if stage to set new pc
  io.ifJumpOut := MuxLookup(
    io.exuOperTypeIn,
    false.B,
    Seq(
      beuJALType  -> true.B,
      beuJALRType -> true.B,
      beuBEQType  -> (io.rsValAIn === io.rsValBIn),
      beuBNEType  -> (io.rsValAIn =/= io.rsValBIn),
      beuBLTType  -> (io.rsValAIn.asSInt() < io.rsValBIn.asSInt()),
      beuBLTUType -> (io.rsValAIn < io.rsValBIn),
      beuBGEType  -> (io.rsValAIn.asSInt() >= io.rsValBIn.asSInt()),
      beuBGEUType -> (io.rsValAIn >= io.rsValBIn)
    )
  )

  //pass it to control to flush pipeline
  io.jumpTypeOut := MuxLookup(
    io.exuOperTypeIn,
    noJumpType,
    Seq(
      beuJALType  -> uncJumpType,
      beuJALRType -> uncJumpType,
      beuBEQType  -> Mux(io.ifJumpOut, condJumpType, noJumpType),
      beuBNEType  -> Mux(io.ifJumpOut, condJumpType, noJumpType),
      beuBLTType  -> Mux(io.ifJumpOut, condJumpType, noJumpType),
      beuBLTUType -> Mux(io.ifJumpOut, condJumpType, noJumpType),
      beuBGEType  -> Mux(io.ifJumpOut, condJumpType, noJumpType),
      beuBGEUType -> Mux(io.ifJumpOut, condJumpType, noJumpType)
    )
  )
  // io.jumpTypeOut := uncJumpType

  protected val newInstAddr: UInt = Wire(UInt(BusWidth.W))

  // if no jump, the newInstAddr is zero
  newInstAddr       := io.exuOperNumIn + io.offsetIn
  io.newInstAddrOut := Mux(io.exuOperTypeIn === beuJALRType, newInstAddr & (~(1.U(BusWidth.W))), newInstAddr)

  //@printf(p"[beu]io.ifJumpOut = 0x${Hexadecimal(io.ifJumpOut)}\n")
  //@printf(p"[beu]io.jumpTypeOut = 0x${Hexadecimal(io.jumpTypeOut)}\n")
  // printf(p"[beu]~(1.U(BusWidth.W) = 0x${Hexadecimal(~(1.U(BusWidth.W)))}\n")
  // printf(p"[beu]io.exuOperNumIn = 0x${Hexadecimal(io.exuOperNumIn)}\n")
  // printf(p"[beu]io.offsetIn = 0x${Hexadecimal(io.offsetIn)}\n")
  // printf(p"[beu]io.newInstAddrOut(pre) = 0x${Hexadecimal(io.exuOperNumIn + io.offsetIn)}\n")
  // printf(p"[beu]io.newInstAddrOut(aft) = 0x${Hexadecimal(io.newInstAddrOut)}\n")
}
