`timescale 1ns/1ps

module top(
    input clk_in,
    input rst_n,

    output LED_R
);

    wire clk_main, clk_debug;
    wire delay_1s;
    wire[9:0] delay_1ms_cnt, delay_1s_cnt;

    clk_div u_clk_div(
        .clk_in(clk_in),
        .rst_n(rst_n),

        .clk_main(clk_main),
        .delay_1s(delay_1s),
        .delay_1ms_cnt(delay_1ms_cnt),
        .delay_1s_cnt(delay_1s_cnt),
        .clk_debug(clk_debug)
    );

    // led u_led(
    //     .clk(clk_main),
    //     .rst_n(rst_n),
    //     .delay_1s(delay_1s),

    //     .val(LED_R)
    // );

    pwm u_pwm(
        .clk(clk_main),
        .rst_n(rst_n),
        .delay_1s(delay_1s),
        .pulse_cnt(delay_1ms_cnt),
        .display_cnt(delay_1s_cnt),

        .val(LED_R)
    );

endmodule
