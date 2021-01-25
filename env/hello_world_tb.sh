#!/usr/bin/env bash

TEST_MODULE="hello_world_tb"
TEST_FILE="./${TEST_MODULE}.v"
GTKW_FILE="./${TEST_MODULE}.gtkw"

if command -v iverilog > /dev/null 2>&1; then
    if command -v gtkwave > /dev/null 2>&1; then
        iverilog -o "${TEST_MODULE}.vvp" ${TEST_FILE}
        vvp "${TEST_MODULE}.vvp"
        if [ -f ${GTKW_FILE} ]; then
            gtkwave ${GTKW_FILE}
        else
            gtkwave "${TEST_MODULE}.vcd"
        fi
    else
        echo "gtkwave has not been installed!"
    fi
else
    echo "iverilog has not been installed!"
fi
