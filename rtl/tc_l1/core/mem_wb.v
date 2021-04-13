`include "defines.v"


module mem_wb (

    input wire clk,
    input wire rst,
    
    input wire[`Hold_Flag_Bus] hold_flag_i, // 流水线暂停标志


    input wire[`RegBus] reg_wdata_i,       // 写寄存器数据
    input wire reg_we_i,                   // 是否要写通用寄存器
    input wire[`RegAddrBus] reg_waddr_i,   // 写通用寄存器地址

    // to regs
    output wire[`RegBus] reg_wdata_o,       // 写寄存器数据
    output wire reg_we_o,                   // 是否要写通用寄存器
    output wire[`RegAddrBus] reg_waddr_o   // 写通用寄存器地址
);

    wire hold_en;
    assign hold_en = (hold_flag_i >= `Hold_Mem);

    // to regs
    wire[`RegBus] reg_wdata;
    gen_pipe_dff #(32) reg_wdata_ff(clk, rst, hold_en, `ZeroWord, reg_wdata_i, reg_wdata);
    assign reg_wdata_o = reg_wdata;

    wire reg_we;
    gen_pipe_dff #(1) reg_we_ff(clk, rst, hold_en, `WriteDisable, reg_we_i, reg_we);
    assign reg_we_o = reg_we;

    wire[`RegAddrBus] reg_waddr;
    gen_pipe_dff #(5) reg_waddr_ff(clk, rst, hold_en, `ZeroReg, reg_waddr_i, reg_waddr);
    assign reg_waddr_o = reg_waddr;

endmodule