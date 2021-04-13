module spi_tb;
 
	// Inputs
	reg clk_50m;
	reg rst_n;
	reg spi_start;
	reg [7:0]spi_data;
	// Outputs
	wire spi_done;
	wire sck;
	wire cs;
	wire mosi;
 
	// Instantiate the Unit Under Test (UUT)
	spi uut (
		.clk_50m(clk_50m), 
		.rst_n(rst_n), 
		.spi_start(spi_start), 
		.spi_done(spi_done), 
		.sck(sck), 
		.cs(cs), 
		.spi_data(spi_data),
		.mosi(mosi)
	);
 
	initial begin
		// Initialize Inputs
		clk_50m = 0;
		rst_n = 0;
		spi_start = 0;
		spi_data = 'd0;
		// Wait 100 ns for global reset to finish
		#100;
        
		// Add stimulus here
        #(1000_00);
        $finish;
	end

        initial begin
        $dumpfile("spi_tb.vcd");
        $dumpvars(0, spi_tb);
    end

    always #5 clk_50m = ~clk_50m;
reg [4:0] count = 'd0;
always @(posedge clk_50m)	
	if(count == 'd20)
		count <= 'd20;
	else
		count <= count + 'd1;
always @(posedge clk_50m)
	if(count <= 'd10)
		rst_n <= 'd0;
	else
		rst_n <= 'd1;
reg [9:0]cnt = 'd0; 
always @(posedge clk_50m)
	if(spi_done)	
		cnt <= 'd0;
	else if(cnt == 'd500)
		cnt <= 'd500;
	else
		cnt <= cnt + 'd1;
always @(posedge clk_50m)
 if(cnt=='d499)	begin
		spi_start <= 'd1;
		spi_data <= 'b10101010;
	end
	else	begin
		spi_start <= 'd0;
		spi_data <= spi_data;
	end
		
endmodule
 