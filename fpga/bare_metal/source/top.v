`timescale 1ns/1ps

module top(
    input clk_in,
    input rst_n,

    output wire[2:0] led,

    //i2c for 0.91 oled
    output scl,
    inout  sda
    //spi for 0.96 tft-lcd
    // output spi_clk,
    // output spi_mosi,
    // output spi_res,
    // output spi_dc,
    // output spi_cs,
    // output spi_blk
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

    wire[1:0] rom_doa;
    reg[7:0] rom_addra;
    rom u_rom(
        .clka(clk_main),
        .rsta(~rst_n_main),
        .addra(rom_addra),

        .doa(rom_doa)
    );

    reg ram_cea, ram_ocea, ram_wea;
    wire[1:0] ram_doa;
    reg[1:0] ram_dia;
    reg[7:0] ram_addra;
    ram u_ram(
        .doa(ram_doa),
        .dia(ram_dia),
        .addra(ram_addra),
        .cea(ram_cea),
        .ocea(ram_ocea),
        .clka(clk_main),
        .wea(ram_wea),
        .rsta(!rst_n_main)
    );

    reg[1:0] led_sel;
    reg led_load_en;
    reg[2:0] delay_6s_count;
    always @(posedge clk_main or negedge rst_n_main) begin
        if(!rst_n_main) begin
            // ram ctr signal
            ram_cea   <= 1'd0;
            ram_wea   <= 1'd0;
            ram_ocea  <= 1'd0;
            // ram addr signal
            ram_addra <= 8'd0;
            // rom addr signal
            rom_addra <= 8'd0;
            // led
            led_load_en <= 1'd1;
            led_sel <= 2'd1;
            delay_6s_count <= 3'd0;
        end
        else if(delay_6s_count == 3'd6) begin
            delay_6s_count <= 3'd0;
            led_sel <= ram_doa;
            led_load_en <= 1'd1;

            if(ram_addra == 8'd6) begin
                ram_addra <= 8'd0;
            end
            else begin
                ram_addra <= ram_addra + 8'd1;
            end
        end
        else begin
            led_load_en <= 1'd0;
            if(delay_1s) begin
                delay_6s_count <= delay_6s_count + 1'd1;
            end
            else begin
                delay_6s_count <= delay_6s_count;
            end

            if(rom_addra == 8'd6) begin
                ram_cea   <= 1'd1;
                ram_wea   <= 1'd0;
                ram_ocea  <= 1'd1;

                ram_dia   <= ram_dia;
                ram_addra <= ram_addra;
                rom_addra <= rom_addra;
            end
            else begin
                ram_cea   <= 1'd1;
                ram_wea   <= 1'd1;
                ram_ocea  <= 1'd1;

                ram_dia <= rom_doa;

                ram_addra <= ram_addra + 8'd1;
                rom_addra <= rom_addra + 8'd1;
            end
        end
    end


    led u_led(
        .clk(clk_main),
        .rst_n(rst_n_main),
        .delay_1s(delay_1s),
        .led_load_en(led_load_en),
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

    oled u_oled(
        .clk_32M(clk_i2c),
        .rst_n(rst_n),
        .scl(scl),
        .sda(sda)
    );

    // lcd u_lcd(
    //     .clk(clk_main),
    //     .rst_n(rst_n_main),

    //     .spi_clk(spi_clk),
    //     .spi_mosi(spi_mosi),
    //     .spi_dc(spi_dc),
    //     .spi_cs(spi_cs),
    //     .spi_blk(spi_blk)
    // );
endmodule
