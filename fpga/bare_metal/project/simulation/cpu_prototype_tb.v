`timescale 1ns/1ps

module cpu_prototype_tb;
    localparam CLOCK_PERIOD = 83;
    reg clk, rst_n;
    wire val;
    reg[31:0] count;


    initial begin
        $dumpfile("cpu_prototype_tb.vcd");
        $dumpvars(0, cpu_prototype_tb);
    end

    initial begin
        clk = 0;
        rst_n = 1;
        #(10);
        rst_n = 0;
        #(10);
        rst_n = 1;
        #(CLOCK_PERIOD * 40);
        $finish;
    end

    always #(CLOCK_PERIOD / 2) clk = ~clk;
    
endmodule