module led (
    input clk,
    input rst_n,
    input delay_1s,

    output reg val
);

    always @(posedge clk or negedge rst_n) begin
        if(!rst_n) begin
            val <= 1'b1;
        end
        else if(delay_1s) begin
            val <= ~val;
        end
        else begin
            val <= val;
        end
    end
endmodule