// stdafx.h : include all files that are needed and can be precompiled
//

#pragma once

#define VC_EXTRALEAN						 // Exclude rarely-used stuff from headers
#include <windows.h>
#include <tchar.h>
#include <psapi.h>							 // PSAPI for EnumProcesses
#include <strsafe.h>
#include <assert.h>
#pragma warning(disable:4786)				 // disable annoying C4786
#include <string>							 // STL string class
#include <list>								 // STL list class
using namespace std;						 // use STL

#include "dvbplugin_ProcessHandler.h"
#include "EnumProc.h"
