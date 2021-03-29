module pwm (
    input clk,
    input rst_n,
    input delay_1s,
    input[9:0] pulse_cnt,
    input[9:0] display_cnt,
    
    output val
);
    
    reg display_mode;
    always @(posedge clk or negedge rst_n) begin
        if(!rst_n) begin
            display_mode <= 1'b0;
        end
        else if(delay_1s) begin
            display_mode <= ~display_mode;
        end
        else begin
            display_mode <= display_mode;
        end
    end

    reg pwm_on;
    always @(posedge clk or negedge rst_n) begin
        if(!rst_n) begin
            pwm_on <= 1'b0;
        end
        else begin
            case(display_mode)
                1'b0: pwm_on <= (pulse_cnt < display_cnt) ? 1'b1 : 1'b0;
                1'b1: pwm_on <= (pulse_cnt < display_cnt) ? 1'b0 : 1'b1; 
            endcase
        end
    end

    assign val = pwm_on;

endmodule