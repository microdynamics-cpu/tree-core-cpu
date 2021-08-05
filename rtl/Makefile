BUILD_DIR = ./build


test:
	mill -i __.test

univBuild:
	mkdir -p $(BUILD_DIR)/univ-build
	mill -i __.test.runMain UnivTop -td $(BUILD_DIR)/univ-build

diffBuild:
	mkdir -p $(BUILD_DIR)/diff-build
	mill -i __.test.runMain SimTop -td $(BUILD_DIR)/diff-build

help:
	mill -i __.test.runMain Top --help

compile:
	mill -i __.compile

bsp:
	mill -i mill.bsp.BSP/install

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

clean:
	-rm -rf $(BUILD_DIR)

.PHONY: test verilog help compile bsp reformat checkformat clean
