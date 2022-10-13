comp:
	verilator -Wall --cc --exe --build top.cpp top.v -o emu -Mdir build

run:
	./build/emu

.PHONY:
	comp run