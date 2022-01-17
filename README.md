<p align="center">
    <img width="200px" src="./.images/tree_core_logo.svg" align="center" alt="Tree Core CPU" />
    <h2 align="center">TreeCore CPU: A series of riscv processors written from scratch</h2>
</p>
<p align="center">
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
The TreeCore processors are the riscv64 cores developed under the [Open Source Chip Project by University (OSCPU)](https://github.com/OSCPU). OSCPU was initiated by ICTCAS(**_Institute of computing Technology, Chinese Academy of Sciences_**), which aims to make students use all open-source toolchain to design, develop open-source chips by themselves. It also can be called "One Life, One Chip" project in Chinese which has achieved two season. Now Season 3 is in progress in 2021.

Now the TreeCore has two version, TreeCoreL1(**_TreeCore Learning 1_**) and TreeCoreL2(**_TreeCore Learning 2_**). The TreeCore project is aim to help students to develop a series of riscv processor by step-to-step materials, So not just for high performance. Not like textbooks exhibit the all the knowledges in one time. TreeCore start a very simple model. provide necessary new concepts or knowledge you need to learn.


## Motivation
I heard the word **_RISCV_** first time in the second semester of my junior year(that is, the summer of 2016). My roommate participated in the pilot class of "Computer Architecture" organized by the college, and **their task was to design a simple soft-core CPU based on the RISCV instruction set**. At that time, I only knew that it was an open source RISC instruction set launched by the University of Berkeley. I felt that it was similar to the MIPS, so I didn't take it too seriously. But what is unexpected is that after just a few period of development, the RISCV has been supported by many Internet and semiconductor giants around the world, and more and more research institutions, start-ups begin to design their own proprietary processors based on it. Although now the performance and application of RISCV are still limited, **I believe RISCV will usher in a revolution that can change the old pattern in someday**.

The ancients once said: **it’s always shallow on paper, and you must do it yourself**. For the learn of the computer architecture, there is no better way to realize it from scratch. So I started to collect materials from the Internet, and I found the learning threshold and cost is very high. In addition, in order to pursue the performance, some open-source CPU cores are very complex(such as using mulit-pipelines, multi-core processing, out-of-order execution technology, etc), it is very difficult for beginners to get started. So I decided to design a series of open source processors from scratch, which has **simple, understandable architecture, high-quality code with step-to-step tutorial**.

I hope it can become a ABC project like Arduino and make more processor enthusiasts or computer related specialized students enter into the computer architecture field. In the future, under the mutual promotion of the software and hardware ecosystem, I believe more people will like CPU development and be willing to spend time on it.

## Feature
TreeCoreL1(**under development**)
* 64-bits single period riscv core
* written by verilog

TreeCoreL2
* 64-bits single-issue, five-stage pipeline riscv core
* written by chisel3
* support RISCV integer(I) instruction set
* supports machine mode privilege levels
* supports AXI4 inst and mem acess
* supports dynamics branch prediction
* can boot rt-thread
* develop under all open-source toolchain

TreeCoreL3(**under development**)
* 64-bits five-stage pipeline riscv core
* written by chisel3
* support RV64IMAC instruction set
* supports machine mode privilege levels
* supports AXI4 inst and mem acess
* supports ICache, DCache(directed-map)
* can boot rt-thread, xv6 and linux
* develop under all open-source toolchain

## Develop Schedule
Now, the develop schedule is recorded by the **Tencent Document**. You can click this link [schedule table](https://docs.qq.com/sheet/DY3lORW5Pa3pLRFpT?newPad=1&newPadType=clone&tab=BB08J2) to view it.

## Datapath Diagram

### Memory Map

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

### Enviroment Setup
> NOTE: All of the components are installed under linux operation system. To gurantee the compatibility and stability, I strongly recommend using `ubuntu 20.04 LTS`.

First, you need to install verilator, mill and dependency libraries:
```bash
$ cd rtl
$ make install
```
Then, download and configuare all components from the github:
```bash
$ make setup
```
After that, you need to set the `NEMU_HOME` and `NOOP_HOME` environment variables:
```bash
$ NEMU_HOME=$(pwd)/dependency/NEMU
$ NOOP_HOME=$(pwd)/dependency
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

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/nemu-build-mem.png"/>
 <p align="center">
  <em>The memory address size menu</em>
 </p>
</p>

Last, remember to type `Save` button in bottom menu to save the `.config` file. Then, type `Exit` to exit the menuconfig.

### Compile runtime libraries
If you already run above steps correctly, you need to compile runtime libraries as follow:

```bash
$ make nemuBuild
$ make dramsim3Build
```

### Recursive test
When you modify the processor design, you
```bash 
$ make riscvTestBuild
$ make cpuTestBuild
```

### Software test
```bash
$ make amTestBuild
```

### SoC test

### Hardware test

- #### Hardware configuration

- #### Function test

## Summary

## Plan

## Update

## License
All of the TreeCore codes are release under the [GPL-3.0 License](LICENSE).

