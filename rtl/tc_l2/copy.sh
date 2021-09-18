#!/bin/bash

# cp -r `ls | grep -v copy.sh | xargs` ../../../oscpu-dev-record/cpu
cp -r src ../../../oscpu-dev-record/cpu
# cp -r test ../../../oscpu-dev-record/cpu
cp -r am/simple-tests ../../../ysyx-software-file
# am-kernels
cp -r am/am-kernels/benchmarks ../../../ysyx-software-file/am-kernels
cp -r am/am-kernels/kernels ../../../ysyx-software-file/am-kernels
cp -r am/am-kernels/LICENSE ../../../ysyx-software-file/am-kernels
cp -r am/am-kernels/README ../../../ysyx-software-file/am-kernels
cp -r am/am-kernels/tests ../../../ysyx-software-file/am-kernels
# abstract-machine
cp -r am/abstract-machine/am ../../../ysyx-software-file/abstract-machine
cp -r am/abstract-machine/klib ../../../ysyx-software-file/abstract-machine
cp -r am/abstract-machine/LICENSE ../../../ysyx-software-file/abstract-machine
cp -r am/abstract-machine/README ../../../ysyx-software-file/abstract-machine
cp -r am/abstract-machine/Makefile ../../../ysyx-software-file/abstract-machine
cp -r am/abstract-machine/scripts ../../../ysyx-software-file/abstract-machine
