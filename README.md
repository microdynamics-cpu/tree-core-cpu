<p align="center">
    <img width="200px" src="./.images/tree_core_logo.svg" align="center" alt="Tree Core CPU" />
    <h2 align="center">TreeCore CPU: A Series of RISCV Processors Written from Scratch</h2>
</p>
<p align="center">
   <a href="https://github.com/microdynamics-cpu/tree-core-cpu/actions">
    <img src="https://img.shields.io/github/workflow/status/microdynamics-cpu/tree-core-cpu/unit-test/main?label=unit-test&logo=github&style=flat-square">
    </a>
    <a href="./LICENSE">
      <img src="https://img.shields.io/github/license/microdynamics-cpu/tree-core-cpu?color=brightgreen&logo=github&style=flat-square">
    </a>
    <a href="https://github.com/microdynamics-cpu/tree-core-cpu">
      <img alt="stars" src="https://img.shields.io/github/stars/microdynamics-cpu/tree-core-cpu?color=blue&style=flat-square" />
    </a>
    <a href="https://github.com/microdynamics-cpu/tree-core-cpu">
      <img src="https://img.shields.io/badge/total%20lines-7k-red?style=flat-square">
    </a>
    <a href="https://github.com/OSCPU">
      <img src="https://img.shields.io/badge/sim%20framework-verilator%20NEMU%20difftest-red?style=flat-square">
  </a>
    <a href="./CONTRIBUTING.md">
      <img src="https://img.shields.io/badge/contribution-welcome-brightgreen?style=flat-square">
    </a>
</p>


## Overview
The TreeCore processors are the riscv cores developed under the [Open Source Chip Project by University (OSCPU)](https://github.com/OSCPU) project. OSCPU was initiated by ICT, CAS(**_Institute of computing Technology, Chinese Academy of Sciences_**), which aims to make students use all open-source toolchains to design chips by themselves. Students enroll in this project need to pass tests, submit final design report and prepare oral defense for the qualification of tape-out. It also can be called "One Life, One Chip" project in Chinese which has carried out three season:
### Season 1[**2021.8-2021.12**]: Five undergraduates design a tape-outed riscv processor in four months
Season 1 was a first educational practice which aimed to design riscv processor by five undergraduates for tape-out in China. And its achievement was [NutShell](https://github.com/OSCPU/NutShell), [a Linux-Compatible RISC-V Processor Designed by Undergraduates](https://www.youtube.com/watch?v=8K97ahPecqE). Five students are all from UCAS(**_University of Chinese Academy of Sciences_**).

### Season 2[**2020.8-2021.x**]: Eleven undergraduates design their own tape-outed processors
Unlike Season 1, Season 2 had eleven undergraduates from five universities to design processors, and it is the first attempt to promote this project to the other university.

### Season 3[**2021.7-2022.1**]: More students(One hundred students), More open source tools(NEMU, difftest, AM...)
TreeCoreL1<sup>[[1]](#id_tcl1)</sup> and TreeCoreL2<sup>[[2]](#id_tcl2)</sup> are the achievement of this season. After about six months of development, TreeCoreL2 obtained the qualification of tape-out in second shuttle. You can visit the official website [ysyx.org](https://ysyx.org/) to get more information.
> NOTE: The PCB card with TreeCoreL2 possible return in the second quarter of 2022, so on board debugging cannot release now.

### Season 4[**2022.2.20-2022.10.28, in progress**]: More open source IPs(SDRAM, VGA...), Smoother learning curve(bbs, tutorials, lecture, ...)
TreeCoreL3<sup>[[3]](#id_tcl3)</sup> will be the expected achievement of this season. TreeCoreL3 is a 64-bits single-issue, five-stage pipeline riscv core with cache written in verilog. Different from the TreeCoreL2, the all softare runtimes to support TreeCoreL3 is implemented by myself.

### Season 5[**2022.8.28-2023.2.10, in progress**]: Provide living broadcast course and development flow forzen
TreeCoreL4<sup>[[4]](#id_tcl4)</sup> will be the expected achievement of this season. TreeCoreL4 is a 64-bits two-issue, six-stage pipeline riscv core with cache written in chisel3.

Now the TreeCore has two version: TreeCoreL1(**_TreeCore Learning 1_**) and TreeCoreL2(**_TreeCore Learning 2_**). The TreeCore project aims to help students to learn how to write riscv processors by themselves with **step-to-step materials**. Not like textbooks only exhibit all of concepts in one time, the learn process of TreeCore is incremental. That means TreeCore only provides a very simple model with necessary new knowledges you need to learn first, then add extra codes to perfect the whole design every time until it is finished.


## Story and Motivation
I heard the word '**_riscv_**' first time in sophomore year(2016). At that time, my roommate participated in the pilot class of **_Computer Architecture_**, and their final assignment was to **design a simple riscv processor**. In fact, I only knew it was an open source RISC ISA launched by the UC, Berkeley. What is unexpected to me is that just after a few period of time, the riscv has been supported by many semiconductor giants and research institutions and **more and more people believe riscv will usher in a revolution that can change the old pattern in someday**.

I've always thought the best way to learn is to practice myself. When searching online, I found the learning threshold of processor is high. In addition, in order to pursue high performance, some open-source riscv cores are very complex(such as using dynamics branch prediction, multi-core processing, out-of-order execution technology, etc), these are very difficult for beginners to learn. In meanwhile, I learned that "One Life, One Chip" project with many ailge hardware developement tools. So why not design and implement processors with these new tools from scratch? The result of that desire is this project.

I hope it can become a ABC project like Arduino to make more processor enthusiasts and computer related specialized students enter into the computer architecture field more easily.

## Feature
IMG!!!!!!!!!!!!!!!! to intro three type processor and timeline.

**intro** the plan with the such as the target every type core need to meet. and timeline

## Usage
This section introduces how to set up development environment and runs unit test for your own riscv processor. Project directory is:
```bash
env    ->
         | hello_world_tb.gtkw # gtkwave wave config
         | hello_world_tb.sh   # compile script
         | hello_world_tb.v    # hello world verilog module
fpga   ->
         | bare_metal/         # bare metal verilog module for fpga
report ->
         | tc_l2.md            # treecore l2 wiki
rtl    ->
         | TreeCoreL1
         | TreeCoreL2
tests  ->
         | compile_rtl.py      # bare metal module compile script
         | compliance_test.py  # isa compliance test
         | run_all_isa_test.py # run all isa test
tools  ->
         | bin2mem.py          # convert bin file to mem file
         | bin2mif.py          # convert bin file to mif file
```

## TreeCoreL1<span id="id_tcl1"></span>
* 64-bits FSM
* written by verilog

In fact, TreeCoreL1 is not a processor, it is a bundle of some independent verilator programs and common chisel modules writing for learning. 

## TreeCoreL2<span id="id_tcl2"></span>
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-arch.drawio.svg"/>
 <p align="center">
  TreeCoreL2 data flow graph
 </p>
</p>

* 64-bits single-issue, five-stage pipeline riscv core
* written by chisel3
* support RISCV integer(I) instruction set
* supports machine mode privilege levels
* supports AXI4 inst and mem acess
* supports dynamics branch prediction
* can boot rt-thread
* develop under all open-source toolchain
asdafafaadsfsafa
IMG!!!!!!!!!!!!!!!


### Develop Schedule
Now, the develop schedule of TreeCore is recorded by the **Tencent Document**. You can click below link to view it:

1. TreeCoreL1&2(**frozen**): [link](https://docs.qq.com/sheet/DY3lORW5Pa3pLRFpT?newPad=1&newPadType=clone&tab=BB08J2)

### Memory Map
To compatible with ysyx3 SoC test, TreeCoreL2 have below memory map range:

| Range                     | Description                                         |
| ------------------------- | --------------------------------------------------- |
| 0x0000_0000 - 0x01ff_ffff | reserve                                             |
| 0x0200_0000 - 0x0200_ffff | clint                                               |
| 0x0201_0000 - 0x0fff_ffff | reserve                                             |
| 0x1000_0000 - 0x1000_0fff | uart16550                                           |
| 0x1000_1000 - 0x1000_1fff | spi controller                                      |
| 0x1000_2000 - 0x2fff_ffff | reserve                                             |
| 0x3000_0000 - 0x3fff_ffff | spi flash xip mode                                  |
| 0x4000_0000 - 0x7fff_ffff | chiplink                                            |
| 0x8000_0000 - 0x8xxx_xxxx | mem                                                 |

#### Configuration

### Enviroment Setup
> NOTE: All of the components and tools are installed under linux operation system. To gurantee the compatibility and stability, I **STRONGLY** recommend using `ubuntu 20.04 LTS`. `ubuntu 18.04` and `ubuntu 16.04` is not supported official.

If you're new to TreeCore project, we suggest you start with the install section. Remeber you **ONLY** need to install the below libraries once. Now all of operations(config, compile, test) have been automated by Makefile. You can visit [unit-test.yml](.github/workflows/unit-test.yml) to get more information.
> NOTE: In order to download and configure all libraries successful, you **NEED** to be able to visit github.com and gitee.com.

First, you need to install verilator, mill, difftest, abstract-machine and other dependency libraries:
```bash
$ git clone https://github.com/microdynamics-cpu/tree-core-cpu.git
$ cd tree-core-cpu/rtl
$ chmod +x scripts/install.sh
$ make install
```
Then, download and configuare all components from the github and gitee:
```bash
$ chmod +x scripts/setup.sh
$ make setup
```
After that, you need to add the `NEMU_HOME` and `NOOP_HOME` environment variables to your shell environment config file:
```bash
$ echo export NEMU_HOME=$(pwd)/dependency/NEMU >> ~/.bashrc # according to shell type your system uses
$ echo export NOOP_HOME=$(pwd)/dependency >> ~/.bashrc
$ source ~/.bashrc
```

Running the ISA test don't need 8G memory, so you can reconfigure the `memory size` to reduce the simulation memory usage. Achieving that, you need to type  `make menuconfig` as follow:

```bash
$ cd dependency/NEMU
$ make menuconfig
```
> NOTE: if you encounter `Your display is too small to run Menuconfig!` error, you need to resize the terminal to match need as the console output: `It must be at least 19 lines by 80 columns`.

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/nemu-build.png"/>
 <p align="center">
  <em>The main configuration menu</em>
 </p>
</p>

Usually, 256MB memory address space is enough for simulation. You need to switch into `[Memory - Configuration]` menu and change `[Memory size]` value into `0x10000000` manually as follow picture shows. It can adjust difftest's simulation memory size from 8G to 256MB.

> NOTE: In fact, the `Memory size` has been modified to `0x10000000` in `make setup` phase. Now, you only need to confirm it once more time.
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/nemu-build-mem.png"/>
 <p align="center">
  <em>The memory address size menu</em>
 </p>
</p>

Last, remember to type `Save` button in bottom menu to save the `.config` file. Then, type `Exit` to exit the menuconfig.

### Compile runtime libraries
If you already run above commands correctly, you can compile runtime libraries as follow:

```bash
$ cd ../../
$ make nemuBuild
$ make dramsim3Build
```

### Compile testcases
Two type of ISA testcases set are used: `riscv test` and `cpu test`.
```bash
$ make riscvTestBuild
$ make cpuTestBuild
$ make amTestBuild
```
> NOTE: you need enough memory to compile the application binaries. Generally speaking, you need at least 4GB of memory.

### Recursive test
After you modify the processor design, you need to run recursive unit test to gurantee the modification is correct.

```bash
$ make CHIP_TARGET=tc_l2 unit-test
```

The unit tests display the progress, testcase name, PASS or FAIL and ipc value.
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/isa-unit-test.png"/>
 <p align="center">
  <em>TreeCoreL2's unit test result</em>
 </p>
</p>

Running unit test need to download `mill` from github.com. If you cannot access the github correctly, you need to type below commands to configure `mill` manually:

```bash
$ # download '0.9.9-assembly' from https://github.com/com-lihaoyi/mill/releases/download/0.9.9/0.9.9-assembly manually.
$ cp 0.9.9-assembly ~/.cache/mill/download
$ mv ~/.cache/mill/download/0.9.9-assembly ~/.cache/mill/download/0.9.9 # change name
$ chmod +x ~/.cache/mill/download/0.9.9
```

### Software test
Software test, also called application test, can provide integrated test for interrupt. You need to recompile the amtest with specific `AM_TARGET` when you want to change the software target.
```bash
# the 'AM_TARGET' option value(default h):
# h => "hello"
# H => "display this help message"
# i => "interrupt/yield test"
# d => "scan devices"
# m => "multiprocessor test"
# t => "real-time clock test"
# k => "readkey test"
# v => "display test"
# a => "audio test"
# p => "x86 virtual memory test"
$ make amTestBuild AM_TARGET=i
$ make amTest
```

### Benchmark test
First, you need to compile the benchmark programs.
```bash
$ make coremarkTestBuild
$ make dhrystoneTestBuild
$ make microbenchTestBuild
```
```bash
$ make coremakrTest
$ make dhrystoneTest
$ make microbenchTest
```

### SoC test
SoC test is based on ysyxSoC project. SoC test provides more accurate simulation environment for processor design.

```bash
$ make CHIP_TARGET=tc_l2 socBuild
# SOC_APP_TYPE: flash, loader
# SOC_APP_NAME: hello, memtest, rtthread
$ make CHIP_TARGET=tc_l2 SOC_APP_TYPE=flash SOC_APP_NAME=hello socTest
```
### Add and Customize new project
```bash
# First modify the `CHIP_TARGET` in Makefile to your custom name which create folder.
$ make template
```

## TreeCoreL3(_under development_)<span id="id_tcl3"></span>


## TreeCoreL4(_under development_)<span id="id_tcl4"></span>
* 64-bits five-stage pipeline riscv core

## Plan

## Update

## License
TreeCore CPU's codes are release under the [GPL-3.0 License](./LICENSE) and compliance with other open source agreements. You can find all 3rd party libraries licenses in [3RD_PARTY.md](./3RD_PARTY.md).

## Acknowledgement
1. [oscpu-framework](https://github.com/OSCPU/oscpu-framework)
2. [NutShell](https://github.com/OSCPU/NutShell)

## Reference

