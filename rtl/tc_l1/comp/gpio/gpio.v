module gpio(
    input wire clk,
	input wire rst_n,

    input wire we_i,
    input wire[31:0] addr_i,
    input wire[31:0] data_i,

    output reg[31:0] data_o,

    input wire[1:0] io_pin_i,
    output wire[31:0] reg_ctrl,
    output wire[31:0] reg_data
    );

    // GPIO控制寄存器
    localparam GPIO_CTRL = 4'h0;
    // GPIO数据寄存器
    localparam GPIO_DATA = 4'h4;

    // 每2位控制1个IO的模式，最多支持16个IO
    // 0: 高阻，1：输出，2：输入
    reg[31:0] gpio_ctrl;
    // 输入输出数据
    reg[31:0] gpio_data;

    assign reg_ctrl = gpio_ctrl;
    assign reg_data = gpio_data;


    // 写寄存器
    always@(posedge clk) 
    begin
        if(rst_n == 1'b0) 
        begin
            gpio_data <= 32'h0;
            gpio_ctrl <= 32'h0;
        end 
        else 
        begin
            if(we_i == 1'b1) 
            begin
                case(addr_i[3:0])
                    GPIO_CTRL:
                    begin
                        gpio_ctrl <= data_i;
                    end
                    GPIO_DATA:
                    begin
                        gpio_data <= data_i;
                    end
                endcase
            end
            else
            begin
                if(gpio_ctrl[1:0] == 2'b10)
                begin
                    gpio_data[0] <= io_pin_i[0];
                end
                if(gpio_ctrl[3:2] == 2'b10)
                begin
                    gpio_data[1] <= io_pin_i[1];
                end
            end
        end
    end

    // 读寄存器
    always@(*)
    begin
        if(rst_n == 1'b0)
        begin
            data_o = 32'h0;
        end
        else
        begin
            case(addr_i[3:0])
                GPIO_CTRL:
                begin
                    data_o = gpio_ctrl;
                end
                GPIO_DATA:
                begin
                    data_o = gpio_data;
                end
                default:
                begin
                    data_o = 32'h0;
                end
            endcase
        end
    end

endmodule
