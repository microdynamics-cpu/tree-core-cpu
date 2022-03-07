#!/bin/bash

# to print the color in terminal
INFO="\033[0;33m"
ERROR="\033[0;31m"
RIGHT="\033[0;32m"
END="\033[0m"

# if not have content, create, otherwise check
configContent() {
    cd $1
    # pwd
    echo -e "${INFO}check Makefile...${END}"
    if [[ -f "Makefile" ]]; then
        echo -e "${RIGHT}Makefile exist!${END}"
    else
        echo -e "${INFO}no exist${END}"
        echo "start config Makefile..."
        echo "sim-verilog:" > Makefile
        echo -e "${RIGHT}config[SUCCESSFUL]${END}"
    fi

    echo -e "${INFO}check source dir...${END}"
    if [[ -d "src" ]]; then
        echo -e "${RIGHT}src exist!${END}"
    else
        echo -e "${INFO}no exist${END}"
        echo "start generate dir..."
        mkdir -p src/main/csrc
        mkdir -p src/main/scala/axi4
        mkdir -p src/main/scala/common
        mkdir -p src/main/scala/core
        mkdir -p src/main/scala/port
        mkdir -p src/main/scala/top
        mkdir -p src/main/scala/utils
        echo -e "${RIGHT}generate src dir[SUCCESSFUL]${END}"
    fi

    echo -e "${INFO}check difftest file...${END}"
    if [[ -f "src/main/scala/utils/Difftest.scala" ]]; then
        echo -e "${RIGHT}Difftest.scala exist!${END}"
    else
        echo -e "${INFO}no exist${END}"
        echo "start generate Difftest.scala..."
        cp ../dependency/difftest/src/main/scala/Difftest.scala src/main/scala/utils/Difftest.scala
        echo -e "${RIGHT}generate Difftest.scala[SUCCESSFUL]${END}"
    fi

    echo -e "${INFO}check module prefix file...${END}"
    if [[ -f "src/main/scala/utils/AddModulePrefix.scala" ]]; then
        echo -e "${RIGHT}AddModulePrefix.scala exist!${END}"
    else
        echo -e "${INFO}no exist${END}"
        echo "start generate AddModulePrefix.scala..."
        cp ../tc_l2/src/main/scala/utils/AddModulePrefix.scala src/main/scala/utils/AddModulePrefix.scala
        echo -e "${RIGHT}generate AddModulePrefix.scala[SUCCESSFUL]${END}"
    fi
}

configTemplate() {
    if [[ -d $1 ]]; then
        echo -e "${RIGHT}$1 exist!${END}"
    else
        mkdir $1
        echo -e "${INFO}[no exist]: config project...${END}"
    fi

    configContent $1
}

configTarget() {
    # HACK: need to make one check statement
    # now dont receive 'tc_l2' as parameter
    if [[ -n $1 && $1 == "tc_l3" ]]; then
        configTemplate $1
    elif [[ -n $1 && $1 == "tc_l4" ]]; then
        configTemplate $1
    else
        configTemplate "tc_l1" # include other error parameters condition
    fi
}

helpInfo() {
    echo -e "${INFO}Usage: setup.sh [-t target][-h]${END}"
    echo -e "Description - set up the template dir env of the treecore riscv processor"
    echo -e ""
    echo -e "${RIGHT}  -t: config specific target directory structure${END}"
    echo -e "sample: ./setup.sh -t [target](default: tc_l1) ${INFO}[target]: [tc_l1, tc_l2, tc_l3, ...]${END}"
    echo -e "${RIGHT}  -h: help information${END}"
}

while getopts 't:h' OPT; do
    case $OPT in
        t) configTarget $OPTARG;;
        h) helpInfo;;
        ?) 
        echo -e "${ERROR}invalid parameters!!!${END}"
        helpInfo
        ;;
    esac
done