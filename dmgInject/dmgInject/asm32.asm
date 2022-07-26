.data

.code
ALIGN 8

asm_func PROC
	mov eax, dword ptr ds : [ebp]
	mov eax, dword ptr ds : [eax + 8]
asm_func ENDP

END