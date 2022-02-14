<p align="center">
    <img width="200px" src="./.images/tree_core_logo.svg" align="center" alt="Tree Core CPU" />
    <h2 align="center">TreeCore CPU: A Series of RISCV Processors Written from Scratch</h2>
</p>
<p align="center">
   <a href="https://github.com/microdynamics-cpu/tree-core-cpu/actions">
    <img src="https://img.shields.io/github/workflow/status/microdynamics-cpu/tree-core-cpu/unit-test/main?label=unit-test&logo=github&style=flat-square">
    </a>
    <a href="./LICENSE">
      <img src="https://img.shields.io/github/license/microdynamics-cpu/tree_core_cpu?color=brightgreen&logo=github&style=flat-square">
    </a>
    <a href="https://github.com/microdynamics-cpu/tree-core-cpu">
      <img alt="stars" src="https://img.shields.io/github/stars/microdynamics-cpu/tree_core_cpu?color=blue&style=flat-square" />
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
TreeCore project is the achievement of this season. Season 3 now is completed, and the official website is [ysyx.org](https://ysyx.org/).

Now the TreeCore has two version: TreeCoreL1(**_TreeCore Learning 1_**) and TreeCoreL2(**_TreeCore Learning 2_**). The TreeCore project aims to help students to learn how to write riscv processors by themselves with **step-to-step materials**. Not like textbooks only exhibit all of concepts in one time, the learn process of TreeCore is incremental. That means TreeCore only provides a very simple model with necessary new knowledges you need to learn first, then add extra codes to modify the whole design.

> NOTE: now the TreeCoreL2 is under tape-out phase. The chip debug and test introduction will release soon.

## Motivation
I heard the word '**_riscv_**' first time in sophomore year(that is, the summer of 2016). My roommate participated in the pilot class of **_Computer Architecture_**, and their final assignment was to **design a simple soft-core riscv processor**. At that time, I only knew it was an open source RISC ISA launched by the UC, Berkeley. What is unexpected to me is that just after a few period of time, the riscv has been supported by many semiconductor giants and research institutions. Although the performance of riscv are still limited now, **I believe riscv will usher in a revolution that can change the old pattern in someday**.

The best way to learn the processor design is to implement it from scratch. When I searched online and found the learning threshold and cost is very high. In addition, in order to pursue high performance, some open-source riscv cores are very complex(such as using dynamics branch prediction, multi-core processing, out-of-order execution technology, etc), these are very difficult for beginners to learn. So I decided to design a series of open source processors from scratch, which has **simple, understandable architecture, high-quality code with step-to-step tutorial**.

I hope it can become a ABC project like Arduino to make more processor enthusiasts and computer related specialized students enter into the computer architecture field. In the future, under the mutual promotion of the software and hardware ecosystem, I believe more people will like processor design and be willing to spend time on it.

## Feature
IMG!!!!!!!!!!!!!!!! to intro three type processor and timeline.

**intro** the plan with the such as the target every type core need to meet. and timeline

**TreeCoreL1**
* 64-bits FSM
* written by chisel3

In fact, TreeCoreL1 is not just a processor, it only supplies the basic implement of Turing machine model: 'loop + '.
IMG!!!!

**TreeCoreL2**
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


**TreeCoreL3(_under development_)**

**TreeCoreL4(_under development_)**
* 64-bits five-stage pipeline riscv core



## Develop Schedule
Now, the develop schedule is recorded by the **Tencent Document**. You can click this link [schedule table](https://docs.qq.com/sheet/DY3lORW5Pa3pLRFpT?newPad=1&newPadType=clone&tab=BB08J2) to view it.

### Memory Map
To compatible with SoC test, All types of TreeCore have same memory map range:

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

## Usage
This section introduces how to set up development environment and runs unit test for your own riscv processor.
### Enviroment Setup
> NOTE: All of the components and tools are installed under linux operation system. To gurantee the compatibility and stability, I strongly recommend using `ubuntu 20.04 LTS`. `ubuntu 18.04` and `ubuntu 16.04` is not supported official.

First, you need to install verilator, mill and dependency libraries:
```bash
$ cd rtl
$ chmod +x scripts/install.sh
$ make install
```
Then, download and configuare all components from the github and gitee:
```bash
$ chmod +x scripts/setup.sh
$ make setup
```
After that, you need to add the `NEMU_HOME` and `NOOP_HOME` environment variables in sh environment config file:
```bash
$ echo export NEMU_HOME=$(pwd)/dependency/NEMU >> ~/.bashrc # according to shell type your system uses
$ echo export NOOP_HOME=$(pwd)/dependency >> ~/.bashrc
$ source ~/.bashrc
```

Becuase running the isa test don't need 8G memory, so you need to config the simulation memory size to reduce memory usage. You need to type  `make menuconfig` as follow:

```bash
$ cd dependency/NEMU
$ make menuconfig
```
> NOTE: if you encount `Your display is too small to run Menuconfig!` error, you need to resize the terminal to match need as the console output: `It must be at least 19 lines by 80 columns`.

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
If you already run above commands correctly, you need to compile runtime libraries as follow:

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
> NOTE: you need to enough memory to compile the application binaries.

### Recursive test
After you modify the processor design, you need to run recursive unit test to gurantee the modification is correct.

```bash
$ make unit-tests
```

First, Running unit test need to download `mill` from github. If you cannot access the github correctly, you need to type below commands to configure `mill` manually:

```bash
$ # download '0.9.9-assembly' from https://github.com/com-lihaoyi/mill/releases/download/0.9.9/0.9.9-assembly manually.
$ cp 0.9.9-assembly ~/.cache/mill/download
$ mv ~/.cache/mill/download/0.9.9-assembly ~/.cache/mill/download/0.9.9 # change name
$ chmod +x ~/.cache/mill/download/0.9.9
```

IMG!!!!!!!!!

### Software test
```bash
$ make 
```

### SoC test
```bash
$ make socBuild
$ make socTest
```
### Customize new core project

## Summary

## Plan

## Update

## License
All of the TreeCore codes are release under the [GPL-3.0 License](LICENSE).

## Acknowledgement


## Reference

