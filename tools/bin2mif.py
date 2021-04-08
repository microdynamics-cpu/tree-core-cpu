import binascii
import sys
import os

try:
    ram_size = 8192

    filePath0 = sys.argv[1]
    filePath2 = sys.argv[2]

    file0 = open(filePath0,'rb')
    file2 = open(filePath2,'w')

    inst_len = os.path.getsize(filePath0)

    file2.write('DEPTH = {0};\n'.format(ram_size))
    file2.write('WIDTH = {0};\n'.format(32))
    file2.write('\n')
    file2.write('ADDRESS_RADIX = HEX;\n')
    file2.write('DATA_RADIX = HEX;\n')
    file2.write('\n')
    file2.write('CONTENT BEGIN\n')

    line_num = 0
    for i in range(inst_len >> 2):
        temp = file0.read(4)
        temp = binascii.b2a_hex(temp[::-1])
        temp = temp.decode()
        temp = str(temp).upper()
        
        file2.write('\t%04x: %s;\n' % (line_num, temp))
        line_num = line_num + 1

    inst_tail_len = inst_len % 4;

    if(inst_tail_len):
        temp = file0.read(inst_tail_len)
        temp = binascii.b2a_hex(temp[::-1])
        temp = temp.decode()
        temp = str(temp).upper()
        temp = (4 - inst_tail_len) * '00' + temp;

        file2.write('\t%04x: %s;\n' % (line_num, temp))
        line_num = line_num + 1

        inst_word_len = (inst_len >> 2) + 1;
    else:
        inst_word_len = (inst_len >> 2);

        file2.write('\t%04x: %s;\n' % (line_num, temp))
        line_num = line_num + 1

    file2.write('\n')
    file2.write('END;')

    file0.close()
    file2.close()

except:
    print("Unexpected error:", sys.exc_info())
    print(filePath2)
