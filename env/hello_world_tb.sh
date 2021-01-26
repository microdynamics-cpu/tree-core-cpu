#!/usr/bin/env bash

# 设置测试与波形文件的路径
# Set the path of test and wavefrom file
TEST_MODULE="hello_world_tb"
TEST_FILE="./${TEST_MODULE}.v"
GTKW_FILE="./${TEST_MODULE}.gtkw"

# 判断iverilog是否安装
# Judge whether iverilog is installed
if command -v iverilog > /dev/null 2>&1; then
    # 判断gtkwave是否安装
    # Judge whether gtkwave is installed
    if command -v gtkwave > /dev/null 2>&1; then
        # 生成vvp仿真文件
        # Generate vvp simulation file
        iverilog -o "${TEST_MODULE}.vvp" ${TEST_FILE}
        # 执行vvp仿真文件
        # Execute vvp simulation file
        vvp "${TEST_MODULE}.vvp"
        # 判断波形文件是否存在
        # Judge whether the wavefrom file exists.
        if [ -f ${GTKW_FILE} ]; then
            # 如果存在则直接加载波形文件执行
            # If it exists, directly load the wavefrom file to execute
            gtkwave ${GTKW_FILE}
        else
            # 如果不存在则加载vcd文件后执行
            # If it doesn't exist, load the vcd file and execute
            gtkwave "${TEST_MODULE}.vcd"
        fi
    else
        echo "gtkwave has not been installed!"
    fi
else
    echo "iverilog has not been installed!"
fi
