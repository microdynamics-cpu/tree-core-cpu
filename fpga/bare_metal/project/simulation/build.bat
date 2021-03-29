iverilog -o cpu_prototype_tb.o -y ../../source cpu_prototype_tb.v 
vvp -n cpu_prototype_tb.o
gtkwave cpu_prototype_tb.vcd