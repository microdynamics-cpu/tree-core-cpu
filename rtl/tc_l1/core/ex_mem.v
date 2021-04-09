`include "defines.v"

// 将执行结果向访存模块传递
module ex_mem (

    input wire clk,
    input wire rst,

    input wire[`Hold_Flag_Bus] hold_flag_i, // 流水线暂停标志
);

endmodule