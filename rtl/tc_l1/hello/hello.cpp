#include "Vhello.h"
#include "verilated.h"

int main(int argc, char **argv, char **env)
{
    VerilatedContext *contextp = new VerilatedContext;
    contextp->commandArgs(argc, argv);
    Vhello *top = new Vhello{contextp};
    while (!contextp->gotFinish())
    {
        top->eval();
    }
    delete top;
    delete contextp;
    return 0;
}