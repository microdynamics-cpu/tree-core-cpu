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
  protected val offsetLsuOperNumType = 5.U(EXUOperNumTypeLen.W)

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
    JAL  -> List(wtRegTrue, jInstType, offsetBeuOperNumType, beuJALType, branchTrue, rdMemFalse, wtMemFalse, aluWtType),
    JALR -> List(wtRegTrue, iInstType, offsetBeuOperNumType, beuJALRType, branchTrue, rdMemFalse, wtMemFalse, aluWtType),
    // b type inst
    BEQ  -> List(wtRegFalse, bInstType, offsetBeuOperNumType, beuBEQType, branchTrue, rdMemFalse, wtMemFalse, nopWtType),
    BNE  -> List(wtRegFalse, bInstType, offsetBeuOperNumType, beuBNEType, branchTrue, rdMemFalse, wtMemFalse, nopWtType),
    BLT  -> List(wtRegFalse, bInstType, offsetBeuOperNumType, beuBLTType, branchTrue, rdMemFalse, wtMemFalse, nopWtType),
    BLTU -> List(wtRegFalse, bInstType, offsetBeuOperNumType, beuBLTUType, branchTrue, rdMemFalse, wtMemFalse, nopWtType),
    BGE  -> List(wtRegFalse, bInstType, offsetBeuOperNumType, beuBGEType, branchTrue, rdMemFalse, wtMemFalse, nopWtType),
    BGEU -> List(wtRegFalse, bInstType, offsetBeuOperNumType, beuBGEUType, branchTrue, rdMemFalse, wtMemFalse, nopWtType),
    // special i type inst
    LB  -> List(wtRegTrue, iInstType, offsetLsuOperNumType, lsuLBType, branchFalse, rdMemTrue, wtMemFalse, nopWtType),
    LBU -> List(wtRegTrue, iInstType, offsetLsuOperNumType, lsuLBUType, branchFalse, rdMemTrue, wtMemFalse, nopWtType),
    LH  -> List(wtRegTrue, iInstType, offsetLsuOperNumType, lsuLHType, branchFalse, rdMemTrue, wtMemFalse, nopWtType),
    LHU -> List(wtRegTrue, iInstType, offsetLsuOperNumType, lsuLHUType, branchFalse, rdMemTrue, wtMemFalse, nopWtType),
    LW  -> List(wtRegTrue, iInstType, offsetLsuOperNumType, lsuLWType, branchFalse, rdMemTrue, wtMemFalse, nopWtType),
    LWU -> List(wtRegTrue, iInstType, offsetLsuOperNumType, lsuLWUType, branchFalse, rdMemTrue, wtMemFalse, nopWtType),
    LD  -> List(wtRegTrue, iInstType, offsetLsuOperNumType, lsuLDType, branchFalse, rdMemTrue, wtMemFalse, nopWtType),
    // s type inst
    SB -> List(wtRegFalse, sInstType, offsetLsuOperNumType, lsuSBType, branchFalse, rdMemFalse, wtMemTrue, memWtType),
    SH -> List(wtRegFalse, sInstType, offsetLsuOperNumType, lsuSHType, branchFalse, rdMemFalse, wtMemTrue, memWtType),
    SW -> List(wtRegFalse, sInstType, offsetLsuOperNumType, lsuSWType, branchFalse, rdMemFalse, wtMemTrue, memWtType),
    SD -> List(wtRegFalse, sInstType, offsetLsuOperNumType, lsuSDType, branchFalse, rdMemFalse, wtMemTrue, memWtType),
    // csr inst
    CSRRS -> List(wtRegTrue, iInstType, nopAluOperNumType, csrRSType, branchFalse, rdMemFalse, wtMemFalse, aluWtType),
    // custom inst
    CUST -> List(wtRegFalse, nopInstType, nopAluOperNumType, custInstType, branchFalse, rdMemFalse, wtMemFalse, nopWtType)
  )
}

class InstDecoderStage extends Module with InstConfig {
  val io = IO(new Bundle {
    // from pc
    val instAddrIn: UInt = Input(UInt(BusWidth.W))
    val instDataIn: UInt = Input(UInt(InstWidth.W))
    // to id2ex
    val rdDataAIn: UInt = Input(UInt(BusWidth.W))
    val rdDataBIn: UInt = Input(UInt(BusWidth.W))

    // from ex
    val exuOperTypeIn: UInt = Input(UInt(InstOperTypeLen.W))
    val exuWtAddrIn:   UInt = Input(UInt(RegAddrLen.W))
    // from forward
    val fwRsEnaAIn: Bool = Input(Bool())
    val fwRsValAIn: UInt = Input(UInt(BusWidth.W))
    val fwRsEnaBIn: Bool = Input(Bool())
    val fwRsValBIn: UInt = Input(UInt(BusWidth.W))

    // to regfile
    val rdEnaAOut:  Bool = Output(Bool())
    val rdAddrAOut: UInt = Output(UInt(RegAddrLen.W))
    val rdEnaBOut:  Bool = Output(Bool())
    val rdAddrBOut: UInt = Output(UInt(RegAddrLen.W))

    // to beu
    val exuOperTypeOut: UInt = Output(UInt(InstOperTypeLen.W))
    val exuOffsetOut:   UInt = Output(UInt(BusWidth.W))
    val exuOperNumOut:  UInt = Output(UInt(BusWidth.W))

    // to ma
    val lsuFunc3Out: UInt = Output(UInt(3.W))
    val lsuWtEnaOut: Bool = Output(Bool())

    // to regfile
    val rsValAOut: UInt = Output(UInt(BusWidth.W))
    val rsValBOut: UInt = Output(UInt(BusWidth.W))
    val wtEnaOut:  Bool = Output(Bool())
    val wtAddrOut: UInt = Output(UInt(RegAddrLen.W))

    // to control
    val stallReqFromIDOut: Bool = Output(Bool())

    // to csr
    val csrAddrOut: UInt = Output(UInt(CSRAddrLen.W))
  })

  protected val rsRegAddrA: UInt = io.instDataIn(19, 15)
  protected val rsRegAddrB: UInt = io.instDataIn(24, 20)
  protected val rdRegAddr:  UInt = io.instDataIn(11, 7)

  protected val decodeRes = ListLookup(io.instDataIn, InstDecoderStage.defDecodeRes, InstDecoderStage.decodeTable)

  // acoording the inst type to construct the imm
  protected val immExtensionUnit = Module(new ImmExten)
  immExtensionUnit.io.instDataIn := io.instDataIn
  immExtensionUnit.io.instTypeIn := decodeRes(1)

  io.lsuFunc3Out := io.instDataIn(14, 12)
  io.lsuWtEnaOut := decodeRes(6)

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
  // 4 for jump inst, 5 for load inst, 6 for store inst
  // because load/store inst both need to calc the mem addr, so have below code
  io.exuOffsetOut := Mux(decodeRes(4).asBool || decodeRes(5).asBool || decodeRes(6).asBool, immExtensionUnit.io.immOut, 0.U)

  when(
    decodeRes(3) === beuJALType ||
      decodeRes(3) === beuBEQType ||
      decodeRes(3) === beuBNEType ||
      decodeRes(3) === beuBLTType ||
      decodeRes(3) === beuBLTUType ||
      decodeRes(3) === beuBGEType ||
      decodeRes(3) === beuBGEUType
  ) {
    io.exuOperNumOut := io.instAddrIn
  }.elsewhen(decodeRes(3) === beuJALRType) {
    // import: maybe this time rdDataA have not been writen to the reg
    when(io.fwRsEnaAIn) {
      io.exuOperNumOut := io.fwRsValAIn
    }.otherwise {
      io.exuOperNumOut := io.rdDataAIn
    }
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

  io.wtEnaOut := decodeRes(0)
  // very important!!! otherwise maybe exist bypass error in wb stage!
  when(io.wtEnaOut) {
    io.wtAddrOut := rdRegAddr
  }.otherwise {
    io.wtAddrOut := 0.U
  }

  // if exist load correlation
  when(
    io.exuOperTypeIn >= lsuLBType && io.exuOperTypeIn <= lsuLDType &&
      ((io.rdEnaAOut && io.exuWtAddrIn === io.rdAddrAOut) ||
        (io.rdEnaBOut && io.exuWtAddrIn === io.rdAddrBOut))
  ) {
    io.stallReqFromIDOut := true.B
  }.otherwise {
    io.stallReqFromIDOut := false.B
  }

  when(decodeRes(3) === csrRSType) {
    io.csrAddrOut := io.instDataIn(31, 20)
  }.otherwise {
    io.csrAddrOut := 0.U
  }
}
