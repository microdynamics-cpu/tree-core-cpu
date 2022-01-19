# 一生一芯第三期项目报告

## 个人介绍

我是缪宇驰，学号是20210324，以前结合芯来的书了解过部分处理器相关内容，但没有实际编写代码实现设计过。西北工业大学航天学院精确制导与控制研究所在读研究生，将于2022年6月毕业。2018年本科毕业于西北工业大学航天学院探测制导与控制技术专业。现主要研究方向为微小卫星空间科学探测，星载计算机设计，小天体表面空间机器人运动规划和仿真。目前参与国家自然科学基金一项，发表国内论文一篇。曾获得研究生一等奖学金等。擅长FPGA板级电路设计开发和调试。热爱开源软硬件运动，业余时间从事开源工具类软件开发，[个人github地址](https://github.com/maksyuki)。

## 项目概述

- 项目地址: [tree-core-cpu](https://github.com/microdynamics-cpu/tree-core-cpu)
- 开发语言：chisel
- 许可证：GPL-3.0

TreeCoreL2是一个支持RV64I的单发射5级流水线的开源处理器核。支持axi4总线取指和访存，支持动态分支预测(BTB, PHT, GHR)，支持机器特权模式下的异常中断处理。能够在difftest和soc仿真环境下启动rt-thread。

## 微架构
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-arch.drawio.svg"/>
 <p align="center">
  TreeCoreL2 总体数据流图
 </p>
</p>


### IFU
使用Gshare
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-ifu.drawio.svg"/>
 <p align="center">
  取指单元主体部分
 </p>
</p>

由于目前TreeCore2的取指和访存没有使用cache，处理器核 所以使用
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-ipc.png"/>
 <p align="center">
  使用分支预测对性能的一点改进
 </p>
</p>

### IDU

### EXU

### MAU

### WBU

### AxiBridge


## 项目结构和参考
TreeCore的代码仓库结构借鉴了[riscv-sodor](https://github.com/ucb-bar/riscv-sodor)和[oscpu-framework](https://github.com/OSCPU/oscpu-framework)，并使用make作为项目构建工具，能够直接使用`make [target]`实现依赖软件的下载和配置，不同平台（difftest和soc）verilog文件生成和修改，回归测试等。

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-make.png"/>
 <p align="center">
  使用make自定义函数扩展
 </p>
</p>

另外TreeCore的实现和测试依赖于众多项目，其中包括：
- [chisel3](https://github.com/chipsalliance/chisel3)
- [verilator](https://github.com/verilator/verilator)
- [NEMU](https://gitee.com/oscpu/NEMU)
- [DRAMsim3](https://github.com/OpenXiangShan/DRAMsim3)
- [difftest](https://gitee.com/oscpu/difftest)
- [Abstract Machine](https://github.com/NJU-ProjectN/abstract-machine)
- [ysyxSoC](https://github.com/OSCPU/ysyxSoC)
- [riscv-tests](https://github.com/NJU-ProjectN/riscv-tests)


立即数扩展模块部分参考了[果壳处理器](https://github.com/OSCPU/NutShell)的实现方式


## 心得感想
调试的bug，和以往做过的不同，遇到的困难和迷茫，相比过去自己的成长，对一生一芯的期望和改进。开发日志。作为。

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-schedule.png"/>
 <p align="center">
  TreeCoreL2开发进度表
 </p>
</p>

在代码实现的过程中，将自己踩过的坑以及qq群各位同学提的问题记录了下来，总结成一个FAQ文档。目前该文档有近3.7万字，202张图片，共128页。之后会对相关问题进行索引，方便查找。

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-guide.png"/>
 <p align="center">
  总结的常见问题文档
 </p>
</p>

一点开发过程的思考，工具的设计

## 计划
目前开发的**TreeCoreL2**是TreeCore系列处理器核的第二个版本，目前基本达到设计目标，后续将会继续优化代码。而第三个版本(**TreeCoreL3**)和第四个版本(**TreeCoreL4**)将会追求更高的性能，也是规划中的参加一生一芯第四期和第五期的处理器。其中**TreeCoreL3**将在前代核的基础上，支持RV64IMAC指令，cache和mmu，并提高流水线级数，使其能够启动rt-thread，xv6和linux。**TreeCoreL4** 则会在前代的基础上实现浮点运算和多发射技术，进一步提高处理器性能。

对于TreeCoreL2来说：
 - 继续改进当前TreeCoreL2的微架构设计，能够使用更多chisel的特性来简化代码实现
 - 将处理器核移植到安路科技的fpga上


