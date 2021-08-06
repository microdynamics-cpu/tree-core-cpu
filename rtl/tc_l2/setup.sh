#!/bin/bash

# to print the color in terminal
INFO="\033[1;33m"
ERROR="\033[1;31m"
RIGHT="\033[1;32m"
END="\033[0m"


ROOT_PATH=$(dirname $(readlink -f "$0"))
AM_FOLDER_PATH=${ROOT_PATH}"/am"
ABSTRACT_MACHINE_FOLDER_PATH=${AM_FOLDER_PATH}"/abstract-machine"
RISCV_TESTS_FOLDER_PATH=${AM_FOLDER_PATH}"/riscv-tests"
AM_KERNELS_FOLDER_PATH=${AM_FOLDER_PATH}"/am-kernels"

DIFFTEST_FOLDER_PATH=${ROOT_PATH}"/difftest"
NEMU_FOLDER_PATH=${ROOT_PATH}"/NEMU"
# download the am repo from the github
mkdir -p ${AM_FOLDER_PATH}
cd ${AM_FOLDER_PATH}

###### abstract-machine ######
if [[ -d ${ABSTRACT_MACHINE_FOLDER_PATH} ]]; then
    echo -e "${RIGHT}abstract-machine exist!${END}"
    # if git fsck --full != 0; then
    #     echo "[download error]: remove the dir and git clone"
    #     rm -rf abstract-machine
    #     git clone https://github.com/NJU-ProjectN/abstract-machine.git
    # fi
else
    echo -e "${INFO}[no download]: git clone${END}"
    git clone https://github.com/NJU-ProjectN/abstract-machine.git
fi

cd ${ABSTRACT_MACHINE_FOLDER_PATH}
git checkout ysyx2021

if [[ -z $AM_HOME ]]; then
    echo -e "${INFO}AM_HOME is empty, set AM_HOME...${END}"
    export AM_HOME=${ABSTRACT_MACHINE_FOLDER_PATH}

elif [[ $AM_HOME != ${ABSTRACT_MACHINE_FOLDER_PATH} ]]; then
    echo -e "${ERROR}AM_HOME is set error, error value: $AM_HOME${END}"
    export AM_HOME=${ABSTRACT_MACHINE_FOLDER_PATH}

else
    echo -e "${RIGHT}AM_HOME exist and is a right value${END}"
fi
echo -e "${RIGHT}AM_HOME: $AM_HOME${END}"

cd ${AM_FOLDER_PATH}

###### riscv-tests ######
if [[ -d ${RISCV_TESTS_FOLDER_PATH} ]]; then
    echo -e "${RIGHT}riscv-tests exist!${END}"
    # if git fsck --full != 0; then
    #     echo "[download error]: remove the dir and git clone"
    #     rm -rf riscv-tests
    #     git clone https://github.com/NJU-ProjectN/riscv-tests.git
    # fi
else
    echo -e "${INFO}[no download]: git clone${END}"
   git clone https://github.com/NJU-ProjectN/riscv-tests.git
fi

###### am-kernels ######
if [[ -d ${AM_KERNELS_FOLDER_PATH} ]]; then
    echo -e "${RIGHT}am-kernels exist!${END}"
    # if git fsck --full != 0; then
    #     echo "[download error]: remove the dir and git clone"
    #     rm -rf am-kernels
    #     git clone https://github.com/NJU-ProjectN/am-kernels.git
    # fi
else
    echo -e "${INFO}[no download]: git clone${END}"
   git clone https://github.com/NJU-ProjectN/am-kernels.git
fi

cd ${ROOT_PATH} # am -> tc-l2

# download the specific version difftest and NEMU
# the version is same as the https://github.com/OSCPU/oscpu-framework.git
###### difftest ######
if [[ -d ${DIFFTEST_FOLDER_PATH} ]]; then
    echo -e "${RIGHT}difftest exist!${END}"
    # if git fsck --full != 0; then
    #     echo "[download error]: remove the dir and git clone"
    #     rm -rf am-kernels
    #     git clone https://github.com/NJU-ProjectN/am-kernels.git
    # fi
else
    echo -e "${INFO}[no download]: git clone${END}"
    git clone https://github.com/OpenXiangShan/difftest.git
fi

cd ${DIFFTEST_FOLDER_PATH}
git checkout -b 086c891828d1f8a1a2738c90e0b10c1f98cc61e0
# change the ram size from 8G to 256MB
sed -i 's/^\/\/\s\+\(#define\s\+EMU_RAM_SIZE\s\+(256\)/\1/' src/test/csrc/common/ram.h
sed -i 's/^#define\s\+EMU_RAM_SIZE\s\+(8/\/\/ &/' src/test/csrc/common/ram.h

cd ${ROOT_PATH}

###### NEMU ######
if [[ -d ${NEMU_FOLDER_PATH} ]]; then
    echo -e "${RIGHT}NEMU exist!${END}"
    # if git fsck --full != 0; then
    #     echo "[download error]: remove the dir and git clone"
    #     rm -rf am-kernels
    #     git clone https://github.com/NJU-ProjectN/am-kernels.git
    # fi
else
    echo -e "${INFO}[no download]: git clone${END}"
    git clone https://github.com/OpenXiangShan/NEMU.git
fi

cd ${NEMU_FOLDER_PATH}
git checkout -b 1e6883d271e48d2412bc46af852a093d7a7fdde7

if [[ -z $NEMU_HOME ]]; then
    echo -e "${INFO}NEMU_HOME is empty, set NEMU_HOME...${END}"
    export NEMU_HOME=${NEMU_FOLDER_PATH}
    export NOOP_HOME=${ROOT_PATH}

elif [[ $NEMU_HOME != ${NEMU_FOLDER_PATH} ]]; then
    echo -e "${ERROR}NEMU_HOME is set error, error value: $NEMU_HOME${END}"
    export NEMU_HOME=${NEMU_FOLDER_PATH}
    export NOOP_HOME=${ROOT_PATH}
else
    echo -e "${RIGHT}NEMU_HOME exist and is a right value${END}"
fi
echo -e "${RIGHT}NEMU_HOME: $NEMU_HOME${END}"
echo -e "${RIGHT}NOOP_HOME: $NOOP_HOME${END}"


make defconfig riscv64-xs-ref_defconfig
# change the sim memory from 8G to 256MB
sed -i 's/^\(CONFIG_MSIZE=0x\)\(.*\)/\110000000/' .config

cd ${ROOT_PATH}



