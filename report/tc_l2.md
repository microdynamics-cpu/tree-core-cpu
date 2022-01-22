# 一生一芯第三期项目报告

## 个人介绍

我是缪宇驰，学号是20210324。西北工业大学航天学院精确制导与控制研究所在读研究生，将于2022年6月毕业。2018年本科毕业于西北工业大学航天学院探测制导与控制技术专业。现主要研究方向为微小卫星空间科学探测，星载计算机设计，小天体表面空间机器人运动规划和仿真。目前参与国家自然科学基金一项，发表国内论文一篇。曾获得研究生一等奖学金等。擅长FPGA板级电路设计开发和调试。热爱开源软硬件运动，业余时间从事开源工具类软件开发，[个人github地址](https://github.com/maksyuki)。

以前没有实际设计过处理器核，参加一生一芯三期算是我第一次完整实现一个处理器。

## 项目概述

- 项目地址: [tree-core-cpu](https://github.com/microdynamics-cpu/tree-core-cpu)
- 开发语言：chisel
- 许可证：GPL-3.0

TreeCoreL2是一个支持RV64I的单发射5级流水线的开源处理器核。支持axi4总线取指和访存，支持动态分支预测(BTB, PHT, GHR)，支持机器特权模式下的异常中断处理。能够在difftest和soc仿真环境下启动rt-thread。

## 微架构设计
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-arch.drawio.svg"/>
 <p align="center">
  TreeCoreL2 总体数据流图
 </p>
</p>

TreeCoreL2的微架构设计采用经典的5级流水线结构，取指和访存的请求通过crossbar进行汇总并转换成自定义的axi-like总线**data exchange(dxchg)**，最后通过转换桥将dxchg协议转换成axi4协议并进行仲裁。下面将着重介绍**取指**，**执行**，**访存**和**crossbar&axi4转换桥**四部分的具体实现。

### 取指单元
取指单元主要功能是计算出下一个周期的pc并向axi总线发送读请求。pc通过多路选择器按照优先级从高到低依次选取`mtvec`、`mepc`、`jump target`、 `branch predict target`和`pc + 4`的值。BPU采用基于全局历史的两级预测器。相关参数如下：
1. Global History Reister(GHR): bit width = 5
2. Pattern History Table(PHT):  size = 32
3. Branch Target Buffer(BTB):   line size = 64(pc) + 64(target) + 1(jump) size = 32

GHR每次从EXU得到分支是否taken的信息用于更新GHR移位寄存器的值，之后输出更新后值到PHT中并与当前pc求异或(**_gshare_**)。其结果作为PHT检索对应entry的地址，PHT每次从EXU得到分支执行后信息用于更新自己。BTB的每个Line记录一个1位的jump，64位的pc和64位的tgt值。1位的jump表示当前记录的指令是否是一个无条件跳转指令。虽然BTB中留有jump的标志，但是目前并不对无条件跳转指令进行预测。因为有些jump指令的target可能是不固定的，比如函数调用中的`ret`指令，会使得BTB以上一次保存的target进行预测，进而跳转到错误的地址。

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-ifu.drawio.svg"/>
 <p align="center">
  取指单元主体部分
 </p>
</p>

由于目前TreeCore2的取指和访存没有使用cache，处理器核需要大量时钟周期来等待axi的响应，所以采用动态分支预测技术后对ipc的提升较小。
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-ipc.png"/>
 <p align="center">
  使用分支预测对性能的一点改进
 </p>
</p>

### 执行单元
执行单元主要用于执行算术逻辑计算、计算分支指令的跳转地址。另外还设计了一个乘除法单元(MDU)和加速计算单元(ACU)用于对矩阵乘除法进行加速，但是由于个人进度的影响，没能按期调通cache，故没有将MDU，ACU集成到提交的版本中。最后执行单元中还实现了CSR寄存器，用于对环境调用异常和中断进行处理。其中`EXU.scala`中的83~92行代码为跳转控制逻辑处理：

```scala
  io.nxtPC.trap  := valid && (timeIntrEn || ecallEn)
  io.nxtPC.mtvec := csrReg.io.csrState.mtvec
  io.nxtPC.mret  := valid && (isa === instMRET)
  io.nxtPC.mepc  := csrReg.io.csrState.mepc
  // (pred, fact)--->(NT, T) or (T, NT)
  protected val predNTfactT = branch && !predTaken
  protected val predTfactNT = !branch && predTaken
  io.nxtPC.branch := valid && (predNTfactT || predTfactNT)
  io.nxtPC.tgt    := Mux(valid && predNTfactT, tgt, Mux(valid && predTfactNT, pc + 4.U, 0.U(XLen.W)))
  io.stall        := valid && (io.nxtPC.branch || timeIntrEn || ecallEn || (isa === instMRET))
```

### 访存单元
访存单元集成了LSU和CLINT，其中LSU负责生成访存所需的读写控制信号(size, wmask等)。CLINT则读入生成的控制信号，若访存的地址处于`0x0200_0000 - 0x0200_ffff`之间，则处理访存的信号，否则将控制信号透传出去。
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-mau.drawio.svg"/>
</p>

### Crossbar&Axi4转换桥
crossbar负责将取值和访存的请求进行合并，统一成一个自定义的axi-like总线**data exchange(dxchg)**，dxchg其实和axi-lite很接近。不过考虑之后扩展的需要，故自定义了一个。axi4转换桥将crossbar的dxchg总线接口转换成标准axi4总线，axi4采用单主机模式，主要通过crossbar中状态机的不同状态来区分一次axi请求是取值还是访存：

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-axi.drawio.svg"/>
 <p align="center">
  axi总线访存实现
 </p>
</p>

状态机有两个状态`eumInst`和`eumMem`，初始化后处于`eumInst`，当IFU发送取值请求并等到axi的响应后，表示一次取值请求完成，同时状态会转移到`eumMem`。状态切换到`eumMem`是因为对于一条指令来说，其只可能是访存指令，在MAU中发起读写axi请求；或是非访存指令，不发起请求。由于TreeCoreL2的微架构实现中不存在处理连续两个访存请求的情况，所以状态机在一次访存后一定会切换到取值状态。


<!-- 分别介绍在不同模式下的访存的信号安排。 -->

2. 示意图介绍下一条指令的控制冒险，bypass，等stall信号的实现？时序图那种，

## 项目结构和参考
TreeCore的代码仓库结构借鉴了[riscv-sodor](https://github.com/ucb-bar/riscv-sodor)和[oscpu-framework](https://github.com/OSCPU/oscpu-framework)组织代码的方式并使用make作为项目构建工具，同时Makefile里面添加了模板参数，可以支持多个不同处理器的独立开发，能够直接使用`make [target]`下载、配置相关依赖软件、生成、修改面向不同平台(difftest和soc)的verilog文件，执行回归测试等。

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-make.png"/>
 <p align="center">
  使用make自定义函数实现回归测试target
 </p>
</p>

1. 另外TreeCore的实现和测试依赖于众多项目，其中包括：
 - [chisel3](https://github.com/chipsalliance/chisel3)
 - [verilator](https://github.com/verilator/verilator)
 - [NEMU](https://gitee.com/oscpu/NEMU)
 - [DRAMsim3](https://github.com/OpenXiangShan/DRAMsim3)
 - [difftest](https://gitee.com/oscpu/difftest)
 - [Abstract Machine](https://github.com/NJU-ProjectN/abstract-machine)
 - [ysyxSoC](https://github.com/OSCPU/ysyxSoC)
 - [riscv-tests](https://github.com/NJU-ProjectN/riscv-tests)
2. 立即数扩展模块部分参考了[果壳处理器](https://github.com/OSCPU/NutShell)的实现方式
3. 流水线结构和各功能单元安排部分参考了[蜂鸟E203](https://github.com/riscv-mcu/e203_hbirdv2)

## 总结

### 心得感想
首先，要衷心地感谢一生一芯三期项目的所有老师，助教同学们一直以来的辛苦付出。去年自己有幸赶到上科大参加了RISCV中国峰会，香山处理器的系列报告让我大饱眼福。当听说新一期一生一芯项目准备面向全国高校学生开放后，作为一名研三临近毕业的学生，深感这次机会的来之不易，便毫不犹豫地报了名。在实际编码调试过程中让我重新学习了很多知识，比如内存地址对齐问题。我记得我第一次听说“地址对齐”这个名词还是13年我大一学c语言的时候，当时老师在讲解union类型时引出了这个概念。但是当时对这个概念没有深入学习下去，这导致我在刚开始调试axi仲裁的时候一直没搞对地址的掩码计算，花了很长时间。另外参加一生一芯三期对于我来说也是个不小的挑战，因为它要求独立开发，要在很短的时间内学习很多新知识，使用很多新工具，而这些是我以前做过的课程实验所没有的。在具体开发过程中，由于本人跨专业的原因，体系结构相关知识比较薄弱，所以很多内容都要从零开始学起。另外我还要兼顾科研任务，毕设实验和找工作等多项事情，时间很紧张，有时很长时间没法调试出一个bug也会让我感到沮丧和迷茫。但是相比于参加之前，自己也确实收获了实实在在的成长。通过参加一生一芯三期，我完整地实现了一个处理器核，虽然还不太完美。学习了chisel，verilator，difftest等众多开源处理器开发工具及其背后的敏捷开发思想，也加深了对软硬件之间工作原理的认知。当时的[进度表](https://docs.qq.com/sheet/DY3lORW5Pa3pLRFpT?newPad=1&newPadType=clone&tab=BB08J2)也记录下了自己开发调试过程中的点点滴滴。那种不停google->查书->编码->调试后bug被解决的喜悦让我终生难忘。

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-schedule.png"/>
 <p align="center">
  TreeCoreL2开发进度表
 </p>
</p>

### 文档资料整理
另外，在自己观看学习视频，编写、调试代码的过程中，为了方便自己复习、消化相关知识，我将自己平时曾踩过的坑以及qq群各位同学的问题记录了下来，并配以相关解答，总结成了一个FAQ文档。目前该文档有近3.7万字，202张图片，共126页。之后有时间将继续对文档中的相关内容进行补充，修改和更新。

<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-guide.png"/>
 <p align="center">
  总结的常见问题文档
 </p>
</p>

### 一点开发过程中的想法: 波形与回归测试联合调试工具的设计
difftest进行差分测试可以快速定位到出错的指令，却无法像波形那样直观地展现多周期完整的信号变化。考虑设计一个工具，当difftest对比到出错的指令时能够触发事件，而这个事件传递到波形组件后可以直接定位到对应时钟周期并显示临近的波形，以方便调试。后期的话考虑直接对波形进行解析，就像嵌入式领域的逻辑分析仪一样，能够直接将诸如操作数，stall等信息标注到波形上。

## 计划
目前开发的**TreeCoreL2**是TreeCore系列处理器核的第二个版本，目前基本达到设计目标，后续将会继续优化代码。而第三个版本(**TreeCoreL3**)和第四个版本(**TreeCoreL4**)将会追求更高的性能，也是规划中的参加一生一芯第四期和第五期的处理器。其中**TreeCoreL3**将在前代核的基础上，支持RV64IMAC指令，cache和mmu，并提高流水线级数，使其能够启动rt-thread，xv6和linux。**TreeCoreL4** 则会在**TreeCoreL3**的基础上实现浮点运算和多发射技术，进一步提高处理器性能。

对于TreeCoreL2来说：
 - 继续改进当前TreeCoreL2的微架构设计，能够使用更多chisel的特性来简化代码实现
 - 将处理器核移植到安路科技的fpga上


