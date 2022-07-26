#include <iostream>
#include <Windows.h>
#include <tlhelp32.h>
#include <Psapi.h>
#include <vector>
#define READABLE (PAGE_EXECUTE_READ | PAGE_EXECUTE_READWRITE | PAGE_EXECUTE_WRITECOPY | PAGE_READONLY | PAGE_READWRITE | PAGE_WRITECOPY)

void MsgBoxAddy(DWORD num)
{
	wchar_t szBuffer[1024];
	swprintf_s(szBuffer, 1024, L"Num: %x", num);
	//	MessageBox(NULL, szBuffer, L"Title", MB_OK);
}

int char2int(const char* s, int i)
{
	int sl = (int)strlen(s);
	if (i >= sl) return -1;
	char input = s[i];
	if (input >= '0' && input <= '9')
		return input - '0';
	if (input >= 'A' && input <= 'F')
		return input - 'A' + 10;
	if (input >= 'a' && input <= 'f')
		return input - 'a' + 10;
	if (input == ' ')
		return -1;
	throw std::invalid_argument("Invalid input string");
}

std::vector<char> vectorFromString(const char* s) {
	int sl = (int)strlen(s);
	std::vector<char> vec;

	int num = 0;
	for (int j = 0; j <= sl; j++)
	{
		int dig = char2int(s, j);

		if (dig == -1)
		{
			vec.push_back(num);
			num = 0;
		}
		else
		{
			int mul = 1;
			if (char2int(s, j + 1) != -1) mul = 16;
			num += dig * mul;
		}
	}

	return vec;
}

MODULEINFO GetModuleInfo(const char* szModule)
{
	MODULEINFO modinfo = { 0 };
	HMODULE hModule = GetModuleHandleA(szModule);
	if (hModule == 0)
		return modinfo;
	GetModuleInformation(GetCurrentProcess(), hModule, &modinfo, sizeof(MODULEINFO));
	return modinfo;
}

void WriteToMemory(uintptr_t addressToWrite, char* valueToWrite, int byteNum)
{
	//used to change our file access type, stores the old
	//access type and restores it after memory is written
	unsigned long OldProtection;
	//give that address read and write permissions and store the old permissions at oldProtection
	VirtualProtect((LPVOID)(addressToWrite), byteNum, PAGE_EXECUTE_READWRITE, &OldProtection);

	//write the memory into the program and overwrite previous value
	memcpy((LPVOID)addressToWrite, valueToWrite, byteNum);

	//reset the permissions of the address back to oldProtection after writting memory
	VirtualProtect((LPVOID)(addressToWrite), byteNum, OldProtection, NULL);
}

//MEMORY_BASIC_INFORMATION mbi;
//DWORD patternLength = (DWORD)strlen(pattern);

//for (BYTE* addr = 0; VirtualQuery(addr, &mbi, sizeof(mbi)); addr = reinterpret_cast<BYTE*>(mbi.BaseAddress) + mbi.RegionSize)
//{
//	if (mbi.State & MEM_COMMIT)
//	{
//		if (mbi.AllocationProtect & READABLE)
//		{
//			for (BYTE* i = addr; i < addr + mbi.RegionSize; ++i)
//			{
//				//std::cout << "0x" << reinterpret_cast<void*>(i);
//				//std::cout << "\t" << static_cast<unsigned>(*i);
//				//std::cout << std::endl;
//				bool found = true;
//				for (DWORD j = 0; found && j < patternLength; j++)
//				{
//					DWORD k = (DWORD)i + (DWORD)j;
//					found &= pattern[j] == *(char*)k;
//				}
//				if (found)
//				{
//					return *i;
//				}
//			}
//		}
//	}
//}
//return 0;

//DWORD_PTR getRelativeCall(DWORD_PTR loc)
//{
//	char pattern[5];
//
//	for (DWORD_PTR j = 0; j < 5; j++)
//	{
//		pattern[j] = *(char*)(loc + j);
//	}
//}

DWORD_PTR FindPattern(const char* module, std::vector<char> pattern, int ordinal)
{
	MODULEINFO modinfo = { 0 };
	HMODULE hModule = GetModuleHandleA(module);
	HANDLE hProc = (HANDLE)hModule;
	if (hModule == 0) return 0;

	GetModuleInformation(GetCurrentProcess(), hModule, &modinfo, sizeof(MODULEINFO));

	DWORD_PTR base = (DWORD_PTR)modinfo.lpBaseOfDll;
	DWORD_PTR size = (DWORD_PTR)modinfo.SizeOfImage;
	
	//printf("base:%lX size:%lX patternsize:%d\n", base, size, pattern.size());

	//printf("pattern:");
	//for (DWORD j = 0; j < pattern.size(); j++)
	//{
	//	printf("%02X ", (unsigned char)pattern[j]);
	//}
	//printf("\n");

	int ordinalCounter = 0;
	size += base;
	for (DWORD_PTR i = base; i < size; i++)
	{
		bool found = true;
		for (DWORD_PTR j = 0; found && j < pattern.size(); j++)
		{
			found &= pattern[j] == *(char*)(i + j);
		}

		if (found)
		{
			//printf("ordinal:%lX   %d\n", (unsigned long)i, ordinalCounter);
			if (ordinalCounter == ordinal)
			{
				return (DWORD_PTR)i;
			}
			ordinalCounter++;
		}
	}
	return 1;
}

uint8_t SetupJumpInstructionBytes(uint8_t* jmpInstruction, DWORD_PTR jumpStartAddress, DWORD_PTR addressToJumpTo, char type, char prepostfix)
{
	const DWORD_PTR x86FixedJumpSize = 5;
	DWORD_PTR relativeAddress = addressToJumpTo - jumpStartAddress - x86FixedJumpSize;
	if(relativeAddress <= 0x7FFFFFFF)
	{
		jmpInstruction[0] = type;
		*(uintptr_t*)&jmpInstruction[1] = relativeAddress;
		return 5;
	}
	else
	{
		char j = 0;
		if(prepostfix == 1)
		{
			jmpInstruction[0] = 0x50;
			j = 1;
		}
		jmpInstruction[0 + j] = 0x48;
		jmpInstruction[1 + j] = 0xB8;
		for(int i = 0; i < 8; i++)
		{
			jmpInstruction[2 + i + j] = addressToJumpTo & 0xFF;
			addressToJumpTo >>= 8;
		}
		jmpInstruction[10 + j] = 0xFF;
		if (type == (char)0xE9)
		{
			jmpInstruction[11 + j] = 0xE0;
		}
		else if (type == (char)0xE8)
		{
			jmpInstruction[11 + j] = 0xD0;
		}
		return 12 + j;
	}
}

void PlaceJumpToAddress(uintptr_t installAddress, uintptr_t addressToJumpTo, uint32_t hookSize)
{
	uint8_t jmpInstruction[14];
	uint8_t nop[45];
	std::fill(nop, nop + sizeof(nop), 0x90);

	uint8_t targetSize = SetupJumpInstructionBytes(jmpInstruction, installAddress, addressToJumpTo, 0xE9, 1);

	DWORD dwProtect[2];
	VirtualProtect((void*)installAddress, hookSize, PAGE_EXECUTE_READWRITE, &dwProtect[0]);

	memcpy((void*)installAddress, jmpInstruction, targetSize);
	if(hookSize - targetSize != 0)
	{
		memcpy((void*)(installAddress + targetSize), nop, hookSize - targetSize);
	}

	memset((void*)(installAddress + hookSize - 1), 0x58, 1);
	VirtualProtect((void*)installAddress, hookSize, dwProtect[0], &dwProtect[1]);
}