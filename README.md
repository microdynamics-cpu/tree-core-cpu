<p align="center">
    <img width="200px" src="./.images/tree_core_logo.svg" align="center" alt="Tree Core CPU" />
    <h1 align="center">TreeCore CPU</h1>
    <p align="center">A series of RISCV soft core processors written from scratch</p>
</p>
<p align="center">
    <a href="./LICENSE">
        <img alt="license" src="https://img.shields.io/github/license/microdynamics-cpu/tree_core_cpu.svg" />
    </a>
    <img alt="stars" src="https://img.shields.io/github/stars/microdynamics-cpu/tree_core_cpu.svg" />
    <img alt="forks" src="https://img.shields.io/github/forks/microdynamics-cpu/tree_core_cpu.svg" />
    <img alt="version" src="https://img.shields.io/badge/version-1.0.0-FF69B4.svg" />
    <img alt="build" src="https://travis-ci.org/microdynamics-cpu/tree_core_cpu.svg?branch=main" />
</p>

<p align="center">
    <a href="./README.md">English</a>·
    <a href="./README_zh-CN.md">简体中文</a>
</p>

## Overview
The TreeCore processors are the riscv64 software core developed under the [Open Source Chip Project by University (OSCPU)](https://github.com/OSCPU). OSCPU was initiated by ICTCAS(**_Institute of computing Technology, Chinese Academy of Sciences_**), which aims to make students use all open-source toolchain to design, develop open-source chips by themselves. It also can be called "One Life, One Chip" project in Chinese which has achieved two season. Now Season 3 is in progress in 2021.

Now the TreeCore has two version, TreeCoreL1(**_TreeCore Learning Core 1_**) and TreeCoreL2(**_TreeCore Learning Core 2_**). The TreeCore project is aim to help students to develop a series of riscv processor by step-to-step materials, So not just for high performance. Not like textbooks exhibit the all the knowledges in one time. TreeCore start a very simple model. provide necessary new concepts or knowledge you need to learn.



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

### Getting Started
#### Enviroment Setup
> NOTE: All of the components are installed under linux operation system. To gurantee the compatibility and stability, I strongly recommend using `ubuntu 20.04 LTS`.

First, you need to install verilator, mill and dependency libraries:
```bash
$ make install
```
Then, download and configuare all components from the github:
```bash
make setup
```

IMG!!!!!!!

Becuase the change the sim memory from 8G to 256MB. need to enter 'make menuconfig' and modify [Memory - Configuration]->[Memory size] to '0x10000000' manually.

cd in root rtl dir
```bash
make nemuBuild
make dramsim3Build
make simpleTestBuild
make riscvTestBuild
make cpuTestBuild
make amTestBuild
```

### Software test

- #### Instruction test

- #### Program test

### Hardware test

- #### Hardware configuration

- #### Function test

## Summary

## Documention

## Plan

## Update

## License

## Story
I heard the word **_RISCV_** first time in the second semester of my junior year(that is, the summer of 2016). My roommate participated in the pilot class of "Computer Architecture" organized by the college, and **their task was to design a simple soft-core CPU based on the RISCV instruction set**. At that time, I only knew that it was an open source RISC instruction set launched by the University of Berkeley. I felt that it was similar to the MIPS, so I didn't take it too seriously. But what is unexpected is that after just a few period of development, the RISCV has been supported by many Internet and semiconductor giants around the world, and more and more research institutions, start-ups begin to design their own proprietary processors based on it. Although now the performance and application of RISCV are still limited, **I believe RISCV will usher in a revolution that can change the old pattern in someday**. 

The ancients once said: **it’s always shallow on paper, and you must do it yourself**. For the learn of the computer architecture, there is no better way to realize it from scratch. So I started to collect materials from the Internet, and I found the learning threshold and cost is very high. In addition, in order to pursue the performance, some open-source CPU cores are very complex(such as using mulit-pipelines, multi-core processing, out-of-order execution technology, etc), it is very difficult for beginners to get started. So I decided to design a series of open source processors from scratch, which has **simple, understandable architecture, high-quality code with step-to-step tutorial**. 

I hope it can become a ABC project like Arduino and make more processor enthusiasts or computer related specialized students enter into the computer architecture field. In the future, under the mutual promotion of the software and hardware ecosystem, I believe more people will like CPU development and be willing to spend time on it. 
