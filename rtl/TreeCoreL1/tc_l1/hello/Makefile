comp:
	verilator -Wall --cc --exe --build hello.cpp hello.v -o emu -Mdir build

run:
	./build/emu

.PHONY:
	comp run