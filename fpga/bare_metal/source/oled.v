module oled (
    input  clk_32M,
    input rst_n,
    output scl,
    inout sda
);

    reg w_enable;
    reg[7:0] reg_addr, data_in;
    wire w_done;

    i2c_master u_i2c_master(
        .clk_32M(clk_32M),
        .rst_n(rst_n),
        .Send(1'b0),
        .HS(1'b0),
        .burst_write(1'b1),
        .scl(scl),
        .sda(sda),
        .reg_addr(reg_addr),
        .data_in(data_in),
        .enable(w_enable),
        .done(w_done)
    );

    localparam DELAY_500MS_LIMIT = 32'd63_99_999;
    // localparam DELAY_500MS_LIMIT = 32'd999; // for tenstbench
    reg[31:0] delay_500ms_count;
    wire delay_done;

    localparam CONFIG_ADDR_NUM = 8'd29;
    reg[7:0] config_cmd_addr[0:31];
    reg[7:0] cmd_iter;

    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            delay_500ms_count <= 32'd0;
        end
        else if(delay_500ms_count == DELAY_500MS_LIMIT) begin
            delay_500ms_count <= delay_500ms_count;
        end
        else begin
            delay_500ms_count <= delay_500ms_count + 1'd1;
        end
    end

    assign delay_done = (delay_500ms_count == DELAY_500MS_LIMIT);

    localparam IDLE       = 3'd0;
    localparam WRITE_CMD  = 3'd1;
    localparam WRITE_DAT  = 3'd2;
    localparam WRITE_DONE = 3'd3;
    reg[2:0] cur_state;
    reg is_first;
    // write the command
    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            reg_addr  <= 8'h00;
            data_in   <= 8'h00;
            cmd_iter  <= 8'd0;
            cur_state <= IDLE;
            w_enable  <= 1'b0;
            is_first  <= 1'd1;

            config_cmd_addr[0]  <= 8'hA8; // 设置分辨率
            config_cmd_addr[1]  <= 8'h1F; //128*64:0x3f  128*32:0x1f

            config_cmd_addr[2]  <= 8'hDA; //设置COM硬件引脚配置,适应分辨率
            config_cmd_addr[3]  <= 8'h02; //0x12:0.96-128*64    0x02:0.96-128*32

            config_cmd_addr[4]  <= 8'hD3; //设置显示偏移
            config_cmd_addr[5]  <= 8'h00; //默认值00 没有偏移

            config_cmd_addr[6]  <= 8'h40; //设置显示开始行   0到63   第[5:0]位 01[xxxxx]  默认这五位是 000000B

            config_cmd_addr[7]  <= 8'hA1; //段segment重映射,对于IIC通讯的四脚OLED要设置成0xA1，如果设置成0xA1的话显示会反置

            config_cmd_addr[8]  <= 8'h81; //对比度设置指令
            config_cmd_addr[9]  <= 8'hFF; //亮度调节 0x00~0xff 即 1~255(亮度设置,越大越亮) 对比度的值

            config_cmd_addr[10] <= 8'hA4; //0xa4,输出遵循RAM内容    0xa5,输出忽略RAM内容

            config_cmd_addr[11] <= 8'hA6; //设置显示方式,正常显示:0xA6,反相显示:0xA7

            config_cmd_addr[12] <= 8'hD5; //设置显示时钟分频/振荡器频率
            config_cmd_addr[13] <= 8'hF0; //设置分率

            config_cmd_addr[14] <= 8'h8D; //充电泵设置
            config_cmd_addr[15] <= 8'h14; //0x14:允许在显示开启的时候使用  0x10:不允许在显示开启的时候使用

            config_cmd_addr[16] <= 8'hAE; //显示关闭 0xAF是开启  0xAE是关闭

            config_cmd_addr[17] <= 8'h20; //设置内存地址模式 有三种模式：水平，垂直，页寻址（默认）
            config_cmd_addr[18] <= 8'h02; //水平：0x00   垂直：0x01   页寻址：0x02

            config_cmd_addr[19] <= 8'hB0; //为页寻址模式设置页面开始地址，0-7

            config_cmd_addr[20] <= 8'hC8; //设置COM扫描方向[0xc0上下反置COM0到COM N-1 左到右] [0xc8正常COM N-1到COM0 右到左]
            config_cmd_addr[21] <= 8'h00; //设置低列地址
            config_cmd_addr[22] <= 8'h10; //设置高列地址

            config_cmd_addr[23] <= 8'h40; //设置显示开始行   0到63   第[5:0]位 01[xxxxx]  默认这五位是 000000b

            config_cmd_addr[24] <= 8'hD9; //设置预充电时期
            config_cmd_addr[25] <= 8'h22;

            config_cmd_addr[26] <= 8'hDB; //Set VCOMH Deselect Level 不是很懂，按照默认的设置就行了
            config_cmd_addr[27] <= 8'h20; //默认是0x20  0.77xVcc

            config_cmd_addr[28] <= 8'hAF; //设置完毕，显示开启 0xAF是开启  0xAE是关闭

            // https://blog.csdn.net/weixin_37344017/article/details/88574870?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522161727824516780269880909%2522%252C%2522scm%2522%253A%252220140713.130102334..%2522%257D&request_id=161727824516780269880909&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduend~default-1-88574870.first_rank_v2_pc_rank_v29&utm_term=0.91+oled&spm=1018.2226.3001.4187
            // config_cmd_addr[0] <= 8'hAE; // display off
            // config_cmd_addr[1] <= 8'hD5; // set memory addressing Mode
            // config_cmd_addr[2] <= 8'h80; // 分频因子
            // config_cmd_addr[3] <= 8'hA8; // 设置驱动路数
            // config_cmd_addr[4] <= 8'h1F; // 默认0X3f(1/64) 0x1f(1/32)
            // config_cmd_addr[5] <= 8'hD3; // 设置显示偏移
            // config_cmd_addr[6] <= 8'h00; // 默认值00
            // config_cmd_addr[7] <= 8'h40; // 设置开始行 【5:0】，行数
            // config_cmd_addr[8] <= 8'h8D; // 电荷泵设置
            // config_cmd_addr[9] <= 8'h14; // bit2,开启/关闭
            // config_cmd_addr[10] <= 8'h20; // 设置内存地址模式
            // config_cmd_addr[11] <= 8'h02; // [[1:0],00，列地址模式;01，行地址模式;10,页地址模式;默认10;
            // config_cmd_addr[12] <= 8'hA1; // 段重定义设置,bit0:0,0->0;1,0->127;
            // config_cmd_addr[13] <= 8'hC8; // 设置COM扫描方向
            // config_cmd_addr[14] <= 8'hDA; // 设置COM硬件引脚配置
            // config_cmd_addr[15] <= 8'h02; // 0.91英寸128*32分辨率
            // config_cmd_addr[16] <= 8'h81; // 对比度设置
            // config_cmd_addr[17] <= 8'h8f; // 1~255(亮度设置,越大越亮)
            // config_cmd_addr[18] <= 8'hD9; // 设置预充电周期
            // config_cmd_addr[19] <= 8'hf1; // [3:0],PHASE 1;[7:4],PHASE 2;
            // config_cmd_addr[20] <= 8'hDB; // 设置VCOMH 电压倍率
            // config_cmd_addr[21] <= 8'h40; // [6:4] 000,0.65*vcc;001,0.77*vcc;011,0.83*vcc;
            // config_cmd_addr[22] <= 8'hA4; // 全局显示开启;bit0:1,开启;0,关闭;(白屏/黑屏)
            // config_cmd_addr[23] <= 8'hA6; // 设置显示方式;bit0:1,反相显示;0,正常显示
            // config_cmd_addr[24] <= 8'h2E; // 停用滚动条
            // config_cmd_addr[25] <= 8'hAF; // 开启显示
        end
        else begin
            if(delay_done) begin
                case(cur_state)
                    IDLE: begin
                        cur_state <= WRITE_CMD; // need to improve to support write data
                        w_enable  <= 1'b0;
                    end
                    WRITE_CMD: begin
                        reg_addr <= 8'h00;
                        if(cmd_iter == CONFIG_ADDR_NUM) begin
                            cur_state <= WRITE_DONE;
                        end
                        else begin
                            cur_state <= WRITE_CMD;
                            if(w_done) begin
                                data_in   <= config_cmd_addr[cmd_iter];
                                cmd_iter  <= cmd_iter + 1'd1;
                                w_enable  <= 1'b1;
                            end
                            else begin // also satisfy for the first cmd.
                                if(is_first) begin
                                    is_first <= 1'd0;
                                    w_enable <= 1'd1;
                                end
                                else begin
                                    is_first <= is_first;
                                    w_enable <= w_enable;
                                end
                                data_in   <= config_cmd_addr[cmd_iter];
                                cmd_iter  <= cmd_iter;
                            end
                        end
                    end
                    WRITE_DAT: begin
                        reg_addr <= 8'h40;
                    end
                    WRITE_DONE: begin
                        cur_state <= WRITE_DONE; // need to improve to support write data
                        w_enable  <= 1'b0;
                    end
                    default: begin
                        cur_state <= IDLE;
                    end
                endcase
            end
            else begin
                reg_addr <= reg_addr;
                cmd_iter <= cmd_iter;
                data_in  <= data_in;
            end
        end
    end

endmodule