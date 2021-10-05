#include <unistd.h>
#include <getopt.h>

#ifdef DUMP_WAVE_VCD
#include <verilated_vcd_c.h>
#elif DUMP_WAVE_FST
#include <verilated_fst_c.h>
#endif
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
    dutPtr = new VysyxSoCFull;
    dutPtr->reset = 1;
    for (int i = 0; i < 10; i++)
    {
      dutPtr->clock = 0;
      dutPtr->eval();
      dutPtr->clock = 1;
      dutPtr->eval();
    }
    dutPtr->clock = 0;
    dutPtr->reset = 0;
    dutPtr->eval();

    if (args.dumpWave)
    {
#ifdef DUMP_WAVE_VCD
      wavePtr = new VerilatedVcdC;
#elif DUMP_WAVE_FST
      wavePtr = new VerilatedFstC;
#endif
      Verilated::traceEverOn(true);
      printf("`dump-wave` enabled, waves will be written to \"soc.wave\".\n");
      dutPtr->trace(wavePtr, 1);
      wavePtr->open("soc.wave");
      wavePtr->dump(0);
    }
  }
  ~Emulator()
  {
    if (args.dumpWave)
    {
      wavePtr->close();
      delete wavePtr;
    }
  }

  void step()
  {
    dutPtr->clock = 1;
    dutPtr->eval();
    cycle++;
    if (args.dumpWave && args.dumpBegin <= cycle && cycle <= args.dumpEnd)
    {
        wavePtr->dump((vluint64_t)cycle);
    }
    dutPtr->clock = 0;
    dutPtr->eval();
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
          args.dumpWave = true;
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
        args.dumpBegin = atoll(optarg);
        break;
      case 'e':
        args.dumpEnd = atoll(optarg);
        break;
      }
    }

    Verilated::commandArgs(argc, argv);
  }

  static inline void print_help(const char *file)
  {
    printf("Usage: %s [OPTION...]\n", file);
    printf("\n");
    printf("  -i, --image=FILE    run with this image file\n");
    printf("      --dump-wave     dump vcd(fst) format waveform when log is enabled.\n");
    printf("                      recommand use fst format, becuase fst format wave\n");
    printf("                      file is much smaller than vcd format. You need to\n");
    printf("                      change compiler option in Makefile to switch format.\n");
    printf("  -b, --log-begin=NUM display log from NUM th cycle\n");
    printf("  -e, --log-end=NUM   stop display log at NUM th cycle\n");
    printf("  -h, --help          print program help info\n");
    printf("\n");
  }

  unsigned long long cycle = 0;

  struct Args
  {
    bool dumpWave = false;
    unsigned long dumpBegin = 0;
    unsigned long dumpEnd = -1;
    const char *image = nullptr;
  } args;

  VysyxSoCFull *dutPtr = nullptr;

#ifdef DUMP_WAVE_VCD
  VerilatedVcdC *wavePtr = nullptr;
#elif DUMP_WAVE_FST
  VerilatedFstC *wavePtr = nullptr;
#endif
  
};
