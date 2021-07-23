
`timescale 1ns/1ps

module oled_tb;
    localparam CLOCK_PERIOD = 31.25; // gen 32Mhz clock
    reg clk, rst_n;
    wire scl, sda;

    initial begin
        $dumpfile("oled_tb.vcd");
        $dumpvars(0, oled_tb);
    end

    initial begin
        clk = 0;
        rst_n = 1;
        #(10);
        rst_n = 0;
        #(10);
        rst_n = 1;
        #(CLOCK_PERIOD * 3000_000);
        $finish;
    end

    always #(CLOCK_PERIOD / 2) clk = ~clk;
    
     
     oled u_oled(
        .clk_32M(clk),
        .rst_n(rst_n),
        .scl(scl),
        .sda(sda)
    );

endmodule