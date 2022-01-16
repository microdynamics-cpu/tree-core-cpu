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
    ubt20_64_package_list=("git" "perl" "python3" "make" "g++" "libfl2" "libfl-dev" "zlibc" "zlib1g" "zlib1g-dev" "ccache" "libgoogle-perftools-dev" "numactl" "perl-doc")
    for package in ${ubt20_64_package_list[@]} ; do
        install_package $package
    done

    dpkg -s verilator >/dev/null 2>&1 && {
        echo "verilator has been installed."
    } || {
        wget -O /tmp/verilator_4_204_amd64.deb https://gitee.com/oscpu/install/attach_files/817254/download/verilator_4_204_amd64.deb
        sudo dpkg -i /tmp/verilator_4_204_amd64.deb
        rm /tmp/verilator_4_204_amd64.deb
    }
}

install_mill() {
    install_package curl
    install_package default-jre
    
    which mill >/dev/null 2>&1 && {
        echo "mill has been installed."
    } || {
        sudo mkdir /usr/local/bin >/dev/null 2>&1
        wget -O /tmp/mill https://gitee.com/oscpu/install/raw/master/mill
        sudo chmod +x /tmp/mill
        sudo mv /tmp/mill /usr/local/bin/
    }
}

install_verilator

# install libsqlite3-dev for difftest
install_package libsqlite3-dev
# install libreadline-dev libsdl2-dev bison for NEMU
install_package libreadline-dev libsdl2-dev bison
# install cmake for DRAMsim3
install_package cmake

[[ $GTKWAVE == "true" ]] && install_package gtkwave libcanberra-gtk-module
[[ $CHISEL == "true" ]] && install_mill

echo "finish!"
