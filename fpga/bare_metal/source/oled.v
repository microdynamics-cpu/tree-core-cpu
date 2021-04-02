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

    // delay 500ms
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

    // fsm
    localparam IDLE                        = 4'd0;
    localparam INIT_DELAY500MS             = 4'd1;
    localparam INIT_WRITE_REGISTER_CMD     = 4'd2;
    localparam INIT_WRITE_DAT              = 4'd3;
    localparam INIT_WRITE_DONE             = 4'd4;
    localparam INIT_CLEAR_PAGE             = 4'd5;
    localparam INIT_CLEAR_PAGE_CMD         = 4'd6;
    localparam INIT_CLEAR_COL              = 4'd7;
    localparam WORK_DISPLAY_ON             = 4'd12;
    localparam WORK_DISPLAY_OFF            = 4'd13;
    localparam WORK_UPDATE_GRAM            = 4'd14;
    localparam WORK_SHOW_POINT             = 4'd15;

    reg[3:0] cur_state, nxt_state;
    always @(*) begin
        case(cur_state)
            IDLE: begin
                nxt_state = INIT_DELAY500MS;
            end
            INIT_DELAY500MS: begin
                if(delay_done) begin
                    nxt_state = INIT_WRITE_REGISTER_CMD;
                end
                else begin
                    nxt_state = INIT_DELAY500MS;
                end
            end
            INIT_WRITE_REGISTER_CMD: begin
                if(cmd_iter == CONFIG_ADDR_NUM) begin
                    // nxt_state = INIT_WRITE_DONE;
                    nxt_state = INIT_CLEAR_PAGE;
                end
                else begin
                    nxt_state = INIT_WRITE_REGISTER_CMD;
                end
            end
            INIT_CLEAR_PAGE: begin
                if(clear_page_iter == INIT_CLEAR_PAGE_NUM) begin
                    nxt_state = INIT_WRITE_DONE;
                end
                else begin
                    nxt_state = INIT_CLEAR_PAGE_CMD;
                end
            end
            INIT_CLEAR_PAGE_CMD: begin
                if(clear_page_write_cmd_iter == INIT_UPDATE_ADDR_NUM) begin
                    nxt_state = INIT_CLEAR_COL;
                end
                else begin
                    nxt_state = INIT_CLEAR_PAGE_CMD;
                end
            end
            INIT_CLEAR_COL: begin
                if(clear_col_iter == INIT_CLEAR_COL_NUM) begin
                    nxt_state = INIT_CLEAR_PAGE;
                end
                else begin
                    nxt_state = INIT_CLEAR_COL;
                end
            end
            INIT_WRITE_DONE: begin
                nxt_state <= INIT_WRITE_DONE;
            end
            default: begin
                nxt_state = IDLE;
            end
        endcase
    end


    // localparam SSD1306_MAX_PAGE = 8'd8;
    // localparam SSD1306_MAX_RAW  = 8'd32;
    // localparam SSD1306_MAX_COL  = 8'd128;
    localparam CONFIG_ADDR_NUM = 8'd30; // very important!!
    localparam INIT_CLEAR_PAGE_NUM = 8'd9;
    localparam INIT_CLEAR_COL_NUM = 8'd129;
    localparam INIT_UPDATE_ADDR_NUM   = 8'd4;
    

    reg[7:0] config_cmd_addr[0:31];
    reg[7:0] config_page_addr[0:31];
    reg[7:0] cmd_iter, clear_page_iter, clear_page_write_cmd_iter, clear_col_iter;
    reg is_first, cmd_counter_en;

    reg load_w_reg_addr, en_w_reg_addr;
    reg load_w_page_addr, en_w_page_addr;
    reg load_w_page_cmd_addr, en_w_page_cmd_addr;
    reg load_w_col_addr, en_w_col_addr;
    always @(*) begin
        reg_addr = 8'h00;

        load_w_reg_addr      = 1'd0;
        load_w_page_addr     = 1'd0;
        load_w_page_cmd_addr = 1'd0;
        load_w_col_addr      = 1'd0;

        en_w_page_addr       = 1'd0;
        en_w_reg_addr        = 1'd0;
        en_w_page_cmd_addr   = 1'd0;
        en_w_col_addr        = 1'd0;

        w_enable = 1'd0;

        case(cur_state)
            IDLE: begin
                is_first             = 1'd1;
                load_w_reg_addr      = 1'd1;
                load_w_page_addr     = 1'd1;
                load_w_page_cmd_addr = 1'd1;
                load_w_col_addr      = 1'd1;
            end
            INIT_WRITE_REGISTER_CMD: begin
                 // very important!
                if(cmd_iter == CONFIG_ADDR_NUM) begin
                    is_first = 1'd1;
                    load_w_reg_addr = 1'd1;
                end
                else begin
                    if(w_done) begin
                        is_first = 1'd0;
                        en_w_reg_addr = 1'd1;
                        w_enable = 1'd1;
                    end
                    else begin
                        if(is_first) begin
                            is_first = 1'd0;
                            en_w_reg_addr = 1'd1;
                            w_enable = 1'd1;
                        end
                        else begin
                            is_first = 1'd0;
                            w_enable = 1'd1;
                        end
                    end
                end
            end
            INIT_CLEAR_PAGE: begin // init is very important!
                if(clear_page_iter == INIT_CLEAR_PAGE_NUM) begin
                    is_first = 1'd1;
                    load_w_page_addr = 1'd1;
                end
            end
            INIT_CLEAR_PAGE_CMD: begin
                if(clear_page_write_cmd_iter == INIT_UPDATE_ADDR_NUM) begin
                    is_first = 1'd1;
                    load_w_page_cmd_addr = 1'd1;
                end
                else begin
                    if(w_done) begin
                        is_first = 1'd0;
                        en_w_page_cmd_addr = 1'd1;
                        w_enable = 1'd1;
                    end
                    else begin
                        if(is_first) begin
                            is_first = 1'd0;
                            en_w_page_cmd_addr = 1'd1;
                            w_enable = 1'd1;
                        end
                        else begin
                            is_first = 1'd0;
                            w_enable = 1'd1;
                        end
                    end
                end
            end
            INIT_CLEAR_COL: begin
                reg_addr <= 8'h40; // very important write data!
                if(clear_col_iter == INIT_CLEAR_COL_NUM) begin
                    is_first = 1'd1;
                    load_w_col_addr = 1'd1;
                    en_w_page_addr = 1'd1; // very important!
                end
                else begin
                    if(w_done) begin
                        is_first = 1'd0;
                        en_w_col_addr = 1'd1;
                        w_enable = 1'd1;
                    end
                    else begin
                        if(is_first) begin
                            is_first = 1'd0;
                            en_w_col_addr = 1'd1;
                            w_enable = 1'd1;
                        end
                        else begin
                            is_first = 1'd0;
                            w_enable = 1'd1;
                        end
                    end
                end
            end
        endcase
    end

    // write the command(FSM)
    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            cur_state <= IDLE;
        end
        else begin
            cur_state <= nxt_state;
        end
    end

    // init the config data
    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
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

            config_cmd_addr[29] <= 8'h00; // can not set in the oled, just for fsm.
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
            config_page_addr[0] <= 8'hB0;
            config_page_addr[1] <= 8'h00;
            config_page_addr[2] <= 8'h10;
            config_page_addr[3] <= 8'h00;
        end
    end

    // data_in
    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            data_in <= 8'h00;
        end
        else if(en_w_reg_addr && cmd_iter < CONFIG_ADDR_NUM) begin
            data_in <= config_cmd_addr[cmd_iter];
        end
        else if(en_w_page_cmd_addr && clear_page_write_cmd_iter < INIT_UPDATE_ADDR_NUM) begin
            case(clear_page_write_cmd_iter)
                0: begin
                    data_in <= config_page_addr[clear_page_write_cmd_iter] + clear_page_iter;
                end
                1, 2, 3: begin
                    data_in <= config_page_addr[clear_page_write_cmd_iter];
                end
            endcase
        end
        else if(en_w_col_addr && clear_col_iter < INIT_CLEAR_COL_NUM) begin
            data_in <= 8'hFF; // set all points in the screen
        end
        else begin
            data_in <= data_in;
        end
    end

    // cmd_iter
    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            cmd_iter <= 8'd0;
        end
        else if(load_w_reg_addr) begin
            cmd_iter <= 8'd0;
        end
        else if(en_w_reg_addr) begin
            if(cmd_iter == CONFIG_ADDR_NUM) begin
                cmd_iter <= 8'd0;
            end
            else begin
                cmd_iter <= cmd_iter + 1'd1;
            end
        end
        else begin
            cmd_iter <= cmd_iter;
        end
    end

    // clear_page_iter;
    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            clear_page_iter <= 8'd0;
        end
        else if(load_w_page_addr) begin
            clear_page_iter <= 8'd0;
        end
        else if(en_w_page_addr) begin
            if(clear_page_iter == INIT_CLEAR_PAGE_NUM) begin
                clear_page_iter <= 8'd0;
            end
            else begin
                clear_page_iter <= clear_page_iter + 1'd1;
            end
        end
        else begin
            clear_page_iter <= clear_page_iter;
        end
    end
    // clear_page_write_cmd_iter
    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            clear_page_write_cmd_iter <= 8'd0;
        end
        else if(load_w_page_cmd_addr) begin
            clear_page_write_cmd_iter <= 8'd0;
        end
        else if(en_w_page_cmd_addr) begin
            if(clear_page_write_cmd_iter == INIT_UPDATE_ADDR_NUM) begin
                clear_page_write_cmd_iter <= 8'd0;
            end
            else begin
                clear_page_write_cmd_iter <= clear_page_write_cmd_iter + 1'd1;
            end
        end
        else begin
            clear_page_write_cmd_iter <= clear_page_write_cmd_iter;
        end
    end

    // clear_col_iter
    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            clear_col_iter <= 8'd0;
        end
        else if(load_w_col_addr) begin
            clear_col_iter <= 8'd0;
        end
        else if(en_w_col_addr) begin
            if(clear_col_iter == INIT_CLEAR_COL_NUM) begin
                clear_col_iter <= 8'd0;
            end
            else begin
                clear_col_iter <= clear_col_iter + 1'd1;
            end
        end
        else begin
            clear_col_iter <= clear_col_iter;
        end
    end
endmodule