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
    ADDI -> List(wtRegTrue, iInstType, immAluOperNumType, aluADDIType, branchFalse, rdMemFalse, wtMemFalse, aluWtType)
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
    val instDataIn: UInt = Input(UInt(InstWidth.W))

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
      (decodeRes(1) =/= InstDecoderStage.jInstType) &&
      (decodeRes(1) =/= InstDecoderStage.iInstType),
    true.B,
    false.B
  )
  io.rdAddrBOut := Mux(
    (decodeRes(1) =/= InstDecoderStage.uInstType) &&
      (decodeRes(1) =/= InstDecoderStage.jInstType) &&
      (decodeRes(1) =/= InstDecoderStage.iInstType),
    rsRegAddrB,
    0.U
  )

  io.aluOperTypeOut := decodeRes(3)

  // TODO: just for testing the addi
  io.rsValAOut := io.rdDataAIn
  io.rsValBOut := Mux(decodeRes(2) === InstDecoderStage.immAluOperNumType, immExtensionUnit.io.immOut, io.rdDataBIn)

  io.wtEnaOut  := decodeRes(0)
  io.wtAddrOut := rdRegAddr

  //@printf(p"[id]io.rdEnaAOut = 0x${Hexadecimal(io.rdEnaAOut)}\n")
  //@printf(p"[id]io.rdAddrAOut = 0x${Hexadecimal(io.rdAddrAOut)}\n")
  //@printf(p"[id]io.rdEnaBOut = 0x${Hexadecimal(io.rdEnaBOut)}\n")
  //@printf(p"[id]io.rdAddrBOut = 0x${Hexadecimal(io.rdAddrBOut)}\n")

  //@printf(p"[id]io.aluOperTypeOut = 0x${Hexadecimal(io.aluOperTypeOut)}\n")
  //@printf(p"[id]io.rsValAOut = 0x${Hexadecimal(io.rsValAOut)}\n")
  //@printf(p"[id]io.rsValBOut = 0x${Hexadecimal(io.rsValBOut)}\n")

  //@printf(p"[id]io.wtEnaOut = 0x${Hexadecimal(io.wtEnaOut)}\n")
  //@printf(p"[id]io.wtAddrOut = 0x${Hexadecimal(io.wtAddrOut)}\n")
}
