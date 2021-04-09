import sys
import filecmp
import subprocess
import sys
import os


# 主函数
def main():
    rtl_dir = r'../../'
    tb_file = r'/tb/tinyriscv_soc_tb.v'
    # iverilog程序
    iverilog_cmd = ['iverilog']
    # 顶层模块
    # iverilog_cmd += ['-s', r'tinyriscv_soc_tb']
    # 编译生成文件
    iverilog_cmd += ['-o', r'out.vvp']
    # 头文件(defines.v)路径
    iverilog_cmd += ['-I', rtl_dir + r'/rtl/tc_l1/core']
    # 宏定义，仿真输出文件
    iverilog_cmd += ['-D', r'OUTPUT="signature.output"']
    # testbench文件
    iverilog_cmd.append(rtl_dir + tb_file)
    # ../rtl/tc_l1/core
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/clint.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/csr_reg.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/ctrl.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/defines.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/div.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/ex.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/ex_mem.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/mem.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/mem_wb.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/wb.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/id.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/id_ex.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/if_id.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/pc_reg.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/regs.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/rib.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/core/tinyriscv.v')
    # ../rtl/tc_l1/comp
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/comp/ram.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/comp/rom.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/comp/timer.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/comp/uart.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/comp/gpio.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/comp/spi.v')
    # ../rtl/tc_l1/debug
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/debug/jtag_dm.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/debug/jtag_driver.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/debug/jtag_top.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/debug/uart_debug.v')
    # ../rtl/tc_l1/soc
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/soc/tinyriscv_soc_top.v')
    # ../rtl/tc_l1/utils
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/utils/full_handshake_rx.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/utils/full_handshake_tx.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/utils/gen_buf.v')
    iverilog_cmd.append(rtl_dir + r'/rtl/tc_l1/utils/gen_dff.v')

    # 编译
    process = subprocess.Popen(iverilog_cmd)
    process.wait(timeout=5)

if __name__ == '__main__':
    sys.exit(main())
