package treecorel3

import chisel._
import chisel.uitl._

object Control {
  val Y = true.B
  val F = false.B

  val PCTypeLen = 2
  val PC_0      = 0.U(PCTypeLen.W)
  val PC_4      = 1.U(PCTypeLen.W)
  val PC_ALU    = 2.U(PCTypeLen.W)
  val PC_EPC    = 3.U(PCTypeLen.W)

  val OprandTypeLen = 1
  val A_XXX         = 0.U(OprandTypeLen.W)
  val A_PC          = 1.U(OprandTypeLen.W)
  val A_RS1         = 2.U(OprandTypeLen.W)
  val B_XXX         = 0.U(OprandTypeLen.W)
  val B_PC          = 1.U(OprandTypeLen.W)
  val B_RS1         = 2.U(OprandTypeLen.W)

  val IMMTypeLen = 3
  val IMM_X      = 0.U(IMMTypeLen.W)
  val IMM_I      = 1.U(IMMTypeLen.W)
  val IMM_S      = 2.U(IMMTypeLen.W)
  val IMM_U      = 3.U(IMMTypeLen.W)
  val IMM_J      = 4.U(IMMTypeLen.W)
  val IMM_B      = 5.U(IMMTypeLen.W)
  val IMM_Z      = 6.U(IMMTypeLen.W)

  val BranchTypeLen = 3
  val BR_XXX        = 0.U(BranchTypeLen.W)
  val BR_LTU        = 1.U(BranchTypeLen.W)
  val BR_LT         = 2.U(BranchTypeLen.W)
  val BR_EQ         = 3.U(BranchTypeLen.W)
  val BR_GEU        = 4.U(BranchTypeLen.W)
  val BR_GE         = 5.U(BranchTypeLen.W)
  val BR_NE         = 6.U(BranchTypeLen.W)

  val StoreTypeLen = 2
  val ST_XXX       = 0.U(StoreTypeLen.W)
  val ST_SW        = 1.U(StoreTypeLen.W)
  val ST_SH        = 2.U(StoreTypeLen.W)
  val ST_SB        = 3.U(StoreTypeLen.W)

  val LoadTypeLen = 3
  val LD_XXX      = 0.U(LoadTypeLen.W)
  val LD_LW       = 1.U(LoadTypeLen.W)
  val LD_LH       = 2.U(LoadTypeLen.W)
  val LD_LB       = 3.U(LoadTypeLen.W)
  val LD_LHU      = 4.U(LoadTypeLen.W)
  val LD_LBU      = 5.U(LoadTypeLen.W)

  val WbTypeLen = 2
  val WB_ALU    = 0.U(WbTypeLen.W)
  val WB_MEM    = 1.U(WbTypeLen.W)
  val WB_PC4    = 2.U(WbTypeLen.W)
  val WB_CSR    = 3.U(WbTypeLen.W)

  //                                                           kill                      wb_en    illegal?
  //                pc_sel a_sel b_sel   imm_sel alu_op br_type |  st_type ld_type wb_sel  | csr_cmd |
  //                  |      |     |      |        |        |   |     |       |       |    |  |      |
  val defRes = List(PC_4, A_XXX, B_XXX, IMM_X, ALU_XXX, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, Y)

  val map = Array(
    LUI    -> List(PC_4, A_PC, B_IMM, IMM_U, ALU_COPY_B, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    AUIPC  -> List(PC_4, A_PC, B_IMM, IMM_U, ALU_ADD, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    JAL    -> List(PC_ALU, A_PC, B_IMM, IMM_J, ALU_ADD, BR_XXX, Y, ST_XXX, LD_XXX, WB_PC4, Y, CSR.N, N),
    JALR   -> List(PC_ALU, A_RS1, B_IMM, IMM_I, ALU_ADD, BR_XXX, Y, ST_XXX, LD_XXX, WB_PC4, Y, CSR.N, N),
    BEQ    -> List(PC_4, A_PC, B_IMM, IMM_B, ALU_ADD, BR_EQ, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BNE    -> List(PC_4, A_PC, B_IMM, IMM_B, ALU_ADD, BR_NE, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BLT    -> List(PC_4, A_PC, B_IMM, IMM_B, ALU_ADD, BR_LT, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BGE    -> List(PC_4, A_PC, B_IMM, IMM_B, ALU_ADD, BR_GE, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BLTU   -> List(PC_4, A_PC, B_IMM, IMM_B, ALU_ADD, BR_LTU, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    BGEU   -> List(PC_4, A_PC, B_IMM, IMM_B, ALU_ADD, BR_GEU, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    LB     -> List(PC_0, A_RS1, B_IMM, IMM_I, ALU_ADD, BR_XXX, Y, ST_XXX, LD_LB, WB_MEM, Y, CSR.N, N),
    LH     -> List(PC_0, A_RS1, B_IMM, IMM_I, ALU_ADD, BR_XXX, Y, ST_XXX, LD_LH, WB_MEM, Y, CSR.N, N),
    LW     -> List(PC_0, A_RS1, B_IMM, IMM_I, ALU_ADD, BR_XXX, Y, ST_XXX, LD_LW, WB_MEM, Y, CSR.N, N),
    LBU    -> List(PC_0, A_RS1, B_IMM, IMM_I, ALU_ADD, BR_XXX, Y, ST_XXX, LD_LBU, WB_MEM, Y, CSR.N, N),
    LHU    -> List(PC_0, A_RS1, B_IMM, IMM_I, ALU_ADD, BR_XXX, Y, ST_XXX, LD_LHU, WB_MEM, Y, CSR.N, N),
    SB     -> List(PC_4, A_RS1, B_IMM, IMM_S, ALU_ADD, BR_XXX, N, ST_SB, LD_XXX, WB_ALU, N, CSR.N, N),
    SH     -> List(PC_4, A_RS1, B_IMM, IMM_S, ALU_ADD, BR_XXX, N, ST_SH, LD_XXX, WB_ALU, N, CSR.N, N),
    SW     -> List(PC_4, A_RS1, B_IMM, IMM_S, ALU_ADD, BR_XXX, N, ST_SW, LD_XXX, WB_ALU, N, CSR.N, N),
    ADDI   -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_ADD, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLTI   -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_SLT, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLTIU  -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_SLTU, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    XORI   -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_XOR, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    ORI    -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_OR, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    ANDI   -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_AND, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLLI   -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_SLL, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SRLI   -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_SRL, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SRAI   -> List(PC_4, A_RS1, B_IMM, IMM_I, ALU_SRA, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    ADD    -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_ADD, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SUB    -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_SUB, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLL    -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_SLL, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLT    -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_SLT, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SLTU   -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_SLTU, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    XOR    -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_XOR, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SRL    -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_SRL, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    SRA    -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_SRA, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    OR     -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_OR, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    AND    -> List(PC_4, A_RS1, B_RS2, IMM_X, ALU_AND, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, Y, CSR.N, N),
    FENCE  -> List(PC_4, A_XXX, B_XXX, IMM_X, ALU_XXX, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    FENCEI -> List(PC_0, A_XXX, B_XXX, IMM_X, ALU_XXX, BR_XXX, Y, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N),
    CSRRW  -> List(PC_0, A_RS1, B_XXX, IMM_X, ALU_COPY_A, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, Y, CSR.W, N),
    CSRRS  -> List(PC_0, A_RS1, B_XXX, IMM_X, ALU_COPY_A, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, Y, CSR.S, N),
    CSRRC  -> List(PC_0, A_RS1, B_XXX, IMM_X, ALU_COPY_A, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, Y, CSR.C, N),
    CSRRWI -> List(PC_0, A_XXX, B_XXX, IMM_Z, ALU_XXX, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, Y, CSR.W, N),
    CSRRSI -> List(PC_0, A_XXX, B_XXX, IMM_Z, ALU_XXX, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, Y, CSR.S, N),
    CSRRCI -> List(PC_0, A_XXX, B_XXX, IMM_Z, ALU_XXX, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, Y, CSR.C, N),
    ECALL  -> List(PC_4, A_XXX, B_XXX, IMM_X, ALU_XXX, BR_XXX, N, ST_XXX, LD_XXX, WB_CSR, N, CSR.P, N),
    EBREAK -> List(PC_4, A_XXX, B_XXX, IMM_X, ALU_XXX, BR_XXX, N, ST_XXX, LD_XXX, WB_CSR, N, CSR.P, N),
    ERET   -> List(PC_EPC, A_XXX, B_XXX, IMM_X, ALU_XXX, BR_XXX, Y, ST_XXX, LD_XXX, WB_CSR, N, CSR.P, N),
    WFI    -> List(PC_4, A_XXX, B_XXX, IMM_X, ALU_XXX, BR_XXX, N, ST_XXX, LD_XXX, WB_ALU, N, CSR.N, N)
  )
}

class ControlIO(implicit p: Parameters) extends Bundle {
  val inst      = Input(UInt(xlen.W))
  val pc_sel    = Output(UInt(2.W))
  val inst_kill = Output(Bool())
  val a_sel     = Output(UInt(1.W))
  val b_sel     = Output(UInt(1.W))
  val imm_sel   = Output(UInt(3.W))
  val alu_op    = Output(UInt(4.W))
  val br_type   = Output(UInt(3.W))
  val st_type   = Output(UInt(2.W))
  val ld_type   = Output(UInt(3.W))
  val wb_sel    = Output(UInt(2.W))
  val wb_en     = Output(Bool())
  val csr_cmd   = Output(UInt(3.W))
  val illegal   = Output(Bool())
}

class Control(implicit p: Parameters) extends Module {
  val io     = IO(new ControlIO)
  val decRes = ListLookup(io.inst, Control.defRes, Control.map)

  // Control signals for Fetch
  io.pc_sel    := decRes(0)
  io.inst_kill := decRes(6)

  // Control signals for Execute
  io.a_sel   := decRes(1)
  io.b_sel   := decRes(2)
  io.imm_sel := decRes(3)
  io.alu_op  := decRes(4)
  io.br_type := decRes(5)
  io.st_type := decRes(7)

  // Control signals for Write Back
  io.ld_type := decRes(8)
  io.wb_sel  := decRes(9)
  io.wb_en   := decRes(10)
  io.csr_cmd := decRes(11)
  io.illegal := decRes(12)
}
