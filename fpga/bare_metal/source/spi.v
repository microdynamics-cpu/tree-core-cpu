module spi(input clk_50m,
                     input rst_n,
                     input spi_start,
                     input[7:0] spi_data,
                     output reg spi_done,
                     output sck,
                     output reg cs,
                     output mosi
    );
reg load_c;
reg en_c;
reg load_a;
reg en_a;
reg load_b;
reg en_b;
wire [4:0]i;
parameter [4:0] s0 = 'b000001;
parameter [4:0] s1 = 'b000010;
parameter [4:0] s2 = 'b000100;
parameter [4:0] s3 = 'b001000;
parameter [4:0] s4 = 'b010000;
parameter [4:0] s5 = 'b100000;
reg [5:0]current_state = 'd0;
reg [5:0]next_state = 'd0;
always @(posedge clk_50m or negedge rst_n)
    if(!rst_n)
        current_state <= s0;
    else
        current_state <= next_state;
 
always @(*)
    case(current_state)
        s0:	begin
            if(spi_start)
                next_state = s1;
            else
                next_state = s0;
        end
        s1:	begin//该状态加载待发送的数据
            if(i == 'd1)
                next_state = s2;
            else
                next_state = s1;
        end
        s2:	begin //1,3,5,7,9,11,13,15
            if(i[0] == 1'b0)//
                next_state = s3;
            else
                next_state = s2;
        end
        s3:	begin //2,4,6,8,10,12,14,16
            if(i == 'd15)
                next_state = s4;
            else if(i[0] == 'd1)
                next_state = s2;
            else
                next_state = s3;
        end
        s4:	begin
            if(i == 'd16)
                next_state = s5;
            else
                next_state = s4;
        end
        s5:	begin
            if(i == 'd0)
                next_state = s0;
            else
                next_state = s5;
        end
        default:	next_state = s0;
    endcase
always @(*)
    case(current_state)
        s0:	begin///空闲状态
            load_c = 'd1;
            en_c = 'd0;
            load_a = 'd0;
            en_a = 'd0;
            load_b = 'd1;
            en_b = 'd0;
            spi_done = 'd0;
            cs = 'd1;
        end
        s1:	begin //加载待发送数据状态
            load_c = 'd0;
            en_c = 'd1;
            load_a = 'd1;
            en_a = 'd0;
            load_b = 'd0;
            en_b = 'd1;
            spi_done = 'd0;
            cs = 'd0;			
        end
        s2:	begin	//第一个时钟沿发送数据
            load_c = 'd0;
            en_c = 'd1;
            load_a = 'd0;
            en_a = 'd1;
            load_b = 'd0;
            en_b = 'd1;
            spi_done = 'd0;
            cs = 'd0;	
        end
        s3:	begin //第二个时钟沿采样数据
            load_c = 'd0;
            en_c = 'd1;
            load_a = 'd0;
            en_a = 'd0;
            load_b = 'd0;
            en_b = 'd1;
            spi_done = 'd0;
            cs = 'd0;
        end
        s4:	begin //数据发送完毕
            load_c = 'd0;
            en_c = 'd1;
            load_a = 'd0;
            en_a = 'd0;
            load_b = 'd0;
            en_b = 'd0;
            spi_done = 'd0;
            cs = 'd0;			
        end
        s5:	begin
            load_c = 'd0;
            en_c = 'd0;
            load_a = 'd0;
            en_a = 'd0;
            load_b = 'd0;
            en_b = 'd0;
            spi_done = 'd1;
            cs = 'd1;	
        end
        default:	begin
            load_c = 'd1;
            en_c = 'd0;
            load_a = 'd0;
            en_a = 'd0;
            load_b = 'd1;
            en_b = 'd0;
            spi_done = 'd0;
            cs = 'd1;			
        end
    endcase
// Instantiate the module
count_num count_num (
    .clk_50m(clk_50m), 
    .load_c(load_c), 
    .en_c(en_c), 
    .count(i)
    );
// Instantiate the module
left_shifter left_shifter (
    .clk_50m(clk_50m), 
    .load_a(load_a), 
    .en_a(en_a), 
    .spi_data_in(spi_data), 
    .mosi(mosi)
    );
// Instantiate the module
sck_generate sck_generate (
    .clk_50m(clk_50m), 
    .load_b(load_b), 
    .en_b(en_b), 
    .sck(sck)
    );
endmodule

module count_num(input clk_50m,
                      input load_c,
                      input en_c,
                      output reg[4:0]count
    );
always @(posedge clk_50m)	 
    if(load_c)
        count <= 'd0; 
    else if(en_c)	begin
        if(count == 'd16)
            count <= 'd0;
        else
            count <= count + 'd1;
    end
    else
        count <= count;
 
endmodule

module left_shifter(input clk_50m,
                          input load_a,
                          input en_a,
                          input [7:0]spi_data_in,
                          output mosi
    );
reg [7:0]data_reg;
always @(posedge clk_50m)
    if(load_a)
        data_reg <= spi_data_in;
    else if(en_a)
        data_reg <= {data_reg[6:0],1'b0};
    else
        data_reg <= data_reg;
assign mosi = data_reg[7];
endmodule

//SPI3模式下工作，SCK空闲时为高电平
//SPI0模式下工作，SCK空闲时为低电平(now) problem according to uvision5 code!
module sck_generate(input clk_50m,
                          input load_b,
                          input en_b,
                          output reg sck
    );
always @(posedge clk_50m)
    if(load_b)
        sck <= 'd1;
    else if(en_b)
        sck <= ~sck;
    else
        sck <= 'd1;
 
endmodule