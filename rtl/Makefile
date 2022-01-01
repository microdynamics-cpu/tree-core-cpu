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
YSYXSOC_HOME     := $(ROOT_PATH)/ysyxSoC/ysyx

###### soc var ######
SOC_CSRC_HOME     += $(ROOT_PATH)/src/main/csrc
SOC_CSRC_LIB_HOME += $(ROOT_PATH)/ysyxSoC/ysyx/peripheral/spiFlash
SOC_CXXFILES      += $(shell find $(SOC_CSRC_HOME) -name "*.cpp")
SOC_CXXFILES      += $(shell find $(SOC_CSRC_LIB_HOME) -name "*.cpp")

SOC_VSRC_HOME     += $(BUILD_DIR)/soc
SOC_COMPILE_HOME  := $(SOC_VSRC_HOME)/emu-compile
SOC_VSRC_TOP      := ysyxSoCFull
SOC_VSRC_LIB_HOME += $(ROOT_PATH)/ysyxSoC/ysyx/peripheral/
SOC_VXXFILES      += $(shell find $(SOC_VSRC_HOME) -name "*.v")
SOC_VXXFILES      += $(shell find $(SOC_VSRC_LIB_HOME) -name "*.v")

SOC_VSRC_INCLPATH += -I$(SOC_VSRC_HOME)
SOC_VSRC_INCLPATH += -I$(ROOT_PATH)/ysyxSoC/ysyx/peripheral/uart16550/rtl
SOC_VSRC_INCLPATH += -I$(ROOT_PATH)/ysyxSoC/ysyx/peripheral/spi/rtl
SOC_CSRC_INCLPATH += -I$(SOC_CSRC_HOME)
SOC_CSRC_INCLPATH += -I$(SOC_CSRC_LIB_HOME)

# if want to ouput vcd wave, replace '-DDUMP_WAVE_FST' to '-DDUMP_WAVE_VCD',
# replace '--trace-fst' to '--trace'
SOC_CXXFLAGS += -std=c++11 -static -Wall $(SOC_CSRC_INCLPATH) -DDUMP_WAVE_FST
SOC_FLAGS    += --cc --exe --top-module $(SOC_VSRC_TOP) 
SOC_FLAGS    += --x-assign unique -O3 -CFLAGS "$(SOC_CXXFLAGS)"
SOC_FLAGS    += --trace-fst --assert --stats-vars --output-split 30000 --output-split-cfuncs 30000 
SOC_FLAGS    += --timescale "1ns/1ns" -Wno-fatal
SOC_FLAGS    += -o $(BUILD_DIR)/soc/emu
SOC_FLAGS    += -Mdir $(BUILD_DIR)/soc/emu-compile
SOC_FLAGS    += $(SOC_VSRC_INCLPATH) $(SOC_CXXFILES) $(SOC_VXXFILES)

CCACHE := $(if $(shell which ccache),ccache,)
ifneq ($(CCACHE),)
export OBJCACHE = ccache
endif


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

chiselBuild:
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
# becuase the framework, now the 'memAXI_0_[r|w]_bits_data' need to be replaced
# by 'memAXI_0_w_bits_data[3:0]' in Makefile
difftestBuild:
	@sed -i 's/io_memAXI_0_\([a-z]*\)_bits_data,/io_memAXI_0_\1_bits_data[3:0],/g' $(BUILD_DIR)/SimTop.v
	@sed -i 's/io_memAXI_0_w_bits_data =/io_memAXI_0_w_bits_data[0] =/g' $(BUILD_DIR)/SimTop.v
	@sed -i 's/ io_memAXI_0_r_bits_data;/ io_memAXI_0_r_bits_data[0];/g' $(BUILD_DIR)/SimTop.v
	$(MAKE) -C $(DIFFTEST_HOME) WITH_DRAMSIM3=1 EMU_TRACE=1

diffAllBuild: chiselBuild difftestBuild

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

###### soc name rule test target ######
socTopModify:
	@mkdir -p $(BUILD_DIR)/soc
	@cp $(BUILD_DIR)/SoCTop.v $(BUILD_DIR)/soc/ysyx_210324.v
	@sed -i 's/module ysyx_210324_SoCTop/module ysyx_210324/g' $(BUILD_DIR)/soc/ysyx_210324.v
	@sed -i 's/io_\([a-z]*\)_\([a-z]*\)_[bits]*_*\([a-z]*\)/io_\1_\2\3/g' $(BUILD_DIR)/soc/ysyx_210324.v

# FIMXE: need a better solution, not just copy to dir everytime
socNameCheck: socTopModify
	@cp $(YSYXSOC_HOME)/soc/cpu-check.py $(BUILD_DIR)/soc
	@cd $(BUILD_DIR)/soc && echo 324 | python3 cpu-check.py

socLintCheck: socNameCheck
	@cp $(BUILD_DIR)/soc/ysyx_210324.v $(YSYXSOC_HOME)/lint/
	@sed -i 's/ID = \([0-9]*\)/ID = 210324/g' $(YSYXSOC_HOME)/lint/Makefile
	@echo -e "\033[1;32mstart lint check....\033[0m"
	$(MAKE) -C $(YSYXSOC_HOME)/lint/ lint
	@echo -e "\033[1;32mlint check done\033[0m"
	@echo -e "\033[1;32mstart lint-unused check....\033[0m"
	$(MAKE) -C $(YSYXSOC_HOME)/lint/ lint-unused
	@echo -e "\033[1;32mlint-unused check done\033[0m"

# socPrevBuild: diffAllBuild socTopModify
socPrevBuild:
# FIXME: if only need to moidfy core, commit it
# @cp $(YSYXSOC_HOME)/soc/ysyxSoCFull.v $(BUILD_DIR)/soc
	@sed -i s/ysyx_000000/ysyx_210324/g $(BUILD_DIR)/soc/ysyxSoCFull.v
	verilator $(SOC_FLAGS)

# socBuild: socPrevBuild
# 	$(MAKE) VM_PARALLEL_BUILDS=1 OPT_FAST="-O3" -C $(SOC_COMPILE_HOME) -f V$(SOC_VSRC_TOP).mk -j2

socBuild:
	@cp $(BUILD_DIR)/soc/ysyx_210324.v ../../oscpu/projects/soc/vsrc/

socRun:
# $(SOC_VSRC_HOME)/emu -i $(YSYXSOC_HOME)/program/bin/flash/hello-flash.bin
# $(SOC_VSRC_HOME)/emu -i $(YSYXSOC_HOME)/program/bin/flash/memtest-flash.bin
# $(SOC_VSRC_HOME)/emu -i $(YSYXSOC_HOME)/program/bin/flash/rtthread-flash.bin
# $(SOC_VSRC_HOME)/emu -i $(YSYXSOC_HOME)/program/bin/loader/hello-loader.bin
# $(SOC_VSRC_HOME)/emu -i $(YSYXSOC_HOME)/program/bin/loader/memtest-loader.bin
# $(SOC_VSRC_HOME)/emu -i $(YSYXSOC_HOME)/program/bin/loader/rtthread-loader.bin
###### clean target ######
cleanBuild:
	rm -rf $(BUILD_DIR)

cleanMillOut:
	rm -rf $(MILL_OUT_DIR)

cleanDepRepo:
	rm -rf $(AM_FOLDER_PATH) $(NEMU_HOME) $(DIFFTEST_HOME)

cleanAll: cleanBuild cleanMillOut cleanDepRepo


.PHONY: millTest chiselBuild help compile bsp reformat checkformat \
		nemuBuild difftestBuild riscvTestBuild cpuTestBuild amTestBuild demoTest \
		socTopModify socNameCheck socBuild\
		cleanBuild cleanMillOut cleanDepRepo cleanAll
