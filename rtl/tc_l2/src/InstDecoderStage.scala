package treecorel2

import chisel3._
import chisel3.util._
import ExecutionStage._
import InstRegexPattern._

object InstDecoderStage {
  private val wtRegFalse = false.B
  private val wtRegTrue  = true.B

  // inst type
  val nopInstType = 0.U(3.W)
  val rInstType   = 1.U(3.W)
  val iInstType   = 2.U(3.W)
  val sInstType   = 3.U(3.W)
  val bInstType   = 4.U(3.W)

  // ALU operation number type
  private val nopAluOperNumType = 0.U(2.W)
  private val regAluOperNumType = 1.U(2.W)
  private val immAluOperNumType = 2.U(2.W)

  private val branchFalse = false.B
  private val branchTrue  = true.B

  private val rdMemFalse = false.B
  private val rdMemTrue  = true.B

  private val wtMemFalse = false.B
  private val wtMemTrue  = true.B

  private val nopWtType = 0.U(2.W)
  private val aluWtType = 1.U(2.W)
  private val memWtType = 2.U(2.W)

  private val defDecodeRes =
    List(wtRegFalse, nopInstType, nopAluOperNumType, aluNopType, branchFalse, rdMemFalse, wtMemFalse, nopWtType)

  private val decodeTable = Array(
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

    val rdEnaAIn:  UInt = Output(Bool())
    val rdAddrAIn: UInt = Output(UInt(RegAddrLen.W))
    val rdEnaBIn:  UInt = Output(Bool())
    val rdAddrBIn: UInt = Output(UInt(RegAddrLen.W))

    val aluOpcodeOut: UInt = Output(UInt(ALUOpcodeLen.W))
    val aluSelOut:    UInt = Output(UInt(ALUSelLen.W))
    val rsValAOut:    UInt = Output(UInt(BusWidth.W))
    val rsValBOut:    UInt = Output(UInt(BusWidth.W))

    val wtEnaOut:  UInt = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))
  })

  private val decodeRes = ListLookup(io.instDataIn, InstDecoderStage.defDecodeRes, InstDecoderStage.decodeTable)

  // acoording the inst type to construct the imm
  private val immExtensionUnit = Module(new ImmExtension)
  immExtensionUnit.io.instDataIn := io.instDataIn
  immExtensionUnit.io.instTypeIn := decodeRes(1)
}
