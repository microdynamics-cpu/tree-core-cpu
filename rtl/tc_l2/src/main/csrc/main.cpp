#include <cstdio>
#include <csignal>
#include <chrono>
namespace chrono = std::chrono;

#include "verilated.h" //Defines common routines
#include "VysyxSoCFull.h"

#include <emu.h>

static int signal_received = 0;

void sig_handler(int signo)
{
    if (signal_received != 0)
    {
        puts("SIGINT received, forcely shutting down.\n");
        exit(0);
    }
    puts("SIGINT received, gracefully shutting down... Type Ctrl+C again to stop forcely.\n");
    signal_received = signo;
}

static Emulator *emu = nullptr;
chrono::system_clock::time_point sim_start_time;
void release()
{
    if (emu != nullptr)
    {
        auto elapsed = chrono::duration_cast<chrono::seconds>(chrono::system_clock::now() - sim_start_time);
        printf("Simulated %llu cycles in %lds\n",
               emu->get_cycle(),
               elapsed.count());
        delete emu;
    }
}

int main(int argc, char *argv[])
{
    printf("Emu compiled at %s, %s\n", __DATE__, __TIME__);

    if (signal(SIGINT, sig_handler) == SIG_ERR)
    {
        printf("can't catch SIGINT\n");
    }
    atexit(release);

    emu = new Emulator(argc, argv);
    printf("Start simulating ...\n");
    sim_start_time = chrono::system_clock::now();
    while (!Verilated::gotFinish() && signal_received == 0)
    {
        emu->step();
    }

    return 0;
}
