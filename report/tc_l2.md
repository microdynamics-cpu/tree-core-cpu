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

### IDU

### EXU

### MAU

### WBU

### debug
<p align="center">
 <img src="https://raw.githubusercontent.com/microdynamics-cpu/tree-core-cpu-res/main/treecore-l2-ipc.png"/>
 <p align="center">
  使用分支预测对性能的一点改进
 </p>
</p>


## 依赖和参考

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

road map，

## 致谢
