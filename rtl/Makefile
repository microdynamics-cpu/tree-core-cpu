BUILD_DIR = tc_l2/build


test:
	mill -i __.test

univBuild:
	mkdir -p $(BUILD_DIR)
	mill -i __.test.runMain sim.univtest.UnivTop -td $(BUILD_DIR)

diffBuild:
	mkdir -p $(BUILD_DIR)
	mill -i __.test.runMain sim.difftest.SimTop -td $(BUILD_DIR)

allBuild: univBuild diffBuild

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
	rm -rf ${BUILD_DIR}

.PHONY: test verilog help compile bsp reformat checkformat clean
