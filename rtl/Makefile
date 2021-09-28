SHELL=/bin/bash

# be carefully, this path will be used in clean(rm -rf)!!!
ROOT_PATH        := $(shell pwd)/tc_l2
BUILD_DIR        := ${ROOT_PATH}/build
MILL_OUT_DIR     := ${ROOT_PATH}/../out

AM_FOLDER_PATH   := $(ROOT_PATH)/am
AM_KERNEL_PATH   := $(AM_FOLDER_PATH)/am-kernels
VERSION_ID       := riscv64-nemu

SIMPLETEST_HOME  := $(AM_FOLDER_PATH)/simple-tests
RISCVTEST_HOME   := $(AM_FOLDER_PATH)/riscv-tests
FCEMUX_HOME      := $(AM_FOLDER_PATH)/fceux-am
CPUTEST_HOME     := $(AM_KERNEL_PATH)/tests/cpu-tests
AMTEST_HOME      := $(AM_KERNEL_PATH)/tests/am-tests
COREMARK_HOME    := $(AM_KERNEL_PATH)/benchmarks/coremark
DHRYSTONE_HOME   := $(AM_KERNEL_PATH)/benchmarks/dhrystone
MICROBENCH_HOME  := $(AM_KERNEL_PATH)/benchmarks/microbench
DIFFTEST_HOME    := $(ROOT_PATH)/difftest
DRAMSIM3_HOME    := $(ROOT_PATH)/DRAMsim3

export AM_HOME       := $(AM_FOLDER_PATH)/abstract-machine
export NEMU_HOME     := $(ROOT_PATH)/NEMU
export NOOP_HOME     := $(ROOT_PATH)
export DRAMSIM3_HOME := $(DRAMSIM3_HOME)

define getRecursiveTestRes
	@echo $$(echo $$(cat $(1)/build/log/allcasenum-log.txt) + 1 | bc) > $(1)/build/log/allcasenum-log.txt
	-@$(BUILD_DIR)/emu -i $< &> $(1)/build/log/$@-log.txt

	@printf "[%30s] " $@
	@if (grep 'HIT GOOD TRAP' $(1)/build/log/$@-log.txt > /dev/null) then \
		echo -e "\033[1;32mPASS! ipc =$$(grep 'IPC' $(1)/build/log/$@-log.txt | cut -d = -f4)\033[0m" \
		$$(echo $$(echo $$(cat $(1)/build/log/passcasenum-log.txt) + 1 | bc) > $(1)/build/log/passcasenum-log.txt); \
	else \
		echo -e "\033[1;31mFAIL!\033[0m"; \
	fi
endef

###### chisel target ######
millTest:
	mill -i __.test

diffBuild:
	mkdir -p $(BUILD_DIR)
	mill -i tc_l2.runMain treecorel2.TopMain -td $(BUILD_DIR)

help:
	mill -i tc_l2.runMain treecorel2.TopMain --help

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

###### DRAMsim3 target ######
dramsim3Build:
	mkdir -p $(DRAMSIM3_HOME)/build
	cd $(DRAMSIM3_HOME)/build && cmake -D COSIM=1 ..
	$(MAKE) -C $(DRAMSIM3_HOME)/build

###### difftest target ######
# if want to use the RamHelper, need to remove the 'WITH_DRAMSIM3=1'
difftestBuild:
	sed -i 's/io_memAXI_0_w_bits_data,/io_memAXI_0_w_bits_data[3:0],/g' $(BUILD_DIR)/SimTop.v
	sed -i 's/io_memAXI_0_r_bits_data,/io_memAXI_0_r_bits_data[3:0],/g' $(BUILD_DIR)/SimTop.v
	sed -i 's/io_memAXI_0_w_bits_data =/io_memAXI_0_w_bits_data[0] =/g' $(BUILD_DIR)/SimTop.v
	sed -i 's/ io_memAXI_0_r_bits_data;/ io_memAXI_0_r_bits_data[0];/g' $(BUILD_DIR)/SimTop.v
	$(MAKE) -C $(DIFFTEST_HOME) WITH_DRAMSIM3=1

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
# now only test the rtl-time and interrupt
amTestBuild:
	$(MAKE) -C $(AMTEST_HOME) ARCH=riscv64-mycpu mainargs=i

###### (am)coremark target ######
coremarkTestBuild:
	$(MAKE) -C $(COREMARK_HOME) ARCH=riscv64-mycpu

###### (am)dhrystone target ######
dhrystoneTestBuild:
	$(MAKE) -C $(DHRYSTONE_HOME) ARCH=riscv64-mycpu


###### (am)microbench target ######
microbenchTestBuild:
	$(MAKE) -C $(MICROBENCH_HOME) ARCH=riscv64-mycpu mainargs=test


###### (am)fcemux target ######
fecmuxTestBuild:
	$(MAKE) -C $(FCEMUX_HOME) ARCH=riscv64-mycpu mainargs=mario

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
	@echo -e "[\033[0;33mAll: $$(cat $(SIMPLETEST_HOME)/build/log/allcasenum-log.txt)  \033[0;32mPASS: $$(cat $(SIMPLETEST_HOME)/build/log/passcasenum-log.txt)  \033[0;31mFAIL: $$(echo $$(echo $$(cat $(SIMPLETEST_HOME)/build/log/allcasenum-log.txt) - $$(cat $(SIMPLETEST_HOME)/build/log/passcasenum-log.txt) | bc))\033[0m]";

$(simpleTestLogFile):
	$(shell touch $(SIMPLETEST_HOME)/build/log/$@)
	$(shell touch $(SIMPLETEST_HOME)/build/log/allcasenum-log.txt)
	$(shell touch $(SIMPLETEST_HOME)/build/log/passcasenum-log.txt)
	$(shell echo 0 > $(SIMPLETEST_HOME)/build/log/allcasenum-log.txt)
	$(shell echo 0 > $(SIMPLETEST_HOME)/build/log/passcasenum-log.txt)

$(simpleTestCaseName): simpletest-%: $(SIMPLETEST_HOME)/build/%-riscv64-mycpu.bin 
	@$(call getRecursiveTestRes, $(SIMPLETEST_HOME))


###### riscv test recursive test target ######
riscvTestbinFile   = $(foreach dir, $(RISCVTEST_HOME)/build, $(wildcard $(dir)/*.bin))
riscvTestCaseName  = $(foreach file, $(riscvTestbinFile), $(patsubst %-riscv64-mycpu, riscvtest-%, $(basename $(notdir $(file)))))
riscvTestLogFile   = $(foreach file, $(riscvTestCaseName), $(patsubst %, %-log.txt, $(file)))
$(shell mkdir -p $(RISCVTEST_HOME)/build/log 1>/dev/null 2>&1)
riscvRecursiveTest: $(riscvTestLogFile) $(riscvTestCaseName)
	@printf "[\033[0;33mall-done\033[0m]\n"
	@echo -e "[\033[0;33mAll: $$(cat $(RISCVTEST_HOME)/build/log/allcasenum-log.txt)  \033[0;32mPASS: $$(cat $(RISCVTEST_HOME)/build/log/passcasenum-log.txt)  \033[0;31mFAIL: $$(echo $$(echo $$(cat $(RISCVTEST_HOME)/build/log/allcasenum-log.txt) - $$(cat $(RISCVTEST_HOME)/build/log/passcasenum-log.txt) | bc))\033[0m]";

$(riscvTestLogFile):
	$(shell touch $(RISCVTEST_HOME)/build/log/$@)
	$(shell touch $(RISCVTEST_HOME)/build/log/allcasenum-log.txt)
	$(shell touch $(RISCVTEST_HOME)/build/log/passcasenum-log.txt)
	$(shell echo 0 > $(RISCVTEST_HOME)/build/log/allcasenum-log.txt)
	$(shell echo 0 > $(RISCVTEST_HOME)/build/log/passcasenum-log.txt)

$(riscvTestCaseName): riscvtest-%: $(RISCVTEST_HOME)/build/%-riscv64-mycpu.bin
	$(call getRecursiveTestRes, $(RISCVTEST_HOME))


###### cpu test recursive test target ######
cpuTestbinFile   = $(foreach dir, $(CPUTEST_HOME)/build, $(wildcard $(dir)/*.bin))
cpuTestCaseName  = $(foreach file, $(cpuTestbinFile), $(patsubst %-riscv64-mycpu, cputest-%, $(basename $(notdir $(file)))))
cpuTestLogFile   = $(foreach file, $(cpuTestCaseName), $(patsubst %, %-log.txt, $(file)))
$(shell mkdir -p $(CPUTEST_HOME)/build/log 1>/dev/null 2>&1)
cpuRecursiveTest: $(cpuTestLogFile) $(cpuTestCaseName)
	@printf "[\033[0;33mall-done\033[0m]\n"
	@echo -e "[\033[0;33mAll: $$(cat $(CPUTEST_HOME)/build/log/allcasenum-log.txt)  \033[0;32mPASS: $$(cat $(CPUTEST_HOME)/build/log/passcasenum-log.txt)  \033[0;31mFAIL: $$(echo $$(echo $$(cat $(CPUTEST_HOME)/build/log/allcasenum-log.txt) - $$(cat $(CPUTEST_HOME)/build/log/passcasenum-log.txt) | bc))\033[0m]";

$(cpuTestLogFile):
	$(shell touch $(CPUTEST_HOME)/build/log/$@)
	$(shell touch $(CPUTEST_HOME)/build/log/allcasenum-log.txt)
	$(shell touch $(CPUTEST_HOME)/build/log/passcasenum-log.txt)
	$(shell echo 0 > $(CPUTEST_HOME)/build/log/allcasenum-log.txt)
	$(shell echo 0 > $(CPUTEST_HOME)/build/log/passcasenum-log.txt)

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

.PHONY: millTest diffBuild help compile bsp reformat checkformat \
        nemuBuild difftestBuild riscvTestBuild cpuTestBuild amTestBuild demoTest recursiveTest \
		cleanBuild cleanMillOut cleanDepRepo cleanAll
