#!/bin/bash

help() {
    echo "Usage:"
    echo "install.sh [-g] [-c]"
    echo "Description:"
    echo "-g: Install gtkwave."
    echo "-c: Install mill for Chisel env."
    exit 0
}

while getopts 'hgc' OPT; do
    case $OPT in
        h) help;;
        g) GTKWAVE="true";;
        c) CHISEL="true";;
        ?) help;;
    esac
done

if !(cat /etc/*release | grep 'Ubuntu 20.04'); then
    echo "Your Linux branch does not meet the requirements, please use Ubuntu 20.04."
    exit 1
fi

UPDATED="false"
install_package() {
    for package in $*                     
    do
        dpkg -s "$package" >/dev/null 2>&1 && {
            echo "$package has been installed."
        } || {
            if [[ $UPDATED == "false" ]]; then
                UPDATED="true"
                sudo apt-get update
            fi
            sudo apt-get --yes install $package
        }
    done
}

install_verilator() {
    ubt20_64_package_list=("git" "perl" "python3" "make" "autoconf" "g++" "flex" "bison" "ccache"
    "libgoogle-perftools-dev" "numactl" "perl-doc" "libfl2" "libfl-dev" "zlibc" "zlib1g" "zlib1g-dev")
    for package in ${ubt20_64_package_list[@]} ; do
        install_package $package
    done

    which verilator >/dev/null 2>&1 && {
        echo "verilator has been installed."
    } || {
        mkdir -p dependency
        git clone https://github.com/verilator/verilator ./dependency/verilator # run first time

        # every time you need to build:
        # unsetenv VERILATOR_ROOT  # For csh; ignore error if on bash
        unset VERILATOR_ROOT  # for bash
        cd dependency/verilator
        git pull         # make sure git repository is up-to-date
        git tag          # see what versions exist
        #git checkout master      # use development branch (e.g. recent bug fixes)
        #git checkout stable      # use most recent stable release
        git checkout v4.204  # switch to specified release version

        autoconf         # create ./configure script
        ./configure      # configure and create Makefile
        make -j `nproc`  # build Verilator itself (if error, try just 'make')
        sudo make install
    }
}

install_mill() {
    install_package curl
    install_package default-jre
    
    which mill >/dev/null 2>&1 && {
        echo "mill has been installed."
    } || {
        sudo sh -c "curl -L https://github.com/com-lihaoyi/mill/releases/download/0.9.9/0.9.9 > /usr/local/bin/mill && chmod +x /usr/local/bin/mill"
    }
}

install_verilator

# install libsqlite3-dev for difftest
install_package libsqlite3-dev
# install libreadline-dev libsdl2-dev bison for NEMU
install_package libreadline-dev libsdl2-dev bison
# install cmake for DRAMsim3
install_package cmake
# isntall riscv toolchain
install_package g++-riscv64-linux-gnu
install_package binutils-riscv64-linux-gnu

[[ $GTKWAVE == "true" ]] && install_package gtkwave libcanberra-gtk-module
[[ $CHISEL == "true" ]] && install_mill

echo "############# verilator and mill install finish!!! #############"
