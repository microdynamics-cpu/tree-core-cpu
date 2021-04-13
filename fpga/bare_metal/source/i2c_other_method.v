module i2c_master_mode(
    input clk_50m,
    input clk_in, //用来同步SDA，使其在SCL低电平改变
    input rst_n,
    input wr_en,
    input[7:0] addr_in,
    input[7:0] data_in,

    output reg done,
    inout SDA,
    output SCL,
    output reg ack1, //仿真信号
    output reg ack2  //仿真信号
    );

    wire[4:0] i;
    wire sda_r;
    reg load_a;      //SCl空闲时加载数据
    reg en_a;        //产生SCL
    reg load_b;      //加载待发送地址
    reg en_b;        //地址移位寄存器使能
    reg load_c;      //计数器加载数据
    reg en_c;        //计数器计数
    reg load_d;      //加载待发送数据
    reg en_d;
    reg SDA_link;    //1时SDA为输出，0时为输入
    reg sad_load;    //sda_r空闲时加载数据
    reg sda_en_addr; //将地址赋值给sda_r
    reg sda_en_data; //将数据赋值给sda_r
    reg start;
    reg stop;

    //reg ack1; //应答信号给ack1
    //reg ack2; //应答信号给ack2
    reg SDA_R = 'd0;
    assign SDA = SDA_link ? SDA_R : 1'b0; /*Z*/ //实际应为z，为仿真方便设置为0
    always @(posedge clk_in) begin
        SDA_R <= sda_r;
    end


    parameter[10:0] IDLE  = 11'b000_0000_0001;
    parameter[10:0] STRAT = 11'b000_0000_0010;
    parameter[10:0] ADDR0 = 11'b000_0000_0100; //加载待发送地址
    parameter[10:0] ADDR1 = 11'b000_0000_1000; //发送地址
    parameter[10:0] ADDR2 = 11'b000_0001_0000; //保持地址
    parameter[10:0] ACK1  = 11'b000_0010_0000;
    parameter[10:0] DATA0 = 11'b000_0100_0000; //加载待发送数据
    parameter[10:0] DATA1 = 11'b000_1000_0000; //发送数据
    parameter[10:0] ACK2  = 11'b001_0000_0000; //应答
    parameter[10:0] STOP  = 11'b010_0000_0000;
    parameter[10:0] DONE  = 11'b100_0000_0000;
    //parameter[11:0] DATA2 = 'b0001_0000_0000; //保持数据
    //parameter[11:0] ACK2 = 'b0010_0000_0000;
    //parameter[11:0] STOP = 'b0100_0000_0000;
    //parameter[11:0] DONE = 'b1000_0000_0000;


    reg[10:0] current_state = 'd0;
    reg[10:0] next_state    = 'd0;

    //次态转移
    always @(posedge clk_50m or negedge rst_n) begin
        if(!rst_n) begin
            current_state <= IDLE;
        end
        else begin
            current_state <= next_state;
        end
    end


    //状态跳变
    always @(*) begin
        case(current_state)
            IDLE: begin
                if(wr_en) begin
                    next_state = STRAT;
                end
                else begin
                    next_state = IDLE;
                end 
            end
            STRAT:    begin
                if(start) begin
                    next_state = ADDR0;
                end
                else begin
                    next_state = STRAT;
                end
            end
            ADDR0: begin
                if(i == 'd1) begin
                    next_state = ADDR1;
                end
                else begin
                    next_state = ADDR0;
                end
            end
            ADDR1: begin
                if(i == 'd16) begin
                    next_state = ACK1;
                end
                else if(i[0] == 'b0) begin //i为偶数
                    next_state = ADDR2;
                end
                else begin
                    next_state = ADDR1;
                end
            end
            ADDR2: begin
                if(i[0] == 'b1) begin
                    next_state = ADDR1;
                end
                else begin
                    next_state = ADDR2;
                end
            end
            ACK1: begin //ack1应答，同时将待发送的数据加载进移位寄存器
                if(i == 'd0) begin
                    next_state = DATA0;
                end
                else begin
                    next_state = ACK1;
                end
            end
            DATA0: begin
                if(i[0] == 'd1) begin
                    next_state = DATA1;
                end
                else begin
                    next_state = DATA0;
                end
            end
            DATA1: begin
                if(i == 'd16) begin
                    next_state = ACK2;
                end
                else if(i[0] == 'b0) begin
                    next_state = DATA0;
                end
                else begin
                    next_state = DATA1;
                end
            end
            ACK2: begin
                if(i == 'd0) begin
                    next_state = STOP;
                end
                else begin
                    next_state = ACK2;
                end
            end
            STOP: begin
                if(stop) begin
                    next_state = DONE;
                end
                else begin
                    next_state = STOP;
                end
            end
            DONE: begin
                if(done) begin
                    next_state = IDLE;
                end
                else begin
                    next_state = DONE;
                end
            end
        default: begin
            next_state = IDLE;
        end
        endcase
    end


    //输出赋值
    always @(*) begin
        case(current_state)
            IDLE: begin
                load_a      = 'd1; //SCl空闲时加载数据，初始化SCL
                en_a        = 'd0;
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0;
                load_c      = 'd1; //计数器加载数据
                en_c        = 'd0;
                load_d      = 'd0; //加载待发送地址
                en_d        = 'd0;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd1; //sda_r空闲时加载数据，初始化SDA
                sda_en_addr = 'd0; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd0; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0;
                ack2        = 'd0;
            end
            STRAT: begin
                load_a      = 'd1;//SCl空闲时加载数据
                en_a        = 'd0;
                load_b      = 'd0;///加载待发送地址
                en_b        = 'd0;
                load_c      = 'd1;///计数器加载数据
                en_c        = 'd0;
                load_d      = 'd0;///加载待发送地址
                en_d        = 'd0;
                SDA_link    = 'd1;//1时SDA为输出，0时为输入
                sad_load    = 'd1;///sda_r空闲时加载数据
                sda_en_addr = 'd0;//将地址赋值给sda_r
//              sda_en_ack1 = 'd0;//ack1时给sda_r赋值
                sda_en_data = 'd0;//将数据赋值给sda_r
//              sda_en_ack2 = 'd0;//ack2时给sda_r赋值
                start       = 'd1;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0;
                ack2        = 'd0;
            end
            ADDR0: begin
                load_a      = 'd0; //SCl空闲时加载数据
                en_a        = 'd1;
                load_b      = 'd1; //加载待发送地址
                en_b        = 'd0;
                load_c      = 'd0; //计数器加载数据
                en_c        = 'd1;
                load_d      = 'd0; //加载待发送地址
                en_d        = 'd0;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd0; //sda_r空闲时加载数据
                sda_en_addr = 'd1; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd0; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0;
                ack2        = 'd0;
            end
            ADDR1: begin
                load_a      = 'd0; //SCl空闲时加载数据
                en_a        = 'd1; //产生SCL
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd1; //地址移位寄存器使能
                load_c      = 'd0; //计数器加载数据
                en_c        = 'd1; //计数器计数
                load_d      = 'd0; //加载待发送地址
                en_d        = 'd0;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd0; //sda_r空闲时加载数据
                sda_en_addr = 'd1; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd0; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0;
                ack2        = 'd0;
            end
            ADDR2: begin
                load_a      = 'd0; //SCl空闲时加载数据
                en_a        = 'd1; //产生SCL
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0; //地址移位寄存器使能
                load_c      = 'd0; //计数器加载数据
                en_c        = 'd1; //计数器计数
                load_d      = 'd0; //加载待发送地址
                en_d        = 'd0;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd0; //sda_r空闲时加载数据
                sda_en_addr = 'd1; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd0; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0;
                ack2        = 'd0;
            end
            ACK1: begin
                load_a      = 'd0; //SCl空闲时加载数据
                en_a        = 'd1; //产生SCL
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0; //地址移位寄存器使能
                load_c      = 'd0; //计数器加载数据
                en_c        = 'd1; //计数器计数
                load_d      = 'd1; //加载待发送数据
                en_d        = 'd0;
                SDA_link    = 'd0; //1时SDA为输出，0时为输入
                sad_load    = 'd0; //sda_r空闲时加载数据
                sda_en_addr = 'd0; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd0; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = !SDA; //应答信号给ack1
                ack2        = 'd0;
            end
            DATA0: begin
                load_a      = 'd0; //SCl空闲时加载数据
                en_a        = 'd1; //产生SCL 
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0; //地址移位寄存器使能
                load_c      = 'd0; //计数器加载数据
                en_c        = 'd1; //计数器计数
                load_d      = 'd0; //加载待发送数据
                en_d        = 'd0;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd0; //sda_r空闲时加载数据
                sda_en_addr = 'd0; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd1; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0; //应答信号给ack1
                ack2        = 'd0;
            end
            DATA1: begin
                load_a      = 'd0; //SCl空闲时加载数据
                en_a        = 'd1; //产生SCL
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0; //地址移位寄存器使能
                load_c      = 'd0; //计数器加载数据
                en_c        = 'd1; //计数器计数
                load_d      = 'd0; ///加载待发送数据
                en_d        = 'd1;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd0; //sda_r空闲时加载数据
                sda_en_addr = 'd0; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd1; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0; //应答信号给ack1
                ack2        = 'd0;
            end
            ACK2: begin//应答后由于之后产生stop信号，所以此时应该就让SCL处于空闲状态
                load_a      = 'd1; //SCl空闲时加载数据
                en_a        = 'd0; //产生SCL
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0; //地址移位寄存器使能
                load_c      = 'd0; //计数器加载数据
                en_c        = 'd1; //计数器计数
                load_d      = 'd0; //加载待发送数据
                en_d        = 'd0;
                SDA_link    = 'd0; //1时SDA为输出，0时为输入
                sad_load    = 'd0; //sda_r空闲时加载数据
                sda_en_addr = 'd0; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd1; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0;  //应答信号给ack1
                ack2        = !SDA; //应答信号给ack2
            end
            STOP: begin
                load_a      = 'd1; //SCl空闲时加载数据
                en_a        = 'd0; //产生SCL
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0; //地址移位寄存器使能
                load_c      = 'd1; //计数器加载数据
                en_c        = 'd0; //计数器计数
                load_d      = 'd0; ///加载待发送数据
                en_d        = 'd0;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd1; //sda_r空闲时加载数据
                sda_en_addr = 'd0; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd0; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd1;
                done        = 'd0;
                ack1        = 'd0; //应答信号给ack1
                ack2        = 'd0; //应答信号给ack2
            end
            DONE: begin
                load_a      = 'd1; //SCl空闲时加载数据
                en_a        = 'd0; //产生SCL
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0; //地址移位寄存器使能
                load_c      = 'd1; //计数器加载数据
                en_c        = 'd0; //计数器计数
                load_d      = 'd0; //加载待发送数据
                en_d        = 'd0;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd1; //sda_r空闲时加载数据
                sda_en_addr = 'd0; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd0; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd1;
                ack1        = 'd0; //应答信号给ack1
                ack2        = 'd0; //应答信号给ack2
            end
            default: begin
                load_a      = 'd1; //SCl空闲时加载数据
                en_a        = 'd0;
                load_b      = 'd0; //加载待发送地址
                en_b        = 'd0;
                load_c      = 'd1; //计数器加载数据
                en_c        = 'd0;
                load_d      = 'd0; //加载待发送地址
                en_d        = 'd0;
                SDA_link    = 'd1; //1时SDA为输出，0时为输入
                sad_load    = 'd1; //sda_r空闲时加载数据
                sda_en_addr = 'd0; //将地址赋值给sda_r
//              sda_en_ack1 = 'd0; //ack1时给sda_r赋值
                sda_en_data = 'd0; //将数据赋值给sda_r
//              sda_en_ack2 = 'd0; //ack2时给sda_r赋值
                start       = 'd0;
                stop        = 'd0;
                done        = 'd0;
                ack1        = 'd0;
                ack2        = 'd0;
            end
        endcase
    end


    scl_generate scl_generate (
        .clk_50m(clk_50m), 
        .load_a(load_a), 
        .en_a(en_a), 
        .scl(SCL)
        );


    ad_left_shifter ad_left_shifter (
        .clk_50m(clk_50m), 
        .addr_in(addr_in), 
        .load_b(load_b), 
        .en_b(en_b),  
        .addr_o(addr_o)
        );


    count_num count_num (
        .clk_50m(clk_50m), 
        .load_c(load_c), 
        .en_c(en_c), 
        .count(i)
        );


    data_left_shifter data_left_shifter (
        .clk_50m(clk_50m), 
        .data_in(data_in), 
        .en_d(en_d), 
        .load_d(load_d), 
        .data_o(data_o)
        );


    SDA_strat_stop SDA_strat_stop (
        .clk_50m(clk_50m), 
        .start(start), 
        .stop(stop), 
        .edge_detect(edge_detect)
        );


    sdar_signal sdar_signal (
        .clk_50m(clk_50m), 
        .sad_load(sad_load), 
        .start(start), 
        .edge_detect(edge_detect), 
        .sda_en_addr(sda_en_addr), 
        .addr_o(addr_o), 
        .sda_en_data(sda_en_data), 
        .data_o(data_o), 
        .stop(stop), 
        .sda_r(sda_r)
        );
endmodule

module scl_generate(
    input clk_50m,
    input load_a,
    input en_a,

    output reg scl
    );

    always @(posedge clk_50m) begin
        if(load_a) begin
            scl <= 'd1;
        end
        else if(en_a) begin
            scl <= ~scl;
        end
        else begin
            scl <= scl;
        end
    end
 
endmodule

module ad_left_shifter(
    input clk_50m,
    input[7:0] addr_in,
    input load_b,
    input en_b,

    output addr_o
    );

    reg[7:0] addr_reg = 'd0;
    always @(posedge clk_50m) begin
        if(load_b) begin
            addr_reg <= addr_in;
        end
        else if(en_b=='b1) begin
            addr_reg <= {addr_reg[6:0],1'b0};
        end
        else begin
            addr_reg <= addr_reg;
        end
    end

    assign addr_o = addr_reg[7];

endmodule


module count_num(
    input clk_50m,
    input load_c,
    input en_c,

    output reg[4:0]count
    );

    always @(posedge clk_50m) begin
        if(load_c) begin
            count <= 'd0;
        end
        else if(en_c) begin
            if(count == 'd17) begin
                count <= 'd0;
            end
            else begin
                count <= count + 'd1;
            end
        end
        else begin
            count <= count;
        end
    end

endmodule

module data_left_shifter(
    input clk_50m,
    input[7:0] data_in,
    input en_d,
    input load_d,

    output data_o
    );

    reg [7:0]data_reg = 'd0;
    always @(posedge clk_50m) begin
        if(load_d) begin
            data_reg <= data_in;
        end
        else if(en_d) begin
            data_reg <= {data_reg[6:0],1'b0};
        end
        else begin
            data_reg <= data_reg;
        end
    end

    assign data_o = data_reg[7];

endmodule

module SDA_strat_stop(
    input clk_50m,
    input start,
    input stop,

    output reg edge_detect = 'd0
    );

    always @(posedge clk_50m) begin
        case({stop,start})
            2'b01:    edge_detect <= 'd0;
            2'b10:    edge_detect <= 'd1;
            default:  edge_detect <= 'd0;
        endcase
    end

endmodule


module sdar_signal(
    input clk_50m,
    input sad_load,
    input start,
    input edge_detect,
    input sda_en_addr,
    input addr_o,
    input sda_en_data,
    input data_o,
    input stop,

    output reg sda_r
    );

    always @(*) begin
        if(sad_load) begin
            sda_r <= 'd1;
        end
        else begin
            case({stop,sda_en_data,sda_en_addr,start})
                4'b0001: sda_r <= edge_detect;
                4'b0010: sda_r <= addr_o;
                4'b0100: sda_r <= data_o;
                4'b1000: sda_r <= edge_detect;
            default:     sda_r <= 'd1;
            endcase
        end
    end
 
endmodule