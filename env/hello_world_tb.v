`timescale 1ns / 1ps

module hello_world_tb;
    // 设置时钟周期为10ns
    // Set the clock cycle to 10ns
    parameter PERIOD = 10;

    reg clk;
    reg rst_n;

    always #(PERIOD / 2) clk = ~clk;

    initial
    begin
        $dumpfile("hello_world_tb.vcd");
        $dumpvars(0, hello_world_tb);
        $display("Hello World!");
        clk = 0;
        rst_n = 0;
        // 延迟10个时钟周期
        // Delay 10 clock cycles
        repeat(10) @(posedge clk);
        rst_n = 1;
        // 延迟10个时钟周期
        // Delay 10 clock cycles
        repeat(10) @(posedge clk);
        $finish;
    end

endmodule
