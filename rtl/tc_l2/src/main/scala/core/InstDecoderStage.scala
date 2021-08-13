package treecorel2

import chisel3._
import chisel3.util._
import treecorel2.common.ConstVal._
import InstRegexPattern._

object InstDecoderStage {
  protected val wtRegFalse = false.B
  protected val wtRegTrue  = true.B

  // inst type
  // nop is equal to [addi x0, x0, 0], so the oper is same as 'addi' inst
  val nopInstType = 2.U(InstTypeLen.W)
  val rInstType   = 1.U(InstTypeLen.W)
  val iInstType   = 2.U(InstTypeLen.W)
  val sInstType   = 3.U(InstTypeLen.W)
  val bInstType   = 4.U(InstTypeLen.W)
  val uInstType   = 5.U(InstTypeLen.W)
  val jInstType   = 6.U(InstTypeLen.W)

  // ALU operation number type
  protected val nopAluOperNumType    = 2.U(EXUOperNumTypeLen.W)
  protected val regAluOperNumType    = 1.U(EXUOperNumTypeLen.W)
  protected val immAluOperNumType    = 2.U(EXUOperNumTypeLen.W)
  protected val shamtAluOperNumType  = 3.U(EXUOperNumTypeLen.W)
  protected val offsetBeuOperNumType = 4.U(EXUOperNumTypeLen.W)

  protected val branchFalse = false.B
  protected val branchTrue  = true.B

  protected val rdMemFalse = false.B
  protected val rdMemTrue  = true.B

  protected val wtMemFalse = false.B
  protected val wtMemTrue  = true.B

  protected val nopWtType = 1.U(wtDataSrcTypeLen.W)
  protected val aluWtType = 1.U(wtDataSrcTypeLen.W)
  protected val memWtType = 2.U(wtDataSrcTypeLen.W)

  protected val defDecodeRes =
    List(wtRegFalse, nopInstType, nopAluOperNumType, aluNopType, branchFalse, rdMemFalse, wtMemFalse, nopWtType)

  protected val decodeTable = Array(
    // i type inst
    ADDI  -> List(wtRegTrue, iInstType, immAluOperNumType, aluADDIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    ADDIW -> List(wtRegTrue, iInstType, immAluOperNumType, aluADDIWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SLTI  -> List(wtRegTrue, iInstType, immAluOperNumType, aluSLTIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SLTIU -> List(wtRegTrue, iInstType, immAluOperNumType, aluSLTIUType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    ANDI  -> List(wtRegTrue, iInstType, immAluOperNumType, aluANDIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    ORI   -> List(wtRegTrue, iInstType, immAluOperNumType, aluORIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    XORI  -> List(wtRegTrue, iInstType, immAluOperNumType, aluXORIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SLLI  -> List(wtRegTrue, iInstType, shamtAluOperNumType, aluSLLIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SLLIW -> List(wtRegTrue, iInstType, shamtAluOperNumType, aluSLLIWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SRLI  -> List(wtRegTrue, iInstType, shamtAluOperNumType, aluSRLIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SRLIW -> List(wtRegTrue, iInstType, shamtAluOperNumType, aluSRLIWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SRAI  -> List(wtRegTrue, iInstType, shamtAluOperNumType, aluSRAIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SRAIW -> List(wtRegTrue, iInstType, shamtAluOperNumType, aluSRAIWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    // u type inst
    LUI   -> List(wtRegTrue, uInstType, immAluOperNumType, aluLUIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    AUIPC -> List(wtRegTrue, uInstType, immAluOperNumType, aluAUIPCType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    // r type inst
    ADD  -> List(wtRegTrue, rInstType, regAluOperNumType, aluADDType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    ADDW -> List(wtRegTrue, rInstType, regAluOperNumType, aluADDWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SLT  -> List(wtRegTrue, rInstType, regAluOperNumType, aluSLTType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SLTU -> List(wtRegTrue, rInstType, regAluOperNumType, aluSLTUType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    AND  -> List(wtRegTrue, rInstType, regAluOperNumType, aluANDType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    OR   -> List(wtRegTrue, rInstType, regAluOperNumType, aluORType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    XOR  -> List(wtRegTrue, rInstType, regAluOperNumType, aluXORType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SLL  -> List(wtRegTrue, rInstType, regAluOperNumType, aluSLLType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SLLW -> List(wtRegTrue, rInstType, regAluOperNumType, aluSLLWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SRL  -> List(wtRegTrue, rInstType, regAluOperNumType, aluSRLType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SRLW -> List(wtRegTrue, rInstType, regAluOperNumType, aluSRLWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SUB  -> List(wtRegTrue, rInstType, regAluOperNumType, aluSUBType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SUBW -> List(wtRegTrue, rInstType, regAluOperNumType, aluSUBWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SRA  -> List(wtRegTrue, rInstType, regAluOperNumType, aluSRAType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    SRAW -> List(wtRegTrue, rInstType, regAluOperNumType, aluSRAWType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    // nop inst
    NOP -> List(wtRegFalse, nopInstType, nopAluOperNumType, aluNopType, branchFalse, rdMemFalse, wtMemFalse, nopWtType),
    // j type inst
    JAL -> List(wtRegTrue, jInstType, offsetBeuOperNumType, beuJALType, branchTrue, rdMemFalse, wtMemFalse, aluWtType),
    JALR -> List(wtRegTrue, iInstType, offsetBeuOperNumType, beuJALRType, branchTrue, rdMemFalse, wtMemFalse, aluWtType),
  )
}

class InstDecoderStage extends Module with InstConfig {
  val io = IO(new Bundle {
    val instAddrIn: UInt = Input(UInt(BusWidth.W))
    val instDataIn: UInt = Input(UInt(InstWidth.W))

    val rdDataAIn: UInt = Input(UInt(BusWidth.W))
    val rdDataBIn: UInt = Input(UInt(BusWidth.W))

    // forward
    val fwRsEnaAIn: Bool = Input(Bool())
    val fwRsValAIn: UInt = Input(UInt(BusWidth.W))
    val fwRsEnaBIn: Bool = Input(Bool())
    val fwRsValBIn: UInt = Input(UInt(BusWidth.W))

    val rdEnaAOut:  Bool = Output(Bool())
    val rdAddrAOut: UInt = Output(UInt(RegAddrLen.W))
    val rdEnaBOut:  Bool = Output(Bool())
    val rdAddrBOut: UInt = Output(UInt(RegAddrLen.W))

    // beu
    val exuOperTypeOut: UInt = Output(UInt(EXUOperTypeLen.W))
    val exuOffsetOut:   UInt = Output(UInt(BusWidth.W))
    val exuOperNumOut: UInt = Output(UInt(BusWidth.W))

    val rsValAOut:      UInt = Output(UInt(BusWidth.W))
    val rsValBOut:      UInt = Output(UInt(BusWidth.W))
    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))
  })

  protected val rsRegAddrA: UInt = io.instDataIn(19, 15)
  protected val rsRegAddrB: UInt = io.instDataIn(24, 20)
  protected val rdRegAddr:  UInt = io.instDataIn(11, 7)
  //@printf(p"[id]rsRegAddrA = 0x${Hexadecimal(rsRegAddrA)}\n")
  //@printf(p"[id]rsRegAddrB = 0x${Hexadecimal(rsRegAddrB)}\n")
  //@printf(p"[id]rdRegAddr = 0x${Hexadecimal(rdRegAddr)}\n")

  protected val decodeRes = ListLookup(io.instDataIn, InstDecoderStage.defDecodeRes, InstDecoderStage.decodeTable)

  //@printf(p"[id]io.instDataIn = 0x${Hexadecimal(io.instDataIn)}\n")
  //@printf(p"[id]decodeRes(0) = 0x${Hexadecimal(decodeRes(0))}\n")
  //@printf(p"[id]decodeRes(1) = 0x${Hexadecimal(decodeRes(1))}\n")
  //@printf(p"[id]decodeRes(2) = 0x${Hexadecimal(decodeRes(2))}\n")
  //@printf(p"[id]decodeRes(3) = 0x${Hexadecimal(decodeRes(3))}\n")
  //@printf(p"[id]decodeRes(4) = 0x${Hexadecimal(decodeRes(4))}\n")
  //@printf(p"[id]decodeRes(5) = 0x${Hexadecimal(decodeRes(5))}\n")
  //@printf(p"[id]decodeRes(6) = 0x${Hexadecimal(decodeRes(6))}\n")
  //@printf(p"[id]decodeRes(7) = 0x${Hexadecimal(decodeRes(7))}\n")

  // acoording the inst type to construct the imm
  protected val immExtensionUnit = Module(new ImmExten)
  immExtensionUnit.io.instDataIn := io.instDataIn
  immExtensionUnit.io.instTypeIn := decodeRes(1)

  when(
    (decodeRes(1) =/= InstDecoderStage.uInstType) &&
      (decodeRes(1) =/= InstDecoderStage.jInstType)
  ) {
    io.rdEnaAOut  := true.B
    io.rdAddrAOut := rsRegAddrA
  }.otherwise {
    io.rdEnaAOut  := false.B
    io.rdAddrAOut := 0.U
  }

  when(
    (decodeRes(1) =/= InstDecoderStage.uInstType) &&
      (decodeRes(1) =/= InstDecoderStage.jInstType) &&
      (decodeRes(1) =/= InstDecoderStage.iInstType)
  ) {
    io.rdEnaBOut  := true.B
    io.rdAddrBOut := rsRegAddrB
  }.otherwise {
    io.rdEnaBOut  := false.B
    io.rdAddrBOut := 0.U
  }

  io.exuOperTypeOut := decodeRes(3)
  // for jal and jalr offset
  io.exuOffsetOut := Mux(decodeRes(3) === beuJALType || decodeRes(3) === beuJALRType, 
                        immExtensionUnit.io.immOut, 0.U)

  when (decodeRes(3) === beuJALType) {
    io.exuOperNumOut := io.instAddrIn
  }.elsewhen(decodeRes(3) === beuJALRType) {
    io.exuOperNumOut := io.rdDataAIn
  }.otherwise {
    io.exuOperNumOut := 0.U
  }

  when(
    decodeRes(3) === aluAUIPCType ||
      decodeRes(3) === beuJALType ||
      decodeRes(3) === beuJALRType
  ) {
    io.rsValAOut := io.instAddrIn
  }.elsewhen(io.fwRsEnaAIn) {
    io.rsValAOut := io.fwRsValAIn
  }.otherwise {
    // if oper don't need rsvalA(such as jal), set this val to 0
    io.rsValAOut := io.rdDataAIn
  }

  // io.rsValAOut      := Mux(io.fwRsEnaAIn, io.fwRsValAIn, io.rdDataAIn)

  when(decodeRes(2) === InstDecoderStage.immAluOperNumType) {
    io.rsValBOut := immExtensionUnit.io.immOut
  }.elsewhen(decodeRes(2) === InstDecoderStage.shamtAluOperNumType) {
    io.rsValBOut := io.instDataIn(25, 20)
  }.elsewhen(decodeRes(3) === beuJALType || decodeRes(3) === beuJALRType) {
    io.rsValBOut := 4.U
  }.elsewhen(io.fwRsEnaBIn) {
    io.rsValBOut := io.fwRsValBIn
  }.otherwise {
    io.rsValBOut := io.rdDataBIn
  }

  io.wtEnaOut  := decodeRes(0)
  io.wtAddrOut := rdRegAddr

  //@printf(p"[id]io.rdEnaAOut = 0x${Hexadecimal(io.rdEnaAOut)}\n")
  //@printf(p"[id]io.rdAddrAOut = 0x${Hexadecimal(io.rdAddrAOut)}\n")
  //@printf(p"[id]io.rdEnaBOut = 0x${Hexadecimal(io.rdEnaBOut)}\n")
  //@printf(p"[id]io.rdAddrBOut = 0x${Hexadecimal(io.rdAddrBOut)}\n")

  //@printf(p"[id]io.exuOperTypeOut = 0x${Hexadecimal(io.exuOperTypeOut)}\n")
  //@printf(p"[id]io.rsValAOut = 0x${Hexadecimal(io.rsValAOut)}\n")
  //@printf(p"[id]io.rsValBOut = 0x${Hexadecimal(io.rsValBOut)}\n")

  //@printf(p"[id]io.wtEnaOut = 0x${Hexadecimal(io.wtEnaOut)}\n")
  //@printf(p"[id]io.wtAddrOut = 0x${Hexadecimal(io.wtAddrOut)}\n")
}
