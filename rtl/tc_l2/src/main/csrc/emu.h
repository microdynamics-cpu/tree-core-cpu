#include <unistd.h>
#include <getopt.h>

#include <verilated_vcd_c.h>
#include <verilated.h>
#include <VysyxSoCFull.h>

extern "C"
{
  void flash_init(const char *img);
}

class Emulator
{
public:
  Emulator(int argc, char *argv[])
  {
    parseArgs(argc, argv);

    if (args.image == nullptr)
    {
      printf("Image file unspecified. Use -i to provide the image of flash");
      exit(1);
    }
    printf("Initializing flash with \"%s\" ...\n", args.image);
    flash_init(args.image);

    printf("Initializing and resetting DUT ...\n");
    dut_ptr = new VysyxSoCFull;
    dut_ptr->reset = 1;
    for (int i = 0; i < 10; i++)
    {
      dut_ptr->clock = 0;
      dut_ptr->eval();
      dut_ptr->clock = 1;
      dut_ptr->eval();
    }
    dut_ptr->clock = 0;
    dut_ptr->reset = 0;
    dut_ptr->eval();

    if (args.dump_wave)
    {
      Verilated::traceEverOn(true);
      printf("`dump-wave` enabled, waves will be written to \"vlt_dump.vcd\".\n");
      fp = new VerilatedVcdC;
      dut_ptr->trace(fp, 1);
      fp->open("vlt_dump.vcd");
      fp->dump(0);
    }
  }
  ~Emulator()
  {
    if (args.dump_wave)
    {
      fp->close();
      delete fp;
    }
  }

  void step()
  {
    dut_ptr->clock = 1;
    dut_ptr->eval();
    cycle++;
    if (args.dump_wave && args.dump_begin <= cycle && cycle <= args.dump_end)
      fp->dump((vluint64_t)cycle);
    dut_ptr->clock = 0;
    dut_ptr->eval();
  }

  unsigned long long get_cycle()
  {
    return cycle;
  }

private:
  void parseArgs(int argc, char *argv[])
  {

    int long_index;
    const struct option long_options[] = {
        {"dump-wave", 0, NULL, 0},
        {"log-begin", 1, NULL, 'b'},
        {"log-end", 1, NULL, 'e'},
        {"image", 1, NULL, 'i'},
        {"help", 0, NULL, 'h'},
        {0, 0, NULL, 0}};

    int o;
    while ((o = getopt_long(argc, const_cast<char *const *>(argv),
                            "-hi:b:e:", long_options, &long_index)) != -1)
    {
      switch (o)
      {
      case 0:
        switch (long_index)
        {
        case 0:
          args.dump_wave = true;
          continue;
        }
        // fall through
      default:
        print_help(argv[0]);
        exit(0);
      case 'i':
        args.image = optarg;
        break;
      case 'b':
        args.dump_begin = atoll(optarg);
        break;
      case 'e':
        args.dump_end = atoll(optarg);
        break;
      }
    }

    Verilated::commandArgs(argc, argv);
  }

  static inline void print_help(const char *file)
  {
    printf("Usage: %s [OPTION...]\n", file);
    printf("\n");
    printf("  -i, --image=FILE           run with this image file\n");
    printf("      --dump-wave            dump waveform when log is enabled\n");
    printf("  -b, --log-begin=NUM        display log from NUM th cycle\n");
    printf("  -e, --log-end=NUM          stop display log at NUM th cycle\n");
    printf("  -h, --help                 print program help info\n");
    printf("\n");
  }

  unsigned long long cycle = 0;

  struct Args
  {
    bool dump_wave = false;
    unsigned long dump_begin = 0;
    unsigned long dump_end = -1;
    const char *image = nullptr;
  } args;

  VysyxSoCFull *dut_ptr = nullptr;
  VerilatedVcdC *fp = nullptr;
};
