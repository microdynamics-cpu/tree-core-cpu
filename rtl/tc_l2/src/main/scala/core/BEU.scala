package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._
import treecorel2.common.{getSignExtn, getZeroExtn}

class BEU extends Module with InstConfig {
  val io = IO(new Bundle {
    val exuOperNumIn:    UInt = Input(UInt(BusWidth.W))
    val exuOperTypeIn: UInt = Input(UInt(EXUOperTypeLen.W))

    // val rsValAIn: UInt = Input(UInt(BusWidth.W))
    // val rsValBIn: UInt = Input(UInt(BusWidth.W))
    val offsetIn: UInt = Input(UInt(BusWidth.W))

    val ifJumpOut:      Bool = Output(Bool())
    val newInstAddrOut: UInt = Output(UInt(BusWidth.W))
    val jumpTypeOut:    UInt = Output(UInt(JumpTypeLen.W))
  })

  io.ifJumpOut := MuxLookup(
    io.exuOperTypeIn,
    false.B,
    Seq(
      beuJALType -> true.B,
      beuJALRType -> true.B
    )
  )

  io.jumpTypeOut := uncJumpType

  protected val newInstAddr:  UInt = Wire(UInt(BusWidth.W))

  newInstAddr := io.exuOperNumIn + io.offsetIn
  io.newInstAddrOut := Mux(io.exuOperTypeIn === beuJALRType, newInstAddr & (~(1.U(BusWidth.W))), newInstAddr)
}
