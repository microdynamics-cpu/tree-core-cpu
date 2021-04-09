import sys
import filecmp
import subprocess
import sys
import os


# 找出path目录下的所有reference_output文件
def list_ref_files(path):
    files = []
    list_dir = os.walk(path)
    for maindir, subdir, all_file in list_dir:
        for filename in all_file:
            apath = os.path.join(maindir, filename)
            if apath.endswith('.reference_output'):
                files.append(apath)
    # print(files)
    return files

# 根据bin文件找到对应的reference_output文件
def get_reference_file(bin_file):
    file_path, file_name = os.path.split(bin_file)
    tmp = file_name.split('.')
    # 得到bin文件的前缀部分
    prefix = tmp[0]
    #print('bin prefix: %s' % prefix)

    files = []
    if (bin_file.find('rv32im') != -1):
        files = list_ref_files(r'../riscv-compliance/riscv-test-suite/rv32im/references')
    elif (bin_file.find('rv32i') != -1):
        files = list_ref_files(r'../riscv-compliance/riscv-test-suite/rv32i/references')
    elif (bin_file.find('rv32Zicsr') != -1):
        files = list_ref_files(r'../riscv-compliance/riscv-test-suite/rv32Zicsr/references')
    elif (bin_file.find('rv32Zifencei') != -1):
        files = list_ref_files(r'../riscv-compliance/riscv-test-suite/rv32Zifencei/references')
    else:
        return None

    # 根据bin文件前缀找到对应的reference_output文件
    for file in files:
        if (file.find(prefix) != -1):
            return file, prefix

    return None, prefix

# 主函数
def main():
    # print(sys.argv[0] + ' ' + sys.argv[1])

    build_dir = os.getcwd() + '/build'
    # print(build_dir)
    # 判断是否已经存在该目录
    if not os.path.exists(build_dir):
        # 目录不存在，进行创建操作
        os.makedirs(build_dir) #使用os.makedirs()方法创建多层目录

    # 0. 
    os.chdir('./build')
    # print(os.getcwd())

    # 1.将bin文件转成mem文件
    cmd = r'python ../../tools/bin2mem.py' + r' ../' + sys.argv[1] + ' inst.data'
    # print(cmd)
    f = os.popen(cmd)
    f.close()

    # 2.编译rtl文件
    cmd = r'python ../compile_rtl.py'
    f = os.popen(cmd)
    f.close()

    # 3.运行
    logfile = open('run.log', 'w')
    vvp_cmd = [r'vvp']
    vvp_cmd.append(r'out.vvp')
    process = subprocess.Popen(vvp_cmd, stdout=logfile, stderr=logfile)
    process.wait(timeout=5)
    logfile.close()

    # 4.比较结果
    ref_file, ref_file_prefix = get_reference_file(sys.argv[1])
    print('[ISA COMPLIANCE TEST]--->[', ref_file_prefix, ']')
    print('START...')

    if (ref_file != None):
        # 如果文件大小不一致，直接报fail
        if (os.path.getsize('signature.output') != os.path.getsize(ref_file)):
            print('!!! FAIL, size != !!!')
            return
        f1 = open('signature.output')
        f2 = open(ref_file)
        f1_lines = f1.readlines()
        i = 0
        # 逐行比较
        for line in f2.readlines():
            # 只要有一行内容不一样就报fail
            if (f1_lines[i] != line):
                print('!!! FAIL, content != !!!')
                f1.close()
                f2.close()
                return
            i = i + 1
        f1.close()
        f2.close()
        print('### PASS! ###')
    else:
        print('No ref file found, please check result by yourself.')

if __name__ == '__main__':
    sys.exit(main())
