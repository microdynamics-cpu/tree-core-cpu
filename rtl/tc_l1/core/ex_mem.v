`include "defines.v"

// 将执行结果向访存模块传递
module ex_mem (

    input wire clk,
    input wire rst,

    input wire[`Hold_Flag_Bus] hold_flag_i, // 流水线暂停标志


    input wire[`MemBus] mem_wdata_i,       // 写内存数据
    input wire[`MemAddrBus] mem_raddr_i,   // 读内存地址
    input wire[`MemAddrBus] mem_waddr_i,   // 写内存地址
    input wire mem_we_i,                   // 是否要写内存
    input wire mem_req_i,                  // 请求访问内存标志

    input wire[`RegBus] reg_wdata_i,       // 写寄存器数据
    input wire reg_we_i,                   // 是否要写通用寄存器
    input wire[`RegAddrBus] reg_waddr_i,   // 写通用寄存器地址


    // to mem
    output wire[`MemBus] mem_wdata_o,       // 写内存数据
    output wire[`MemAddrBus] mem_raddr_o,   // 读内存地址
    output wire[`MemAddrBus] mem_waddr_o,   // 写内存地址
    output wire mem_we_o,                   // 是否要写内存
    output wire mem_req_o,                  // 请求访问内存标志

    // to regs
    output wire[`RegBus] reg_wdata_o,       // 写寄存器数据
    output wire reg_we_o,                   // 是否要写通用寄存器
    output wire[`RegAddrBus] reg_waddr_o   // 写通用寄存器地址
);

    wire hold_en;
    assign hold_en = (hold_flag_i >= `Hold_Ex);

    // to mem
    wire[`MemBus] mem_wdata;
    gen_pipe_dff #(32) mem_wdata_ff(clk, rst, hold_en, `ZeroWord, mem_wdata_i, mem_wdata);
    assign mem_wdata_o = mem_wdata;

    wire[`MemAddrBus] mem_raddr;
    gen_pipe_dff #(32) mem_raddr_ff(clk, rst, hold_en, `ZeroWord, mem_raddr_i, mem_raddr);
    assign mem_raddr_o = mem_raddr;

    wire[`MemAddrBus] mem_waddr;
    gen_pipe_dff #(32) mem_waddr_ff(clk, rst, hold_en, `ZeroWord, mem_waddr_i, mem_waddr);
    assign mem_waddr_o = mem_waddr;

    wire mem_we;
    gen_pipe_dff #(1) mem_we_ff(clk, rst, hold_en, `WriteDisable, mem_we_i, mem_we);
    assign mem_we_o = mem_we;

    wire mem_req;
    gen_pipe_dff #(1) mem_req_ff(clk, rst, hold_en, `RIB_NREQ, mem_req_i, mem_req);
    assign mem_req_o = mem_req;


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