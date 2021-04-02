@REM iverilog -o cpu_prototype_tb.o -y ../../source cpu_prototype_tb.v 
@REM vvp -n cpu_prototype_tb.o
@REM gtkwave cpu_prototype_tb.vcd

@REM TEST I2C MASTER MODE
@REM iverilog -o i2c_master_tb.o -y ../../source i2c_master_tb.v 
@REM vvp -n i2c_master_tb.o
@REM gtkwave i2c_master_tb.vcd

iverilog -o oled_tb.o -y ../../source oled_tb.v 
vvp -n oled_tb.o
gtkwave oled_tb.vcd

@REM iverilog -o spi_tb.o -y ../../source spi_tb.v 
@REM vvp -n spi_tb.o
@REM gtkwave spi_tb.vcd