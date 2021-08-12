package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._
import treecorel2.common.{getSignExtn, getZeroExtn}


object BEU {

}

class BEU extends Module with InstConfig {
  val io = IO(new Bundle {
    val beuOperTypeIn: UInt = Input(UInt(EXUOperTypeLen.W))
    val rsValAIn:      UInt = Input(UInt(BusWidth.W))
    val rsValBIn:      UInt = Input(UInt(BusWidth.W))

    val ifJump: Bool = Output(Bool())
    val newPC:  UInt = Output(UInt(BusWidth.W))
  })
}
