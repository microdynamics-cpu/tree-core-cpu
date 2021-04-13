`include "defines.v"

module wb (
    input wire[`RegBus] reg_wdata_i,       // 写寄存器数据
    input wire reg_we_i,                   // 是否要写通用寄存器
    input wire[`RegAddrBus] reg_waddr_i,   // 写通用寄存器地址

    output wire[`RegBus] reg_wdata_o,       // 写寄存器数据
    output wire reg_we_o,                   // 是否要写通用寄存器
    output wire[`RegAddrBus] reg_waddr_o   // 写通用寄存器地址
);

    assign reg_wdata_o = reg_wdata_i;
    assign reg_we_o = reg_we_i;
    assign reg_waddr_o = reg_waddr_i;

endmodule