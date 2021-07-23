
`define CPU_RESET_ADDR        32'h0
`define DATA_INIT_VALUE       32'h0
`define REG_INIT_VALUE        5'h0

`define RST_ENABLE            1'b0
`define RST_DISABLE           1'b1
`define WRITE_ENABLE          1'b1
`define WRITE_DISABLE         1'b0
`define READ_ENABLE           1'b1
`define READ_DISABLE          1'b0
`define CHIP_ENABLE           1'b1
`define CHIP_DISABLE          1'b0
`define JUMP_ENABLE           1'b1
`define JUMP_DISABLE          1'b0
`define HOLD_ENABLE           1'b1
`define HOLD_DISABLE          1'b0

`define TRUE                  1'b1
`define FALSE                 1'b0
`define STOP                  1'b1
`define NO_STOP               1'b0

// DIV
`define DIV_RESULT_NOT_READY  1'b0
`define DIV_RESULT_READY      1'b1
`define DIV_START             1'b1
`define DIV_STOP              1'b0

// BUS
`define RIB_ACK               1'b1
`define RIB_NACK              1'b0
`define RIB_REQ               1'b1
`define RIB_NREQ              1'b0

// INTERRUPT
`define INT_ASSERT            1'b1
`define INT_DEASSERT          1'b0
`define INT_BUS               7:0
`define INT_NONE              8'h0
`define INT_RET               8'hFF
`define INT_TIMER0            8'b00000001
`define INT_TIMER0_ENTRY_ADDR 32'h4

// CONTROL
`define HOLD_FLAG_BUS         2:0
`define HOLD_NONE             3'b000
`define HOLD_PC               3'b001
`define Hold_IF               3'b010
`define Hold_ID               3'b011


`define ROM_NUM               4096  // ROM DEPTH
`define MEM_NUM               4096  // MEMORY DEPTH
`define MEM_DATA_BUS          31:0
`define MEM_ADDR_BUS          31:0
`define INST_DATA_BUS         31:0
`define INST_ADDR_BUS         31:0

// COMMON REGS
`define REG_ADDR_BUS          4:0
`define REG_DATA_BUS          31:0
`define REG_DOUBLE_DATA_BUS   63:0
`define REG_WIDTH             32
`define REG_NUM               32
`define REG_NUM_LOG2          5


// I TYPE INST
`define INST_TYPE_I           7'b0010011
`define INST_ADDI             3'b000
`define INST_SLTI             3'b010
`define INST_SLTIU            3'b011
`define INST_XORI             3'b100
`define INST_ORI              3'b110
`define INST_ANDI             3'b111
`define INST_SLLI             3'b001
`define INST_SRI              3'b101

// L TYPE INST
`define INST_TYPE_L           7'b0000011
`define INST_LB               3'b000
`define INST_LH               3'b001
`define INST_LW               3'b010
`define INST_LBU              3'b100
`define INST_LHU              3'b101

// S TYPE INST
`define INST_TYPE_S           7'b0100011
`define INST_SB               3'b000
`define INST_SH               3'b001
`define INST_SW               3'b010

// R AND M TYPE INST
`define INST_TYPE_R_M         7'b0110011

// R TYPE INST
`define INST_ADD_SUB          3'b000
`define INST_SLL              3'b001
`define INST_SLT              3'b010
`define INST_SLTU             3'b011
`define INST_XOR              3'b100
`define INST_SR               3'b101
`define INST_OR               3'b110
`define INST_AND              3'b111

// M TYPE INST
`define INST_MUL              3'b000
`define INST_MULH             3'b001
`define INST_MULHSU           3'b010
`define INST_MULHU            3'b011
`define INST_DIV              3'b100
`define INST_DIVU             3'b101
`define INST_REM              3'b110
`define INST_REMU             3'b111

// J TYPE INST
`define INST_JAL              7'b1101111
`define INST_JALR             7'b1100111

`define INST_LUI              7'b0110111
`define INST_AUIPC            7'b0010111
`define INST_NOP              32'h00000001
`define INST_NOP_OP           7'b0000001
`define INST_MRET             32'h30200073
`define INST_RET              32'h00008067

`define INST_FENCE            7'b0001111
`define INST_ECALL            32'h73
`define INST_EBREAK           32'h00100073

// J TYPE INST
`define INST_TYPE_B           7'b1100011
`define INST_BEQ              3'b000
`define INST_BNE              3'b001
`define INST_BLT              3'b100
`define INST_BGE              3'b101
`define INST_BLTU             3'b110
`define INST_BGEU             3'b111

// CSR INST
`define INST_CSR              7'b1110011
`define INST_CSRRW            3'b001
`define INST_CSRRS            3'b010
`define INST_CSRRC            3'b011
`define INST_CSRRWI           3'b101
`define INST_CSRRSI           3'b110
`define INST_CSRRCI           3'b111

// CSR REG ADDR
`define CSR_CYCLE             12'hc00
`define CSR_CYCLEH            12'hc80
`define CSR_MTVEC             12'h305
`define CSR_MCAUSE            12'h342
`define CSR_MEPC              12'h341
`define CSR_MIE               12'h304
`define CSR_MSTATUS           12'h300
`define CSR_MSCRATCH          12'h340
