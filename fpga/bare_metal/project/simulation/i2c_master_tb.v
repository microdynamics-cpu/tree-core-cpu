`timescale 1ns/1ps

module i2c_master_tb;
    localparam CLOCK_PERIOD = 31.25;
    reg clk, rst_n;
    reg[7:0] reg_addr, data_in;
    wire scl, sda, w_done;
    wire[8:0] debug_count_delay;
    wire[3:0] debug_i2c_state;
    wire[7:0] debug_write_data;


    initial begin
        $dumpfile("i2c_master_tb.vcd");
        $dumpvars(0, i2c_master_tb);
    end

    initial begin
        // init
        clk = 0;
        reg_addr <= 8'h66;
        data_in  <= 8'hA3;

        rst_n = 1;
        #(10);
        rst_n = 0;
        #(10);
        rst_n = 1;
        #(CLOCK_PERIOD * 1000_00);
        $finish;
    end

    always #(CLOCK_PERIOD / 2) clk = ~clk;
    

    i2c_master u_i2c_master(
        .clk_32M(clk),
        .rst_n(rst_n),
        .Send(1'b0),
        .HS(1'b0),
        .burst_write(1'b1),
        .scl(scl),
        .sda(sda),
        .reg_addr(reg_addr),
        .data_in(data_in),
        .enable(1'b1),
        .done(w_done),
        .debug_count_delay(debug_count_delay),
        .debug_i2c_state(debug_i2c_state),
        .debug_write_data(debug_write_data)
    );
endmodule