module counter #(
    parameter DELAY_LIMIT = 10'd0
)
(
    input clk,
    input rst_n,
    input delay_en,

    output[9:0] counter_cnt,
    output counter_done
);
    reg[9:0] count;
    always @(posedge clk or negedge rst_n) begin
        if(!rst_n) begin
            count <= 10'd0;
        end
        else if(delay_en) begin
            if(count == DELAY_LIMIT) begin
                count <= 10'd0;
            end
            else begin
                count <= count + 1'd1;
            end
        end
        else begin
            count <= count;
        end
    end

    assign counter_cnt  = count;
    assign counter_done = (count == DELAY_LIMIT && delay_en) ? 1'b1 : 1'b0;
endmodule