// CProcessHandler.cpp : Main entry point of the DLL
//

#include "stdafx.h"



BOOL APIENTRY DllMain( HMODULE hModule,
					  DWORD  ul_reason_for_call,
					  LPVOID lpReserved
					  )
{

	//OutputDebugString(L"ProcessHandler DLL loaded");
	return TRUE;
}


JNIEXPORT jboolean JNICALL Java_dvbplugin_dvbviewer_ProcessHandler_isActive
(JNIEnv *env, jobject obj, jstring string){
	LPCWSTR basename = (LPCWSTR)env->GetStringChars(string, 0);

	WCHAR message[256];
	StringCbPrintf(message, sizeof(message),
				   L"ProcessHandler_isActive: Searching for process named '%s'...",
				   basename);
	OutputDebugString(message);

	CProcessIterator itp;
	for (DWORD pid=itp.First(); pid; pid=itp.Next()) {
		// Note: first module in CProcessModuleIterator is EXE for this process
		//
		WCHAR modname[_MAX_PATH];
		CProcessModuleIterator itm(pid);
		HMODULE hModule = itm.First(); // .EXE
		if (hModule) {
			GetModuleBaseName(itm.GetProcessHandle(), hModule, modname, _MAX_PATH);
			if (0 == lstrcmpi(basename, modname)) {
				OutputDebugString(L"...found the process");
				return true;
			}
		}
	}

	return false;
}

/* unused
JNIEXPORT void JNICALL Java_dvbplugin_dvbviewer_ProcessHandler_stopProcess
(JNIEnv *env, jobject obj, jstring string){
	LPCWSTR basename = (LPCWSTR)env->GetStringChars(string, 0);

	WCHAR message[256];
	StringCbPrintf(message, sizeof(message),
				   L"ProcessHandler_stopProcess: Searching for process named '%s'...",
				   basename);
	OutputDebugString(message);

	CProcessIterator itp;
	for (DWORD pid=itp.First(); pid; pid=itp.Next()) {
		// Note: first module in CProcessModuleIterator is EXE for this process
		//
		WCHAR modname[_MAX_PATH];
		CProcessModuleIterator itm(pid);
		HMODULE hModule = itm.First(); // .EXE
		if (hModule) {
			GetModuleBaseName(itm.GetProcessHandle(), hModule, modname, _MAX_PATH);
			if (0 == lstrcmpi(basename, modname)) {
				OutputDebugString(L"ProcessHandler_stopProcess: ...found the process");

				// just get the first top-level window
				CMainWindowIterator itw(pid);
				HWND hwnd = itw.First();

				// get the process handle
				HANDLE hproc = OpenProcess(SYNCHRONIZE, FALSE, pid);

				// send the close message
				SendMessage(hwnd, WM_CLOSE, 0, 0);
				OutputDebugString(TEXT("ProcessHandler_stopProcess: Sent message WM_CLOSE, waiting for process end..."));

				// wait for the process' end
				DWORD rc = WaitForSingleObject(hproc, 15000);
				if (WAIT_OBJECT_0 == rc) {
					OutputDebugString(TEXT("ProcessHandler_stopProcess: ...process ended"));
				}
				else if (WAIT_FAILED == rc) {
					StringCbPrintf(message, sizeof(message),
								   L"ProcessHandler_stopProcess: Waiting for process end failed. ErrorCode: %x",
								   GetLastError());
					OutputDebugString(message);
				}
				else {
					StringCbPrintf(message, sizeof(message),
								   L"ProcessHandler_stopProcess: Waiting for process end returned %d",
								   rc);
					OutputDebugString(message);
				}

				// do not forget to close the handle
				CloseHandle(hproc);
			}
		}
	}
}
*/

JNIEXPORT void JNICALL Java_dvbplugin_dvbviewer_ProcessHandler_sendMessage
(JNIEnv *env, jobject obj, jstring string, jint msg, jint wparam) {
	LPCWSTR basename = (LPCWSTR)env->GetStringChars(string, 0);

	WCHAR message[256];
	StringCbPrintf(message, sizeof(message),
				   L"ProcessHandler_sendMessage: Searching for process named '%s'...",
				   basename);
	OutputDebugString(message);

	CProcessIterator itp;
	for (DWORD pid=itp.First(); pid; pid=itp.Next()) {
		// Note: first module in CProcessModuleIterator is EXE for this process
		//
		WCHAR modname[_MAX_PATH];
		CProcessModuleIterator itm(pid);
		HMODULE hModule = itm.First(); // .EXE
		if (hModule) {
			GetModuleBaseName(itm.GetProcessHandle(), hModule, modname, _MAX_PATH);
			if (0 == lstrcmpi(basename, modname)) {
				OutputDebugString(L"ProcessHandler_sendMessage: ...found the process");

				// just get the first top-level window
				CMainWindowIterator itw(pid);
				HWND hwnd = itw.First();

				// send the message
				SendMessage(hwnd, msg, wparam, 0);
				OutputDebugString(TEXT("ProcessHandler_sendMessage: Sent message"));
			}
		}
	}
}
