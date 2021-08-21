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
COREMARK_HOME    := $(AM_KERNEL_PATH)/benchmarks/coremark
DHRYSTONE_HOME   := $(AM_KERNEL_PATH)/benchmarks/dhrystone
MICROBENCH_HOME  := $(AM_KERNEL_PATH)/benchmarks/microbench
DIFFTEST_HOME    := $(ROOT_PATH)/difftest

AllTestCaseNum  := 0
PassTestCaseNum := 0
FailTestCaseNum := 0

define getRecursiveTestRes
	-@$(BUILD_DIR)/emu -i $< &> $(1)/build/log/$@-log.txt
	@printf "[%30s] " $@
	@if (grep 'HIT GOOD TRAP' $(1)/build/log/$@-log.txt > /dev/null) then echo -e "\033[1;32mPASS!\033[0m"; else echo -e "\033[1;31mFAIL!\033[0m"; fi
endef

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
	$(MAKE) -C $(SIMPLETEST_HOME) ARCH=riscv64-mycpu

###### riscv-tests target ######
riscvTestBuild:
	$(MAKE) -C $(RISCVTEST_HOME) ARCH=riscv64-mycpu

###### (am)cpu-tests target ######
cpuTestBuild:
	$(MAKE) -C $(CPUTEST_HOME) ARCH=riscv64-mycpu

###### (am)am-tests target ######
# now only test the rtl-time
amTestBuild:
	$(MAKE) -C $(AMTEST_HOME) ARCH=riscv64-mycpu mainargs=t

###### (am)coremark target ######
coremarkTestBuild:
	$(MAKE) -C $(COREMARK_HOME) ARCH=riscv64-mycpu

###### (am)dhrystone target ######
dhrystoneTestBuild:
	$(MAKE) -C $(DHRYSTONE_HOME) ARCH=riscv64-mycpu


###### (am)microbench target ######
microbenchTestBuild:
	$(MAKE) -C $(MICROBENCH_HOME) ARCH=riscv64-mycpu mainargs=test


###### demo test target ######
demoTest:
	$(BUILD_DIR)/emu -i $(RISCVTEST_HOME)/build/addi-$(VERSION_ID).bin


###### simple test recursive test target ######
simpleTestbinFile   = $(foreach dir, $(SIMPLETEST_HOME)/build, $(wildcard $(dir)/*.bin))
simpleTestCaseName  = $(foreach file, $(simpleTestbinFile), $(patsubst %-riscv64-mycpu, simpletest-%, $(basename $(notdir $(file)))))
simpleTestLogFile   = $(foreach file, $(simpleTestCaseName), $(patsubst %, %-log.txt, $(file)))
$(shell mkdir -p $(SIMPLETEST_HOME)/build/log 1>/dev/null 2>&1)

simpleRecursiveTest: $(simpleTestLogFile) $(simpleTestCaseName)
	@printf "[\033[0;33m%s\033[0m]\n" all-done

$(simpleTestLogFile):
	$(shell touch $(SIMPLETEST_HOME)/build/log/$@)

$(simpleTestCaseName): simpletest-%: $(SIMPLETEST_HOME)/build/%-riscv64-mycpu.bin 
	@$(call getRecursiveTestRes, $(SIMPLETEST_HOME))


###### riscv test recursive test target ######
riscvTestbinFile   = $(foreach dir, $(RISCVTEST_HOME)/build, $(wildcard $(dir)/*.bin))
riscvTestCaseName  = $(foreach file, $(riscvTestbinFile), $(patsubst %-riscv64-mycpu, riscvtest-%, $(basename $(notdir $(file)))))
riscvTestLogFile   = $(foreach file, $(riscvTestCaseName), $(patsubst %, %-log.txt, $(file)))
$(shell mkdir -p $(RISCVTEST_HOME)/build/log 1>/dev/null 2>&1)
riscvRecursiveTest: $(riscvTestLogFile) $(riscvTestCaseName)
	@printf "[\033[0;33mall-done\033[0m]\n"
	@printf "[\033[0;33mAll: %s  \033[0;32mPASS: %s \033[0;31mFAIL: %s \033[0m]\n" $(AllTestCaseNum) $(PassTestCaseNum) $(FailTestCaseNum)

$(riscvTestLogFile):
	$(shell touch $(RISCVTEST_HOME)/build/log/$@)

$(riscvTestCaseName): riscvtest-%: $(RISCVTEST_HOME)/build/%-riscv64-mycpu.bin
	$(call getRecursiveTestRes, $(RISCVTEST_HOME))


###### cpu test recursive test target ######
cpuTestbinFile   = $(foreach dir, $(CPUTEST_HOME)/build, $(wildcard $(dir)/*.bin))
cpuTestCaseName  = $(foreach file, $(cpuTestbinFile), $(patsubst %-riscv64-mycpu, cputest-%, $(basename $(notdir $(file)))))
cpuTestLogFile   = $(foreach file, $(cpuTestCaseName), $(patsubst %, %-log.txt, $(file)))
$(shell mkdir -p $(CPUTEST_HOME)/build/log 1>/dev/null 2>&1)
cpuRecursiveTest: $(cpuTestLogFile) $(cpuTestCaseName)
	@printf "[\033[0;33mall-done\033[0m]\n"
	@printf "[\033[0;33mAll: %s  \033[0;32mPASS: %s \033[0;31mFAIL: %s \033[0m]\n" $(AllTestCaseNum) $(PassTestCaseNum) $(FailTestCaseNum)

$(cpuTestLogFile):
	$(shell touch $(CPUTEST_HOME)/build/log/$@)

$(cpuTestCaseName): cputest-%: $(CPUTEST_HOME)/build/%-riscv64-mycpu.bin
	$(call getRecursiveTestRes, $(CPUTEST_HOME))


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
