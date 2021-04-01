`timescale 1ns/1ps

module i2c_master(
    input  clk_32M,        // main clock
    input  rst_n,          // low level reset 
    input  Send,           // Send: [0]: write [1]: Read
    input  HS,             // HS: [1]: high speed 400kHz  [0]: low speed 100kHz
    input  burst_write,    // burst_write: [1]: write multi bytes [0]: only write the slave device's register
    output scl,
    inout  sda,
    input[7:0]  reg_addr,  // write slave device's register addr
    input[7:0]  data_in,   // write slave device's register data
    output[7:0] data_out,  // output 8bit data

    input enable,
    output reg done,

    output[8:0] debug_count_delay,
    output[3:0] debug_i2c_state,
    output[7:0] debug_write_data
    );

    // frequency scale
    reg[3:0] scl_state;   // used for scl state indicator
    reg[8:0] count_delay; // 500 clyle, used to generate the require scl clock
    reg scl_r;            // register of scl

    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            count_delay <= 9'd0;
        end
        else if((count_delay == 9'd80) && HS) begin
            count_delay <= 9'd0; // to get the frequency of 400KHz
        end
        else if((count_delay == 9'd320) && !HS) begin
            count_delay <= 9'd0; // to get the frequency of 100KHz
        end 
        else begin
            count_delay <= count_delay + 1'b1; // clock count
        end
    end

    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            scl_state <= 4'd8;
        end
        else if(HS) begin
            case(count_delay)
                9'd20:    scl_state <= 4'd5; // scl_state=5: scl at the center of high level 
                9'd40:    scl_state <= 4'd6; // scl_state=6: scl at the negative edge
                9'd60:    scl_state <= 4'd7; // scl_state=7: scl at the center of low level
                9'd80:    scl_state <= 4'd4; // scl_state=4: scl at the positive edge
                default:  scl_state <= 4'd8;
            endcase
        end
        else begin
            case(count_delay)
                9'd80:     scl_state <= 4'd1; // scl_state=1: scl at the center of high level
                9'd160:    scl_state <= 4'd2; // scl_state=2: scl at the negative edge
                9'd240:    scl_state <= 4'd3; // scl_state=3: scl at the center of low level
                9'd320:    scl_state <= 4'd0; // scl_state=0: scl at the positive edge
                default:   scl_state <= 4'd8;
            endcase
        end
    end

    `define SCL_POS  (scl_state == 4'd0 || scl_state == 4'd4) // scl_state=0: scl at the positive edge
    `define SCL_HIG  (scl_state == 4'd1 || scl_state == 4'd5) // scl_state=1: scl at the center of high level
    `define SCL_NEG  (scl_state == 4'd2 || scl_state == 4'd6) // scl_state=2: scl at the negative edge
    `define SCL_LOW  (scl_state == 4'd3 || scl_state == 4'd7) // scl_state=3: scl at the center of low level

    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
            scl_r <= 1'b1;
        end
        else if(`SCL_POS) begin
            scl_r <= 1'b1; //scl at the positive edge
        end
        else if(`SCL_NEG) begin
            scl_r <= 1'b0; //scl at the negative edge
        end
    end

    //generate the final I2C
    assign scl = enable ? scl_r : 1'b1;

    //-------Setting the default slave address  --------------------------------------
    `define SlaveAddress_WRITE 8'b0111_1000
    `define SlaveAddress_READ  8'b0111_1001

    //-------The detail of I2C master write and read process----------------------
    reg[7:0] write_data; // the temporary writing data register of I2C 
    reg[7:0] read_data;  // the temporary  reading data register of I2C

    //The state of all I2C write and read process
    parameter IDLE   = 4'd0;
    parameter START1 = 4'd1;  // start write slave device
    parameter ADD1   = 4'd2;  // write slave addr
    parameter ACK1   = 4'd3;  // wait ack
    parameter WD     = 4'd4;  // write slave register
    parameter ACK2   = 4'd5;  // wait ack
    parameter WD2    = 4'd6;  // writh slave data
    parameter ACK3   = 4'd7;  // wait ack
    parameter START2 = 4'd8;  // start read slave device
    parameter ADD2   = 4'd9;  // write slave addr
    parameter ACK4   = 4'd10; // wait ack
    parameter RD     = 4'd11; // read data
    parameter NOACK  = 4'd12;
    parameter STOP   = 4'd13;

    reg[3:0] i2c_state; // the state regiser of I2C progress
    reg sda_r;          // register of sda
    reg sda_dir;        // the direction of sda [1]: control sda to ouput [0]: release sda to input
    reg[3:0] num;       // data number counter

    always @(posedge clk_32M or negedge rst_n) begin
        if(!rst_n) begin
                i2c_state <= IDLE;
                sda_r     <= 1'b1;
                sda_dir   <= 1'b0;
                num       <= 4'd0;
                read_data <= 8'b0;
                done      <= 1'b0;
            end
        else begin
           case(i2c_state)
                IDLE: begin
                    sda_dir <= 1'b1; // release I2C bus
                    sda_r   <= 1'b1;
                    done    <= 1'b0;
                    if(enable) begin // start write or read data
                        write_data <= `SlaveAddress_WRITE;    // send slave device address
                        i2c_state  <= START1;
                    end
                    else begin
                        i2c_state  <= IDLE;
                    end
                end
                START1: begin
                    if(`SCL_HIG) begin
                        if(enable) begin
                            sda_dir   <= 1'b1;
                            sda_r     <= 1'b0; // generate start signal
                            i2c_state <= ADD1;
                            num       <= 4'd0; // clear counter 
                        end
                        else begin
                            i2c_state <= IDLE;
                        end 
                    end
                    else begin
                        i2c_state <= START1; // wait scl to high level
                    end
                end
                ADD1: begin
                    if(`SCL_LOW) begin
                        if(enable) begin
                            if(num == 4'd8) begin
                                num       <= 4'd0;
                                sda_r     <= 1'b1;
                                sda_dir   <= 1'b0; // prepare to receive ack signal
                                i2c_state <= ACK1;
                            end
                            else begin
                                i2c_state <= ADD1;
                                num <= num + 1'b1;
                                case(num)
                                    4'd0: sda_r <= write_data[7];
                                    4'd1: sda_r <= write_data[6];
                                    4'd2: sda_r <= write_data[5];
                                    4'd3: sda_r <= write_data[4];
                                    4'd4: sda_r <= write_data[3];
                                    4'd5: sda_r <= write_data[2];
                                    4'd6: sda_r <= write_data[1];
                                    4'd7: sda_r <= write_data[0];
                                    default: ;
                                endcase
                            end
                        end
                        else begin
                            i2c_state <= IDLE;
                        end 
                    end 
                    else begin
                        i2c_state <= ADD1;
                    end
                end
                ACK1: begin
                    if(`SCL_HIG /*&& !sda*/) begin // detect ack or not detect
                        i2c_state  <= WD;          // go to next state
                        write_data <= reg_addr;    // write slave device's register addr
                    end
                    else begin
                        i2c_state <= ACK1;
                    end
                end
                WD: begin
                    if(`SCL_LOW) begin
                        if(enable) begin
                            if(num == 4'd8) begin
                                num       <= 4'd0;
                                sda_r     <= 1'b1;
                                sda_dir   <= 1'b0;
                                i2c_state <= ACK2;
                            end
                            else begin
                                sda_dir <= 1'b1;        
                                num <= num + 1'b1;
                                case(num)
                                    4'd0: sda_r <= write_data[7];
                                    4'd1: sda_r <= write_data[6];
                                    4'd2: sda_r <= write_data[5];
                                    4'd3: sda_r <= write_data[4];
                                    4'd4: sda_r <= write_data[3];
                                    4'd5: sda_r <= write_data[2];
                                    4'd6: sda_r <= write_data[1];
                                    4'd7: sda_r <= write_data[0];
                                    default: ;
                                endcase
                                i2c_state <= WD;
                            end
                        end
                        else begin
                            i2c_state <= IDLE;
                        end
                    end
                    else begin
                        i2c_state <= WD;
                    end
                end
                ACK2: begin
                    if( `SCL_HIG/*&& !sda*/) begin
                        if(!Send) begin           // judge write or read
                            if(burst_write) begin // judge if support the burst write
                               i2c_state <= WD2;
                               write_data <= data_in; 
                            end
                            else begin
                                i2c_state  <= STOP; // prepare to finish write process
                                write_data <= 8'b0;
                            end
                        end
                        else begin
                            write_data <= `SlaveAddress_READ;  // keeping on the process of read
                            i2c_state  <= START2;              // generate start signal 2
                        end
                    end
                    else begin
                        i2c_state <= ACK2;
                    end
                end
                WD2: begin
                    if(`SCL_LOW) begin
                        if(enable) begin
                            if(num == 4'd8) begin
                                num       <= 4'd0;
                                sda_r     <= 1'b1;
                                sda_dir   <= 1'b0;
                                i2c_state <= ACK3;
                            end
                            else begin
                                sda_dir <= 1'b1;
                                num <= num + 1'b1;
                                case(num)
                                    4'd0: sda_r <= write_data[7];
                                    4'd1: sda_r <= write_data[6];
                                    4'd2: sda_r <= write_data[5];
                                    4'd3: sda_r <= write_data[4];
                                    4'd4: sda_r <= write_data[3];
                                    4'd5: sda_r <= write_data[2];
                                    4'd6: sda_r <= write_data[1];
                                    4'd7: sda_r <= write_data[0];
                                    default: ;
                                endcase
                                i2c_state <= WD2;
                            end
                        end
                        else begin
                            i2c_state <= IDLE;
                        end
                    end
                    else begin
                        i2c_state <= WD2;
                    end
                end
                ACK3: begin
                    if( `SCL_HIG/*&& !sda*/) begin // some unnecessary!!
                        i2c_state  <= STOP;
                        write_data <= 8'b0;
                    end
                    else begin
                        i2c_state <= ACK3;
                    end
                end
                START2: begin
                    if(enable) begin
                        if(`SCL_LOW) begin
                            sda_dir   <= 1'b1;
                            sda_r     <= 1'b1;
                            i2c_state <= START2;
                        end
                        else if(`SCL_HIG) begin
                            sda_r     <= 1'b0;
                            i2c_state <= ADD2;
                        end
                        else begin
                            i2c_state <= START2;
                        end
                    end
                    else begin
                        i2c_state <= IDLE;
                    end
                end
                ADD2: begin // send the read address
                    if(`SCL_LOW)
                        if(enable) begin
                            if(num == 4'd8) begin
                                num       <= 4'd0;
                                sda_r     <= 1'b1;
                                sda_dir   <= 1'b0;
                                i2c_state <= ACK4;
                            end
                            else begin
                                num <= num + 1'b1;
                                case(num)
                                    4'd0: sda_r <= write_data[7];
                                    4'd1: sda_r <= write_data[6];
                                    4'd2: sda_r <= write_data[5];
                                    4'd3: sda_r <= write_data[4];
                                    4'd4: sda_r <= write_data[3];
                                    4'd5: sda_r <= write_data[2];
                                    4'd6: sda_r <= write_data[1];
                                    4'd7: sda_r <= write_data[0];
                                    default: ;
                                endcase
                                i2c_state <= ADD2;
                            end
                        end
                        else begin
                            i2c_state <= IDLE;
                        end
                    else begin
                        i2c_state <= ADD2;
                    end
                end
                ACK4: begin
                    if( `SCL_HIG/*&& !sda*/) begin
                        i2c_state <= RD;
                        sda_dir   <= 1'b0;
                    end
                    else begin
                        i2c_state <= ACK4;
                    end
                end
                RD: begin    // read the data from slave    
                    if(enable)            
                        if(num <= 4'd7) begin
                            i2c_state <= RD;
                            if(`SCL_HIG) begin    
                                num <= num + 1'b1;    
                                case (num)
                                    4'd0: read_data[7] <= sda;
                                    4'd1: read_data[6] <= sda;  
                                    4'd2: read_data[5] <= sda; 
                                    4'd3: read_data[4] <= sda; 
                                    4'd4: read_data[3] <= sda; 
                                    4'd5: read_data[2] <= sda; 
                                    4'd6: read_data[1] <= sda; 
                                    4'd7: read_data[0] <= sda; 
                                    default: ;
                                endcase
                            end
                        end
                        else if((`SCL_LOW) && (num == 4'd8)) begin
                            num       <= 4'd0;
                            sda_dir   <= 1'b1;
                            sda_r     <= 1'b1;
                            i2c_state <= NOACK;
                        end
                        else begin
                            i2c_state <= RD;
                        end
                    else begin
                        i2c_state <= IDLE;
                    end
                end
                NOACK: begin // send no ack
                    if( `SCL_HIG/*&& sda*/) begin
                        i2c_state <= STOP;
                    end
                    else begin
                        i2c_state <= NOACK;
                    end
                end
                STOP: begin       // generate the stop signal
                    if(`SCL_LOW) begin
                        sda_dir   <= 1'b1;
                        sda_r     <= 1'b0;
                        i2c_state <= STOP;
                    end
                    else if(`SCL_HIG) begin
                            sda_r     <= 1'b1;    
                            i2c_state <= IDLE;
                            done <= 1'b1;
                    end
                    else begin
                        i2c_state <= STOP;
                    end
                end
                default: begin
                    i2c_state <= IDLE;
                    done <= 1'b0;
                end
            endcase 
        end
    end

    assign sda = sda_dir ? sda_r : 1'bz;
    assign data_out = read_data;

    assign debug_count_delay = count_delay;
    assign debug_i2c_state = i2c_state;
    assign debug_write_data = write_data;

endmodule
