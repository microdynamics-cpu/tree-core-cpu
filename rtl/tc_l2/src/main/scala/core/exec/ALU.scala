package treecorel2

import chisel3._
import chisel3.util.{Cat, MuxLookup}
import treecorel2.common.ConstVal._
import treecorel2.common.{getSignExtn, getZeroExtn}

class ALU extends Module with InstConfig {
  val io = IO(new Bundle {
    // from id
    val exuOperTypeIn: UInt = Input(UInt(InstOperTypeLen.W))
    val rsValAIn:      UInt = Input(UInt(BusWidth.W))
    val rsValBIn:      UInt = Input(UInt(BusWidth.W))
    // from csr
    val csrRdDataIn: UInt = Input(UInt(BusWidth.W))

    // to ex2ma
    val wtDataOut: UInt = Output(UInt(BusWidth.W))
    // to csr
    val csrwtEnaOut:  Bool = Output(Bool())
    val csrWtDataOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val res: UInt = Wire(UInt(BusWidth.W))

  res := MuxLookup(
    io.exuOperTypeIn,
    0.U((BusWidth.W)),
    Seq(
      aluADDIType  -> (io.rsValAIn + io.rsValBIn),
      aluADDIWType -> (io.rsValAIn + io.rsValBIn),
      aluSLTIType  -> Cat(0.U((BusWidth - 1).W), io.rsValAIn.asSInt < io.rsValBIn.asSInt),
      aluSLTIUType -> (io.rsValAIn < io.rsValBIn),
      aluANDIType  -> (io.rsValAIn & io.rsValBIn),
      aluORIType   -> (io.rsValAIn | io.rsValBIn),
      aluXORIType  -> (io.rsValAIn ^ io.rsValBIn),
      aluSLLIType  -> (io.rsValAIn << io.rsValBIn(5, 0)),
      aluSLLIWType -> (io.rsValAIn << io.rsValBIn(4, 0)),
      aluSRLIType  -> (io.rsValAIn >> io.rsValBIn(5, 0)),
      aluSRLIWType -> (io.rsValAIn(31, 0) >> io.rsValBIn(4, 0)),
      aluSRAIType  -> ((io.rsValAIn.asSInt >> io.rsValBIn(5, 0)).asUInt),
      aluSRAIWType -> ((io.rsValAIn(31, 0).asSInt >> io.rsValBIn(4, 0)).asUInt),
      aluLUIType   -> (io.rsValBIn << 12),
      aluAUIPCType -> (io.rsValAIn + (io.rsValBIn << 12)),
      aluADDType   -> (io.rsValAIn + io.rsValBIn),
      aluADDWType  -> (io.rsValAIn + io.rsValBIn),
      aluSLTType   -> Cat(0.U((BusWidth - 1).W), io.rsValAIn.asSInt < io.rsValBIn.asSInt),
      aluSLTUType  -> (io.rsValAIn < io.rsValBIn),
      aluANDType   -> (io.rsValAIn & io.rsValBIn),
      aluORType    -> (io.rsValAIn | io.rsValBIn),
      aluXORType   -> (io.rsValAIn ^ io.rsValBIn),
      aluSLLType   -> (io.rsValAIn << io.rsValBIn(5, 0)),
      aluSLLWType  -> (io.rsValAIn << io.rsValBIn(4, 0)),
      aluSRLType   -> (io.rsValAIn >> io.rsValBIn(5, 0)),
      aluSRLWType  -> (io.rsValAIn(31, 0) >> io.rsValBIn(4, 0)),
      aluSUBType   -> (io.rsValAIn - io.rsValBIn),
      aluSUBWType  -> (io.rsValAIn - io.rsValBIn),
      aluSRAType   -> ((io.rsValAIn.asSInt >> io.rsValBIn(5, 0)).asUInt),
      aluSRAWType  -> ((io.rsValAIn(31, 0).asSInt >> io.rsValBIn(4, 0)).asUInt),
      // special jal and jalr oper
      beuJALType  -> (io.rsValAIn + io.rsValBIn),
      beuJALRType -> (io.rsValAIn + io.rsValBIn),
      aluNopType  -> (io.rsValAIn + io.rsValBIn),
      // csr inst
      csrRWType  -> io.csrRdDataIn,
      csrRSType  -> io.csrRdDataIn,
      csrRCType  -> io.csrRdDataIn,
      csrRWIType -> io.csrRdDataIn,
      csrRSIType -> io.csrRdDataIn,
      csrRCIType -> io.csrRdDataIn
    )
  )

  when(
    io.exuOperTypeIn === aluADDIWType ||
      io.exuOperTypeIn === aluSLLIWType ||
      io.exuOperTypeIn === aluSRLIWType ||
      io.exuOperTypeIn === aluSRAIWType ||
      io.exuOperTypeIn === aluADDWType ||
      io.exuOperTypeIn === aluSLLWType ||
      io.exuOperTypeIn === aluSRLWType ||
      io.exuOperTypeIn === aluSUBWType ||
      io.exuOperTypeIn === aluSRAWType
  ) {
    io.wtDataOut := getSignExtn(BusWidth, res(31, 0), res(31))
  }.otherwise {
    io.wtDataOut := res
  }

  when(io.exuOperTypeIn === csrRWType || io.exuOperTypeIn === csrRWIType) {
    io.csrwtEnaOut  := true.B
    io.csrWtDataOut := io.rsValAIn

  }.elsewhen(io.exuOperTypeIn === csrRSType || io.exuOperTypeIn === csrRSIType) {
    io.csrwtEnaOut  := true.B
    io.csrWtDataOut := io.wtDataOut | io.rsValAIn
  }.elsewhen(io.exuOperTypeIn === csrRCType || io.exuOperTypeIn === csrRCIType) {
    io.csrwtEnaOut  := true.B
    io.csrWtDataOut := io.wtDataOut & (~io.rsValAIn)
  }.otherwise {
    io.csrwtEnaOut  := false.B
    io.csrWtDataOut := 0.U(BusWidth.W)
  }
}
