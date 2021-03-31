`timescale 1ns/1ps

module clk_div(
    input clk_in,
    input rst_n,

    output clk_main,
    output reg rst_n_main,

    output delay_1us,
    output delay_1ms,
    output delay_1s,
    output[9:0] delay_1us_cnt,
    output[9:0] delay_1ms_cnt,
    output[9:0] delay_1s_cnt,

    output clk_i2c,
    output clk_debug
    );

    localparam DELAY_1US_LIMIT = 10'd11;
    localparam DELAY_1MS_LIMIT = 10'd999;
    localparam DELAY_1S_LIMIT  = 10'd999;

    pll u_pll(
        .refclk(clk_in),
        .reset(~rst_n),

        .clk0_out(clk_main),
        .clk1_out(clk_debug),
        .clk2_out(clk_i2c)
    );

    // sync reset signal
    always @(posedge clk_main) begin
        if(!rst_n) begin
            rst_n_main <= 1'b0;
        end
        else begin
            rst_n_main <= 1'b1;
        end
    end


    counter #(
        .DELAY_LIMIT(DELAY_1US_LIMIT)
    ) u_counter_1us(
        .clk(clk_main),
        .rst_n(rst_n_main),
        .delay_en(1'b1),

        .counter_cnt(delay_1us_cnt),
        .counter_done(delay_1us)
    );

    counter #(
        .DELAY_LIMIT(DELAY_1MS_LIMIT)
    ) u_counter_1ms(
        .clk(clk_main),
        .rst_n(rst_n_main),
        .delay_en(delay_1us),

        .counter_cnt(delay_1ms_cnt),
        .counter_done(delay_1ms)
    );

    counter #(
        .DELAY_LIMIT(DELAY_1S_LIMIT)
    ) u_counter_1s(
        .clk(clk_main),
        .rst_n(rst_n_main),
        .delay_en(delay_1ms),

        .counter_cnt(delay_1s_cnt),
        .counter_done(delay_1s)
    );

endmodule
