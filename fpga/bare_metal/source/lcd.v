module lcd (
    input clk,
    input rst_n,

    output spi_clk,
    output spi_mosi,
    output spi_dc, // change in fsm
    output spi_cs,
    output spi_blk
);

    assign spi_blk = 1'b1;

    reg spi_start;
    wire spi_done;
    reg[7:0] spi_data;
    spi u_spi(
        .clk_50m(clk),
        .rst_n(rst_n),
        .spi_start(spi_start),
        .spi_data(spi_data),

        .spi_done(spi_done),
        .sck(spi_clk),
        .cs(spi_cs),
        .mosi(spi_mosi)
    );

    localparam IDLE = 'd0;
    localparam INIT_DELAY = 'd1;
    localparam INI_WRITE_RD = 'd2;

    reg[2:0] cur_state, nxt_state;

    always @(posedge clk or negedge rst_n) begin
        if(!rst_n) begin
            cur_state <= IDLE;
        end
        else begin
            cur_state <= nxt_state;
        end
    end

    always @(*) begin
        case(cur_state)
            IDLE: begin
               spi_start <= 1'd0;
               spi_data  <= 8'd0;
            end
        endcase
    end


endmodule