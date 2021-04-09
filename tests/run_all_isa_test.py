import sys
import filecmp
import subprocess
import sys
import os


# 找出path目录下的所有bin文件
def list_binfiles(path):
    files = []
    list_dir = os.walk(path)
    for maindir, subdir, all_file in list_dir:
        for filename in all_file:
            apath = os.path.join(maindir, filename)
            if apath.endswith('.bin'):
                files.append(apath)

    # print(maindir)
    # print(all_file)
    return files

# 主函数
def main():
    bin_files = list_binfiles(r'./riscv-compliance/build_generated/rv32i')
    bin_files += list_binfiles(r'./riscv-compliance/build_generated/rv32im')
    # print(bin_files)

    # 对每一个bin文件进行测试
    pass_cnt = 0
    fail_cnt = 0
    fail_info = []
    for file in bin_files:
        # print(file)
        cmd = r'python compliance_test.py ' + ' ' + file
        f = os.popen(cmd)
        r = f.read()
        f.close()
        # print(r)
        if (r.find('PASS') != -1):
            print(file + '    [PASS]')
            pass_cnt = pass_cnt + 1

        else:
            print(file + '    [FAIL]')
            fail_cnt = fail_cnt + 1
            fail_info.append(file)

    print('######', 'PASS(%d), FAIL(%d)' % (pass_cnt, fail_cnt), '######', sep=' ')
    print('Fail File Name:')
    for val in fail_info:
        print(val);

if __name__ == '__main__':
    sys.exit(main())