`timescale 1ns/1ps

module top(
    input clk_in,
    input rst_n,

    output wire[2:0] led,
    output scl,
    inout  sda
);

    wire clk_main, clk_i2c, clk_debug, rst_n_main;
    wire delay_1s;
    wire[9:0] delay_1ms_cnt, delay_1s_cnt;

    clk_div u_clk_div(
        .clk_in(clk_in),
        .rst_n(rst_n),

        .clk_main(clk_main),
        .rst_n_main(rst_n_main),
        .delay_1s(delay_1s),
        .delay_1ms_cnt(delay_1ms_cnt),
        .delay_1s_cnt(delay_1s_cnt),

        .clk_i2c(clk_i2c),
        .clk_debug(clk_debug)
    );

    wire[1:0] doa;
    reg[7:0] addra;
    rom u_rom(
        .clka(clk_main),
        .rsta(~rst_n_main),
        .addra(addra),

        .doa(doa)
    );

    reg[1:0] led_sel;
    reg load_en;
    reg[2:0] delay_6s_count;

    always @(posedge clk_main or negedge rst_n_main) begin
        if(!rst_n_main) begin
            load_en <= 1'd1;
            led_sel <= 2'd0;
            delay_6s_count <= 3'd0;
            addra   <= 8'd0;
        end
        else begin
            if(delay_6s_count == 3'd6) begin
                delay_6s_count <= 3'd0;
                led_sel <= doa;
                load_en <= 1'd1;
                if(addra == 8'd6) begin
                    addra <= addra;
                end
                else begin
                    addra <= addra + 8'd1;
                end
            end
            else begin
                load_en <= 1'd0;
                if(delay_1s) begin
                    delay_6s_count <= delay_6s_count + 1'd1;
                end
                else begin
                    delay_6s_count <= delay_6s_count;
                end
            end
        end
    end

    led u_led(
        .clk(clk_main),
        .rst_n(rst_n_main),
        .delay_1s(delay_1s),
        .load_en(load_en),
        .led_sel(led_sel),
        .led(led)
    );

    // pwm u_pwm(
    //     .clk(clk_main),
    //     .rst_n(rst_n_main),
    //     .delay_1s(delay_1s),
    //     .pulse_cnt(delay_1ms_cnt),
    //     .display_cnt(delay_1s_cnt),

    //     .val(LED_R)
    // );

    // oled u_oled(
    //     .clk_32M(clk_i2c),
    //     .rst_n(rst_n),
    //     .scl(scl),
    //     .sda(sda)
    // );

endmodule
