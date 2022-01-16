#!/bin/bash

# to print the color in terminal
INFO="\033[0;33m"
ERROR="\033[0;31m"
RIGHT="\033[0;32m"
END="\033[0m"


ROOT_PATH=$(dirname $(readlink -f "$0"))/../dependency
AM_FOLDER_PATH=${ROOT_PATH}"/am"
ABSTRACT_MACHINE_FOLDER_PATH=${AM_FOLDER_PATH}"/abstract-machine"
RISCV_TESTS_FOLDER_PATH=${AM_FOLDER_PATH}"/riscv-tests"
AM_KERNELS_FOLDER_PATH=${AM_FOLDER_PATH}"/am-kernels"

DIFFTEST_FOLDER_PATH=${ROOT_PATH}"/difftest"
NEMU_FOLDER_PATH=${ROOT_PATH}"/NEMU"
DRAMSIM3_FOLDER_PATH=${ROOT_PATH}"/DRAMsim3"
YSYXSOC_PATH=${ROOT_PATH}"/ysyxSoC"

#TODO: am-kernel, simple-test need to dowload from the 'ysyx_software_file' repo
# download the am repo from the github
###### abstract-machine ######
configAbstractMachine() {
    mkdir -p ${AM_FOLDER_PATH}
    cd ${AM_FOLDER_PATH}

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

    cd ${ROOT_PATH}
}

###### riscv-tests ######
configTestSuites() {
    mkdir -p ${AM_FOLDER_PATH}
    cd ${AM_FOLDER_PATH}

    if [[ -d ${RISCV_TESTS_FOLDER_PATH} ]]; then
        echo -e "${RIGHT}riscv-tests exist!${END}"
    else
        echo -e "${INFO}[no download]: git clone${END}"
    git clone https://github.com/NJU-ProjectN/riscv-tests.git
    fi

    # cd ${ROOT_PATH}

    # mkdir -p ${AM_FOLDER_PATH}
    # cd ${AM_FOLDER_PATH}

    # if [[ -d ${CPU_TESTS_FOLDER_PATH} ]]; then
    #     echo -e "${RIGHT}simple-tests exist!${END}"
    # else
    #     echo -e "${INFO}[no download]: git clone${END}"
    # git clone https://github.com/NJU-ProjectN
    # fi

    # cd ${ROOT_PATH}
}

###### am-kernels ######
configAMKernels() {
    mkdir -p ${AM_FOLDER_PATH}
    cd ${AM_FOLDER_PATH}

    if [[ -d ${AM_KERNELS_FOLDER_PATH} ]]; then
        echo -e "${RIGHT}am-kernels exist!${END}"
    else
        echo -e "${INFO}[no download]: git clone${END}"
    git clone https://github.com/NJU-ProjectN/am-kernels.git
    fi

    cd ${ROOT_PATH}
}

# download the specific commit id difftest and NEMU
# the commit id is same as the https://github.com/OSCPU/oscpu-framework.git
###### difftest ######
configDiffTest() {
    cd ${ROOT_PATH}

    if [[ -d ${DIFFTEST_FOLDER_PATH} ]]; then
        echo -e "${RIGHT}difftest exist!${END}"
    else
        echo -e "${INFO}[no download]: git clone${END}"
        git clone https://gitee.com/oscpu/difftest.git
    fi

    cd ${DIFFTEST_FOLDER_PATH}
    git checkout 56d947b
    # change the ram size from 8G to 256MB
    sed -i 's/^\/\/\s\+\(#define\s\+EMU_RAM_SIZE\s\+(256\)/\1/' src/test/csrc/common/ram.h
    sed -i 's/^#define\s\+EMU_RAM_SIZE\s\+(8/\/\/ &/' src/test/csrc/common/ram.h

    cd ${ROOT_PATH}
}

###### NEMU ######
configNemu() {
    if [[ -d ${NEMU_FOLDER_PATH} ]]; then
        echo -e "${RIGHT}NEMU exist!${END}"
    else
        echo -e "${INFO}[no download]: git clone${END}"
        git clone https://gitee.com/oscpu/NEMU.git
    fi

    cd ${NEMU_FOLDER_PATH}
    git checkout e402575

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
    # need to enter 'make menuconfig' and 
    # modify [Memory Configuration]->[Memory size] to '0x10000000' manually
    # sed -i 's/^\(CONFIG_MSIZE=0x\)\(.*\)/\110000000/' .config

    cd ${ROOT_PATH}
}

# the commit id is same as the https://github.com/OSCPU/oscpu-framework.git
###### dramsim3 ######
configDramSim3() {
    cd ${ROOT_PATH}

    if [[ -d ${DRAMSIM3_FOLDER_PATH} ]]; then
        echo -e "${RIGHT}dramsim3 exist!${END}"
    else
        echo -e "${INFO}[no download]: git clone${END}"
        git clone https://github.com/OpenXiangShan/DRAMsim3.git
    fi

    cd ${DRAMSIM3_FOLDER_PATH}
    git checkout 5723f6b1cc157ac2d7b4154b50fd1799c9cf54aa
    cd ${ROOT_PATH}
}

###### ysyxSoC ######
configysyxSoC() {
    cd ${ROOT_PATH}

    if [[ -d ${YSYXSOC_PATH} ]]; then
        echo -e "${RIGHT}ysyxSoC exist!${END}"
    else
        echo -e "${INFO}[no download]: git clone${END}"
        git clone --depth 1 https://github.com/OSCPU/ysyxSoC.git
    fi
}

helpInfo() {
    echo -e "${INFO}Usage: setup.sh [-a][-n][-d][-i][-m][-r][-k][-y][-s repo][-h]${END}"
    echo -e "Description - set up the build env of the treecore riscv processor"
    echo -e ""
    echo -e "${RIGHT}  -a: download and config all the repos${END}"
    echo -e "${RIGHT}  -n: download and config nemu${END}"
    echo -e "${RIGHT}  -d: download and config difftest${END}"
    echo -e "${RIGHT}  -i: download and config dramsim3${END}"
    echo -e "${RIGHT}  -m: download and config abstract-machine${END}"
    echo -e "${RIGHT}  -r: download and config simple-tests, riscv-tests${END}"
    echo -e "${RIGHT}  -k: download and config am-kernels${END}"
    echo -e "${RIGHT}  -y: download and config ysyx-soc${END}"
    echo -e "${RIGHT}  -s: download and config specific repo${END}"
    echo -e "sample: ./setup.sh -s [repo](default: nemu) ${INFO}[repo]: [nemu, diffttest, dramsim3, am, testsuites, am-kernels, ysyx-soc]${END}"
    echo -e "${RIGHT}  -h: help information${END}"
    
}

configSpecRepo() {
    if [[ -n $1 && $1 == "all" ]]; then
        configAbstractMachine
        configTestSuites
        configAMKernels
        configDiffTest
        configNemu
        configDramSim3
        configysyxSoC
    elif [[ -n $1 && $1 == "nemu" ]]; then
        configNemu
    elif [[ -n $1 && $1 == "difftest" ]]; then
        configDiffTest
    elif [[ -n $1 && $1 == "dramsim3" ]]; then
        configDramSim3
    elif [[ -n $1 && $1 == "am" ]]; then
        configAbstractMachine
    elif [[ -n $1 && $1 == "testsuites" ]]; then
        configTestSuites
    elif [[ -n $1 && $1 == "am-kernels" ]]; then
        configAMKernels
    elif [[ -n $1 && $1 == "ysyx-soc" ]]; then
        configysyxSoC
    else
        echo -e "${ERROR}the params [$1] is not found.${END} opt value: [nemu, diffttest, dramsim3, am, testsuites, am-kernels, ysyx-soc]"
    fi
}

mkdir -p ${ROOT_PATH}
# Check parameters
while getopts 'andimrkys:h' OPT; do
    case $OPT in
        a) configSpecRepo "all";;
        n) configNemu;;
        d) configDiffTest;;
        i) configDramSim3;;
        m) configAbstractMachine;;
        r) configTestSuites;;
        k) configAMKernels;;
        y) configysyxSoC;;
        s) configSpecRepo $OPTARG;;
        h) helpInfo;;
        ?) 
        echo -e "${ERROR}invalid parameters!!!${END}"
        helpInfo
        ;;
    esac
done

