module led (
    input clk,
    input rst_n,
    input delay_1s,
    input led_load_en,
    input wire[1:0] led_sel,
    output reg[2:0] led
);

    always @(posedge clk or negedge rst_n) begin
        if(!rst_n) begin
            led <= 3'b111;
        end
        else if(led_load_en) begin
            led <= 3'b111;
        end
        else if(delay_1s) begin
            led[led_sel] <= ~led[led_sel];
        end
        else begin
            led[led_sel] <= led[led_sel];
        end
    end
endmodule