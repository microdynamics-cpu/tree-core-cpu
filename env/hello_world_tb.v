`timescale 1ns / 1ps

module hello_world_tb;
    // 设置时钟周期为10ns
    // Set the clock cycle to 10ns
    parameter CYCLE = 10;

    reg clk;
    reg rst_n;

    // 每半个时钟周期变更一次时钟信号的高低状态
    always #(CYCLE / 2) clk = ~clk;

    initial
    begin
        // 生成vcd文件以供波形仿真使用
        // Generate vcd file for waveform simulation
        $dumpfile("hello_world_tb.vcd");
        // 将模块的信号都添加到vcd文件中
        // Add the module's signals to the vcd file
        $dumpvars(0, hello_world_tb);
        // 在仿真过程中打印相应的字符串
        // Print the string in the simulation process
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
