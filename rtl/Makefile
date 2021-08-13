SHELL=/bin/bash

# be carefully, this path will be used in clean(rm -rf)!!!
ROOT_PATH        := $(shell pwd)/tc_l2
BUILD_DIR        := ${ROOT_PATH}/build
MILL_OUT_DIR     := ${ROOT_PATH}/../out

AM_FOLDER_PATH   := $(ROOT_PATH)/am
AM_KERNEL_PATH   := $(AM_FOLDER_PATH)/am-kernels
VERSION_ID       := riscv64-nemu

export AM_HOME   := $(AM_FOLDER_PATH)/abstract-machine
export NEMU_HOME := $(ROOT_PATH)/NEMU
export NOOP_HOME := $(ROOT_PATH)

SIMPLETEST_HOME  := $(AM_FOLDER_PATH)/simple-tests
RISCVTEST_HOME   := $(AM_FOLDER_PATH)/riscv-tests
CPUTEST_HOME     := $(AM_KERNEL_PATH)/tests/cpu-tests
AMTEST_HOME      := $(AM_KERNEL_PATH)/tests/am-tests
DIFFTEST_HOME    := $(ROOT_PATH)/difftest



###### chisel target ######
millTest:
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

###### NEMU target ######
nemuBuild:
	$(MAKE) -C $(NEMU_HOME)

###### difftest target ######
difftestBuild:
	$(MAKE) -C $(DIFFTEST_HOME)

diffAllBuild: diffBuild difftestBuild

simpleTestBuild:
	$(MAKE) -C $(SIMPLETEST_HOME) ARCH=riscv64-nemu

###### riscv-tests target ######
riscvTestBuild:
	$(MAKE) -C $(RISCVTEST_HOME) ARCH=riscv64-nemu

###### (am)cpu-tests target ######
cpuTestBuild:
	$(MAKE) -C $(CPUTEST_HOME) ARCH=riscv64-nemu

###### (am)am-tests target ######
amTestBuild:
	$(MAKE) -C $(AMTEST_HOME) ARCH=riscv64-nemu

###### demo test target ######
demoTest:
	$(BUILD_DIR)/emu -i $(RISCVTEST_HOME)/build/addi-$(VERSION_ID).bin

###### recursive test target ######
binFile   = $(foreach dir, $(SIMPLETEST_HOME)/build, $(wildcard $(dir)/*.bin))
testName  = $(foreach file, $(binFile), $(patsubst %-riscv64-nemu, %, $(basename $(notdir $(file)))))
logFile   = $(foreach file, $(testName), $(patsubst %, %-log.txt, $(file)))
elfFile   = $(foreach dir, $(SIMPLETEST_HOME)/build, $(wildcard $(dir)/*.elf))
disasFile = $(foreach dir, $(SIMPLETEST_HOME)/build, $(wildcard $(dir)/*.txt))
$(shell mkdir -p $(SIMPLETEST_HOME)/build/log 1>/dev/null 2>&1)

simpleRecursiveTest: $(logFile) $(testName)
	@printf "[\033[0;33m%s\033[0m]\n" all-done

$(testName): %: $(SIMPLETEST_HOME)/build/%-riscv64-nemu.bin 
	@$(BUILD_DIR)/emu -i $< &> $(SIMPLETEST_HOME)/build/log/$@-log.txt
	@grep 'HIT GOOD TRAP' $(SIMPLETEST_HOME)/build/log/$@-log.txt
# if grep find the 'HIT GOOD TRAP', the return value is 0
ifeq (0, 0)
	@printf "[%-10s \033[0;32m%20s\033[0m]\n" $@ PASS
else
	@printf "[%-10s \033[0;31m%20s\033[0m]\n" $@ FAIL
endif

# @$(shell $(BUILD_DIR)/emu -i $(binFile) &> $(SIMPLETEST_HOME)/build/log/$1)
# ifeq $(shell grep HIT GOOD TRAP $(logFile))
# 	@printf "[%-10s \033[0;32m%20s\033[0m]\n" $@ PASS
# else
# 	@printf "[%-10s \033[0;31m%20s\033[0m]\n" $@ FAIL
# endif

$(logFile):
	$(shell touch $(SIMPLETEST_HOME)/build/log/$@)


###### clean target ######
cleanBuild:
	rm -rf $(BUILD_DIR)

cleanMillOut:
	rm -rf $(MILL_OUT_DIR)

cleanDepRepo:
	rm -rf $(AM_FOLDER_PATH) $(NEMU_HOME) $(DIFFTEST_HOME)

cleanAll: cleanBuild cleanMillOut cleanDepRepo

.PHONY: millTest univBuild diffBuild allBuild help compile bsp reformat checkformat \
        nemuBuild difftestBuild riscvTestBuild cpuTestBuild amTestBuild demoTest recursiveTest \
		cleanBuild cleanMillOut cleanDepRepo cleanAll
