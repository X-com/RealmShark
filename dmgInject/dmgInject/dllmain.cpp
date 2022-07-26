#include "pch.h"
#include <windows.h>
#include <strsafe.h>
#include <winsock2.h>
#include "trampoline.h"

//extern "C" int __stdcall asm_func();

//typedef __int64(__fastcall* tCalcDefDmg) (const struct QUrl* a1);
//
//tCalcDefDmg oCalcDefDmg;

HANDLE hPipe;
DWORD writtenSize = 0;
const char dataSize = 21;
BOOL success = FALSE;
char data[dataSize] = {0b10101010, 0b01010101, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

void sendDmgMSG(unsigned int dmg, unsigned int time, unsigned short id, unsigned int preDmg, unsigned int effect1, unsigned int effect2)
{
    data[2] = 0;
    *(unsigned int*)&data[3] = dmg;
    *(unsigned int*)&data[7] = time;
    *(unsigned int*)&data[11] = effect1;
    *(unsigned int*)&data[15] = effect2;
    *(unsigned short*)&data[19] = id;

    //dataSize = sizeof(data);

    success = WriteFile(
        hPipe,        // handle to pipe 
        data,     // buffer to write from 
        dataSize, // number of bytes to write 
        &writtenSize,   // number of bytes written 
        NULL);        // not overlapped I/O 

    if (!success || dataSize != writtenSize)
    {
        //printf("InstanceThread WriteFile failed, GLE=%d.\n", GetLastError());
        hPipe = 0;
        return;
    }
}

void sendRngMSG(unsigned int rngi, unsigned short id, unsigned int effect)
{
    data[2] = 1;
    *(unsigned int*)&data[3] = rngi;
    *(unsigned int*)&data[7] = effect;
    *(unsigned short*)&data[11] = id;
    //dataSize = sizeof(data);

    success = WriteFile(
        hPipe,        // handle to pipe 
        data,     // buffer to write from 
        dataSize, // number of bytes to write 
        &writtenSize,   // number of bytes written 
        NULL);        // not overlapped I/O 

    if (!success || dataSize != writtenSize)
    {
        //printf("InstanceThread WriteFile failed, GLE=%d.\n", GetLastError());
        hPipe = 0;
        return;
    }
}

void setPHandle() {
    hPipe = CreateFileA("\\\\.\\pipe\\pipeRealmDmg",
        //GENERIC_READ |  // read and write access 
        GENERIC_WRITE,
        0,              // no sharing 
        NULL,           // default security attributes
        OPEN_EXISTING,  // opens existing pipe 
        0,              // default attributes 
        NULL);          // no template file 

    if (hPipe == INVALID_HANDLE_VALUE) hPipe = 0;
}

void hook(unsigned int dmg, unsigned int time, unsigned short id, unsigned int preDmg, unsigned int *effects)
{
    if (hPipe)
    {
        printf("sending dmg:%d time:%d id:%d pre:%d\n", dmg, time, id, preDmg);
        //for (int i = 0; i < 12; i++) {
        //    printf("n:%d eff:%d\n", i, effects[i]);
        //}
        sendDmgMSG(dmg, time, id, preDmg, effects[8], effects[9]);
    }
    else
    {
        //printf("connecting\n");
        setPHandle();
    }
}

void hook2(unsigned int rngi, unsigned short id, unsigned int effect)
{
    if (hPipe)
    {
        printf("rngi:%d id:%d eff:%x\n", rngi, id, effect);
        sendRngMSG(rngi, id, effect);
    }
    else
    {
        //printf("connecting\n");
        setPHandle();
    }
}

void HookFunction(DWORD_PTR targetAddress, uintptr_t HookFunctionAddress, uint8_t hookSize)
{
    uint8_t jmpInstruction[15], methodSize;
    uintptr_t trampolineAddress, trampoAddressPointer;

    trampoAddressPointer = trampolineAddress = (uintptr_t)VirtualAlloc(0, hookSize + 100, MEM_COMMIT | MEM_RESERVE, PAGE_EXECUTE_READWRITE);

    memset((void*)trampoAddressPointer, 0x58, 1);
    trampoAddressPointer += 1;

    memcpy((void*)trampoAddressPointer, (void*)targetAddress, hookSize);
    trampoAddressPointer += hookSize;

    memcpy((void*)trampoAddressPointer, "\x50\x41\x53\x41\x52\x41\x51\x41\x50\x52\x51\x48\x83\xEC\x30\x48\x89\xE9\x4C\x8B\x8B\x84\x01\x00\x00\x48\x8B\x87\x20\x02\x00\x00\x48\x89\x44\x24\x20", 37);
    trampoAddressPointer += 37;
    //memcpy((void*)trampoAddressPointer, "\x41\x53\x41\x52\x41\x51\x41\x50\x52\x51\x48\x89\xE9", 13);
    //trampoAddressPointer += 13;
    //memcpy((void*)trampoAddressPointer, "\x48\x89\xE9", 3);
    //trampoAddressPointer += 3;

    methodSize = SetupJumpInstructionBytes(jmpInstruction, trampoAddressPointer, HookFunctionAddress, 0xE8, 0);
    memcpy((void*)trampoAddressPointer, jmpInstruction, methodSize);
    trampoAddressPointer += methodSize;

    memcpy((void*)trampoAddressPointer, "\x48\x83\xC4\x30\x59\x5A\x41\x58\x41\x59\x41\x5A\x41\x5B", 14);
    trampoAddressPointer += 14;

    uint8_t jumpSize = SetupJumpInstructionBytes(jmpInstruction, trampoAddressPointer, targetAddress + (DWORD_PTR)hookSize - (DWORD_PTR)1, 0xE9, 0);
    memcpy((void*)trampoAddressPointer, jmpInstruction, jumpSize);

    PlaceJumpToAddress(targetAddress, trampolineAddress, hookSize);
}

void HookFunction2(DWORD_PTR targetAddress, uintptr_t HookFunctionAddress, uint8_t hookSize)
{
    uint8_t jmpInstruction[15], methodSize;
    uintptr_t trampolineAddress, trampoAddressPointer;

    trampoAddressPointer = trampolineAddress = (uintptr_t)VirtualAlloc(0, hookSize + 100, MEM_COMMIT | MEM_RESERVE, PAGE_EXECUTE_READWRITE);

    memcpy((void*)trampoAddressPointer, "\x41\x53\x41\x52\x41\x51\x41\x50\x52\x51\x48\x83\xEC\x20\x48\x8B\x96\xB0\x01\x00\x00\x4C\x8B\x87\x20\x02\x00\x00\x4D\x8B\x40\x20", 32);
    trampoAddressPointer += 32;

    methodSize = SetupJumpInstructionBytes(jmpInstruction, trampoAddressPointer, HookFunctionAddress, 0xE8, 0);
    memcpy((void*)trampoAddressPointer, jmpInstruction, methodSize);
    trampoAddressPointer += methodSize;

    memcpy((void*)trampoAddressPointer, "\x48\x83\xC4\x20\x59\x5A\x41\x58\x41\x59\x41\x5A\x41\x5B\x58", 15);
    trampoAddressPointer += 15;

    memcpy((void*)trampoAddressPointer, (void*)targetAddress, hookSize);
    trampoAddressPointer += hookSize;

    memset((void*)trampoAddressPointer, 0x50, 1);
    trampoAddressPointer += 1;

    uint8_t jumpSize = SetupJumpInstructionBytes(jmpInstruction, trampoAddressPointer, targetAddress + (DWORD_PTR)hookSize - (DWORD_PTR)1, 0xE9, 0);
    memcpy((void*)trampoAddressPointer, jmpInstruction, jumpSize);

    PlaceJumpToAddress(targetAddress, trampolineAddress, hookSize);
}

DWORD WINAPI InitiateHooks(HMODULE hModule)
{
    AllocConsole();
    FILE* f;
    freopen_s(&f, "CONOUT$", "w", stdout);

    // 00007FFB60924AF5 | 4C:8BC8 | mov r9,rax| breakif(0), logif(1, Hook here)
    // 00007FFB60924AF8 | 45:0FB6C5 | movzx r8d, r13b |
    // \x4C\x8B\xC8\x45\x0F\xB6\xC5
    //DWORD_PTR loc = FindPattern("gameassembly.dll", "\x4C\x8B\xC8\x45\x0F\xB6\xC5");
    //                                        8B C7 48 8B 9C 24 A0 00 00 00 48 81 C4 80 00 00 00

    // 48 8D AC 24 E0 FE FF FF 48 81 EC 20 02 00 00
    // 4C 8D 5C 24 70 49 8B 5B 30 49 8B 73 38 49 8B 7B 40 49 8B E3 41 5F 41 5E 41 5D 41 5C 5D
    //std::vector<char> pattern = vectorFromString("48 8D AC 24 E0 FE FF FF 48 81 EC 20 02 00 00");
    //DWORD_PTR loc = FindPattern("qt5gui.dll", pattern, 0);
    //printf("loc:%lX\n", (DWORD_PTR)loc);
    //loc += 0x3d;

    //$ - 6              00 | 8B83 80010000 | mov eax, dword ptr ds : [rbx + 180] |
    //$ ==>              00 | 44 : 887C24 28 | mov byte ptr ss : [rsp + 28] , r15b |
    //$ + 5              00 | 894424 20 | mov dword ptr ss : [rsp + 20] , eax |
    // 48 8B D8 48 85 C0 74 66 8B 8C 24
    std::vector<char> pattern = vectorFromString("44 88 7C 24 28 89 44 24 20");
    //std::vector<char> pattern = vectorFromString("48 85 C9 74 25 45 33 C0 48 8B D3 48 8B 5C 24 50");
                                                //48 85 C9 74 25 45 33 C0 48 8B D3 48 8B 5C 24 50
    DWORD_PTR loc = FindPattern("gameassembly.dll", pattern, 0);
    loc -= 0x6;

    //std::vector<char> pattern = vectorFromString("45 33 C9 C7 44 24 20 01 00 00 00 45 33 C0");
    //DWORD_PTR loc = FindPattern("unityplayer.dll", pattern, 0);
    //printf("loc:%lX\n", (unsigned long)loc);

    //printf("loc:%lX\n", (unsigned long)loc);

    if (loc < 10) return 0;
    printf("hook 1 at: %x\n", loc);

    HookFunction(loc, (uintptr_t)hook, 15);

    //C1 E8 10 44 69 C0 A7 41 00 00 0F B7 C1 69 C8 A7 41 00 00
    pattern = vectorFromString("C1 E8 10 44 69 C0 A7 41 00 00 0F B7 C1 69 C8 A7 41 00 00");
    loc = FindPattern("gameassembly.dll", pattern, 0);
    if (loc < 10) return 0;
    printf("hook 2 at: %x\n", loc);

    HookFunction2(loc, (uintptr_t)hook2, 19);

    return 0;
}

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
                     )
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
        CloseHandle(CreateThread(nullptr, 0, (LPTHREAD_START_ROUTINE)InitiateHooks, hModule, 0, nullptr));
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}
