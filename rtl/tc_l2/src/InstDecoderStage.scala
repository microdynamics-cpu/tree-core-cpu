package treecorel2

import chisel3._
import chisel3.util._
import ExecutionStage._
import InstRegexPattern._

object InstDecoderStage {
  protected val wtRegFalse = false.B
  protected val wtRegTrue  = true.B

  // inst type
  val nopInstType = 0.U(3.W)
  val rInstType   = 1.U(3.W)
  val iInstType   = 2.U(3.W)
  val sInstType   = 3.U(3.W)
  val bInstType   = 4.U(3.W)
  val uInstType   = 5.U(3.W)
  val jInstType   = 6.U(3.W)

  // ALU operation number type
  protected val nopAluOperNumType = 0.U(2.W)
  protected val regAluOperNumType = 1.U(2.W)
  protected val immAluOperNumType = 2.U(2.W)

  protected val branchFalse = false.B
  protected val branchTrue  = true.B

  protected val rdMemFalse = false.B
  protected val rdMemTrue  = true.B

  protected val wtMemFalse = false.B
  protected val wtMemTrue  = true.B

  protected val nopWtType = 0.U(2.W)
  protected val aluWtType = 1.U(2.W)
  protected val memWtType = 2.U(2.W)

  protected val defDecodeRes =
    List(wtRegFalse, nopInstType, nopAluOperNumType, aluNopType, branchFalse, rdMemFalse, wtMemFalse, nopWtType)

  protected val decodeTable = Array(
    ADDI -> List(wtRegTrue, iInstType, regAluOperNumType, aluADDIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType)
    // ADD -> List(wtRegTrue, rInstType, regAluOperNumType, ALU_ADD, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    // SUB -> List(wtRegTrue, rInstType, regAluOperNumType, ALU_SUB, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    // AND -> List(wtRegTrue, rInstType, regAluOperNumType, ALU_AND, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    // OR -> List(wtRegTrue, rInstType, regAluOperNumType, ALU_OR, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    // LW -> List(wtRegTrue, iInstType, immAluOperNumType, ALU_ADD, branchFalse, rdMemTrue, wtMemFalse, memWtType),
    // SW -> List(wtRegFalse, sInstType, immAluOperNumType, ALU_ADD, branchFalse, rdMemFalse, wtMemTrue, nopWtType),
    // BEQ -> List(wtRegFalse, bInstType, regAluOperNumType, ALU_SUB, branchTrue, rdMemFalse, wtMemFalse, nopWtType),
    // NOP -> List(
    //   wtRegFalse,
    //   nopInstType,
    //   nopAluOperNumType,
    //   aluNopType,
    //   branchFalse,
    //   rdMemFalse,
    //   wtMemFalse,
    //   nopWtType
    // )
  )
}

class InstDecoderStage extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instAddrIn: UInt = Input(UInt(BusWidth.W))
    val instDataIn: UInt = Input(UInt(BusWidth.W))

    val rdDataAIn: UInt = Input(UInt(BusWidth.W))
    val rdDataBIn: UInt = Input(UInt(BusWidth.W))

    val rdEnaAOut:  Bool = Output(Bool())
    val rdAddrAOut: UInt = Output(UInt(RegAddrLen.W))
    val rdEnaBOut:  Bool = Output(Bool())
    val rdAddrBOut: UInt = Output(UInt(RegAddrLen.W))

    val aluOperTypeOut: UInt = Output(UInt(ALUOperTypeLen.W))
    val rsValAOut:      UInt = Output(UInt(BusWidth.W))
    val rsValBOut:      UInt = Output(UInt(BusWidth.W))

    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))
  })

  protected val rsRegAddrA: UInt = io.instAddrIn(19, 15)
  protected val rsRegAddrB: UInt = io.instAddrIn(24, 20)
  protected val rdRegAddr:  UInt = io.instAddrIn(11, 7)

  protected val decodeRes = ListLookup(io.instDataIn, InstDecoderStage.defDecodeRes, InstDecoderStage.decodeTable)

  // acoording the inst type to construct the imm
  protected val immExtensionUnit = Module(new ImmExtension)
  immExtensionUnit.io.instDataIn := io.instDataIn
  immExtensionUnit.io.instTypeIn := decodeRes(1)

  io.rdEnaAOut := Mux(
    (decodeRes(1) =/= InstDecoderStage.uInstType) &&
      (decodeRes(1) =/= InstDecoderStage.jInstType),
    true.B,
    false.B
  )
  io.rdAddrAOut := Mux(
    (decodeRes(1) =/= InstDecoderStage.uInstType) &&
      (decodeRes(1) =/= InstDecoderStage.jInstType),
    rsRegAddrA,
    0.U
  )

  io.rdEnaBOut := Mux(
    (decodeRes(1) =/= InstDecoderStage.uInstType) &&
      (decodeRes(1) =/= InstDecoderStage.jInstType),
    true.B,
    false.B
  )
  io.rdAddrBOut := Mux(
    (decodeRes(1) =/= InstDecoderStage.uInstType) &&
      (decodeRes(1) =/= InstDecoderStage.jInstType),
    rsRegAddrB,
    0.U
  )

  io.aluOperTypeOut := decodeRes(3)

  // TODO: just for testing the addi
  io.rsValAOut := io.rdDataAIn
  io.rsValBOut := Mux(decodeRes(2) === InstDecoderStage.immAluOperNumType, immExtensionUnit.io.immOut, io.rdDataBIn)

  io.wtEnaOut  := decodeRes(0)
  io.wtAddrOut := rdRegAddr
}
