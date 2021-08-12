package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._
import treecorel2.common.{getSignExtn, getZeroExtn}


object ALU {
  val aluADDIType  = 0.U(EXUOperTypeLen.W)
  val aluADDIWType = 1.U(EXUOperTypeLen.W)
  val aluSLTIType  = 2.U(EXUOperTypeLen.W)
  val aluSLTIUType = 3.U(EXUOperTypeLen.W)
  val aluANDIType  = 4.U(EXUOperTypeLen.W)
  val aluORIType   = 5.U(EXUOperTypeLen.W)
  val aluXORIType  = 6.U(EXUOperTypeLen.W)
  val aluSLLIType  = 7.U(EXUOperTypeLen.W)
  val aluSLLIWType = 8.U(EXUOperTypeLen.W)
  val aluSRLIType  = 9.U(EXUOperTypeLen.W)
  val aluSRLIWType = 10.U(EXUOperTypeLen.W)
  val aluSRAIType  = 11.U(EXUOperTypeLen.W)
  val aluSRAIWType = 12.U(EXUOperTypeLen.W)

  val aluLUIType   = 13.U(EXUOperTypeLen.W)
  val aluAUIPCType = 14.U(EXUOperTypeLen.W)

  val aluADDType  = 15.U(EXUOperTypeLen.W)
  val aluADDWType = 16.U(EXUOperTypeLen.W)
  val aluSLTType  = 17.U(EXUOperTypeLen.W)
  val aluSLTUType = 18.U(EXUOperTypeLen.W)
  val aluANDType  = 19.U(EXUOperTypeLen.W)
  val aluORType   = 20.U(EXUOperTypeLen.W)
  val aluXORType  = 21.U(EXUOperTypeLen.W)
  val aluSLLType  = 22.U(EXUOperTypeLen.W)
  val aluSLLWType = 23.U(EXUOperTypeLen.W)
  val aluSRLType  = 24.U(EXUOperTypeLen.W)
  val aluSRLWType = 25.U(EXUOperTypeLen.W)
  val aluSUBType  = 26.U(EXUOperTypeLen.W)
  val aluSUBWType = 27.U(EXUOperTypeLen.W)
  val aluSRAType  = 28.U(EXUOperTypeLen.W)
  val aluSRAWType = 29.U(EXUOperTypeLen.W)

  val aluNopType = 63.U(EXUOperTypeLen.W)
}

class ALU extends Module with InstConfig {
  val io = IO(new Bundle {
    val aluOperTypeIn: UInt = Input(UInt(EXUOperTypeLen.W))
    val rsValAIn:      UInt = Input(UInt(BusWidth.W))
    val rsValBIn:      UInt = Input(UInt(BusWidth.W))

    val resOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val res: UInt = Wire(UInt(BusWidth.W))

  res := MuxLookup(
    io.aluOperTypeIn,
    0.U,
    Seq(
      ALU.aluADDIType  -> (io.rsValAIn + io.rsValBIn),
      ALU.aluADDIWType -> (io.rsValAIn + io.rsValBIn),
      ALU.aluSLTIType  -> Cat(0.U((BusWidth - 1).W), io.rsValAIn.asSInt < io.rsValBIn.asSInt),
      ALU.aluSLTIUType -> (io.rsValAIn < io.rsValBIn),
      ALU.aluANDIType  -> (io.rsValAIn & io.rsValBIn),
      ALU.aluORIType   -> (io.rsValAIn | io.rsValBIn),
      ALU.aluXORIType  -> (io.rsValAIn ^ io.rsValBIn),
      ALU.aluSLLIType  -> (io.rsValAIn << io.rsValBIn(5, 0)),
      ALU.aluSLLIWType -> (io.rsValAIn << io.rsValBIn(4, 0)),
      ALU.aluSRLIType  -> (io.rsValAIn >> io.rsValBIn(5, 0)),
      ALU.aluSRLIWType -> (io.rsValAIn(31, 0) >> io.rsValBIn(4, 0)),
      ALU.aluSRAIType  -> ((io.rsValAIn.asSInt >> io.rsValBIn(5, 0)).asUInt),
      ALU.aluSRAIWType -> ((io.rsValAIn(31, 0).asSInt >> io.rsValBIn(4, 0)).asUInt),
      ALU.aluLUIType   -> (io.rsValBIn << 12),
      ALU.aluAUIPCType -> (io.rsValAIn + (io.rsValBIn << 12)),
      ALU.aluADDType   -> (io.rsValAIn + io.rsValBIn),
      ALU.aluADDWType  -> (io.rsValAIn + io.rsValBIn),
      ALU.aluSLTType   -> Cat(0.U((BusWidth - 1).W), io.rsValAIn.asSInt < io.rsValBIn.asSInt),
      ALU.aluSLTUType  -> (io.rsValAIn < io.rsValBIn),
      ALU.aluANDType   -> (io.rsValAIn & io.rsValBIn),
      ALU.aluORType    -> (io.rsValAIn | io.rsValBIn),
      ALU.aluXORType   -> (io.rsValAIn ^ io.rsValBIn),
      ALU.aluSLLType   -> (io.rsValAIn << io.rsValBIn(5, 0)),
      ALU.aluSLLWType  -> (io.rsValAIn << io.rsValBIn(4, 0)),
      ALU.aluSRLType   -> (io.rsValAIn >> io.rsValBIn(5, 0)),
      ALU.aluSRLWType  -> (io.rsValAIn(31, 0) >> io.rsValBIn(4, 0)),
      ALU.aluSUBType   -> (io.rsValAIn - io.rsValBIn),
      ALU.aluSUBWType  -> (io.rsValAIn - io.rsValBIn),
      ALU.aluSRAType   -> ((io.rsValAIn.asSInt >> io.rsValBIn(5, 0)).asUInt),
      ALU.aluSRAWType  -> ((io.rsValAIn(31, 0).asSInt >> io.rsValBIn(4, 0)).asUInt),
      ALU.aluNopType   -> (io.rsValAIn + io.rsValBIn)
    )
  )

  when(
    io.aluOperTypeIn === ALU.aluADDIWType ||
      io.aluOperTypeIn === ALU.aluSLLIWType ||
      io.aluOperTypeIn === ALU.aluSRLIWType ||
      io.aluOperTypeIn === ALU.aluSRAIWType ||
      io.aluOperTypeIn === ALU.aluADDWType ||
      io.aluOperTypeIn === ALU.aluSLLWType ||
      io.aluOperTypeIn === ALU.aluSRLWType ||
      io.aluOperTypeIn === ALU.aluSUBWType ||
      io.aluOperTypeIn === ALU.aluSRAWType
  ) {
    io.resOut := getSignExtn(BusWidth, res(31, 0))
  }.otherwise {
    io.resOut := res
  }

  //@printf(p"[ex]io.aluOperTypeIn = 0x${Hexadecimal(io.aluOperTypeIn)}\n")
  //@printf(p"[ex]io.rsValAIn = 0x${Hexadecimal(io.rsValAIn)}\n")
  //@printf(p"[ex]io.rsValBIn = 0x${Hexadecimal(io.rsValBIn)}\n")
  //@printf(p"[ex]io.resOut = 0x${Hexadecimal(io.resOut)}\n")
  //@printf("\n")
}
