`include "defines.v"

module mem (
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
    
    // to mem
    assign mem_wdata_o = mem_wdata_i;
    assign mem_raddr_o = mem_raddr_i;
    assign mem_waddr_o = mem_waddr_i;
    assign mem_we_o = mem_we_i;
    assign mem_req_o = mem_req_i;

    // to regs
    assign reg_wdata_o = reg_wdata_i;
    assign reg_we_o = reg_we_i;
    assign reg_waddr_o = reg_waddr_i;

endmodule