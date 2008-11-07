/*
Quadmore JAVA to SAPI bridge
Version 3, um, alpha
Copyright 2004 Bert Szoghy
webmaster@quadmore.com
special thanks for the code review by an aghast Stefan Jetchick
(he is not to blame)
*/
#pragma once

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "sphelper.h"
#include "QuadmoreTTS.h"
#include <sapi.h>
#include <stdio.h>
#include <iostream>
#include <string.h>
#include <atlbase.h>
#include "resource.h"
#include "quadinit.h"

using namespace std;

static QUADSAPI::SetUpQUADSAPI setupQuadSapi;

//Yes I know this is not pretty:
CString strVoiceSelected = "";

HRESULT hr = E_FAIL;
QUADSAPI quadsapi;

QUADSAPI::SetUpQUADSAPI::SetUpQUADSAPI()
{
	//AfxMessageBox("inside SetUpQUADSAPI creator");
	hr = ::CoInitialize(NULL);
}

QUADSAPI::SetUpQUADSAPI::~SetUpQUADSAPI()
{
	//AfxMessageBox("inside SetUpQUADSAPI destructor");
	::CoUninitialize();
}

void QUADSAPI::UseQUADSAPI()
{
	//AfxMessageBox("inside UseQUADSAPI()");
}

class CQuadTTSApp : public CWinApp
{
public:
	CQuadTTSApp();

public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};


JNIEXPORT jboolean JNICALL Java_speechplugin_quadmore_QuadmoreTTS_setVoiceToken(JNIEnv *env,jobject obj,jstring strVoiceToken)
{
	const char *str = env->GetStringUTFChars(strVoiceToken, 0);
	if (str == NULL)
	{
		//Clean up, required:
		env->ReleaseStringUTFChars(strVoiceToken, str);
		return FALSE; /* OutOfMemoryError already thrown */
	}
	else
	{
		//We got a string!
		//Assign this to a global variable which will be checked by the SpeakDarling
		//method before speaking and used if not blank
		strVoiceSelected.Format("%s",str);
	}
	//Clean up, required:
	env->ReleaseStringUTFChars(strVoiceToken, str);
	return TRUE;
}


JNIEXPORT jboolean JNICALL Java_speechplugin_quadmore_QuadmoreTTS_SpeakDarling(JNIEnv *env,jobject obj,jstring strInput)
{
	/*
		Don't blame the JNI book by Liang!

		For C :
		-------
		const char *str = (*env)->GetStringUTFChars(env, prompt, 0);
		(*env)->ReleaseStringUTFChars(env, prompt, str);

		For C++ :
		-------
		const char *str = env->GetStringUTFChars(prompt, 0);
		env->ReleaseStringUTFChars(prompt, str);
	*/

	cout << "\nMicrosoft SAPI about to start reading...\n(time to open the PC speakers)";

	bool blnStatus = true;
	const char *str = env->GetStringUTFChars(strInput, 0);
	if (str == NULL)
	{
		//Clean up, required:
		env->ReleaseStringUTFChars(strInput, str);
		blnStatus = false; /* OutOfMemoryError already thrown */
	}
	else
	{
		//We got a string!
		ISpVoice * pVoice = NULL;

		if(FAILED(hr))
		{
			//Clean up, required:
			env->ReleaseStringUTFChars(strInput, str);
			blnStatus = false;
		}
		else
		{
			HRESULT hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void **)&pVoice);
			if(SUCCEEDED(hr))
			{
				CString InputArgumentText;
				InputArgumentText.Format("%s",str);

				//Check the global variable storing the voice selected via setVoiceToken
				if(strVoiceSelected == "")
				{
					//Default: no voice selected specifically, use default
					hr = pVoice->Speak(InputArgumentText.AllocSysString(),0,NULL);
				}
				else
				{
					//Use whatever voice is selected
					HRESULT hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void **)&pVoice);
					if(SUCCEEDED(hr))
					{
						WCHAR   **m_ppszTokenIds;
						USES_CONVERSION;
						CComPtr<IEnumSpObjectTokens>    cpEnum;
						CSpDynamicString*				szDescription;
						ISpObjectToken                  *pToken = NULL;
						CComPtr<ISpObjectToken>         cpVoiceToken; //the token is the voice
						CComPtr<ISpVoice>               cpVoice;
						ULONG                           ulCount = 0;

						// Create the SAPI TTS
						hr = cpVoice.CoCreateInstance( CLSID_SpVoice );

						if( SUCCEEDED(hr) )
						{
							WCHAR *pszCurTokenId = NULL;
							ULONG ulIndex = 0, ulNumTokens = 0, ulCurToken = -1;

							hr = SpEnumTokens(SPCAT_VOICES, NULL, NULL, &cpEnum);

							if (hr == S_OK)
							{
								hr = cpEnum->GetCount( &ulNumTokens );

								if (SUCCEEDED(hr) && ulNumTokens != 0)
								{
									szDescription = new CSpDynamicString [ulNumTokens];
									m_ppszTokenIds = new WCHAR* [ulNumTokens];

									ZeroMemory(m_ppszTokenIds, ulNumTokens * sizeof(WCHAR *));

									//Unfortunately, need to loop through all available voices to find the voice
									//that matches the one selected
									UINT i =0;
									while (cpEnum->Next(1, &pToken, NULL) == S_OK)
									{
										hr = SpGetDescription(pToken, &szDescription[ulIndex]);
										ulIndex++;

										if(SUCCEEDED(hr))
										{
											hr = cpVoice->SetVoice(pToken);

											//Resist the urge to add parentheses in the next line, it won't compile
											if(CString(szDescription[i]) == strVoiceSelected)
											{
												if(SUCCEEDED(hr))
												{
													hr = cpVoice->Speak(InputArgumentText.AllocSysString(),SPF_DEFAULT,NULL);
													//break out of the loop, no need to cycle through
													//the remaining voice tokens:
													break;

													/*
														For the above line, here is a snippet of a code review discussion,
														from Stefan Jetchick to Bert Szoghy:

														(Stefan writes)
														I didn't mean to say
														you didn't know what your own loop was doing! Of course you
														knew!

														I meant: it was hard to see what the target of the goto was
														(a "break" is a disguised goto). I remember you thinking twice
														before finding where the break went. I didn't see it right away
														either.

														But that is a moot point. I was thinking about this whole "break"
														thing, and wondering why, in all those years of professional
														C++ programming, I had never done that.

														And then the obvious hit me: I never used "break", because
														I used C++, not C.

														The more you use C++, the more you rely on standard algorithms
														to manipulate standard data structures (i.e. supplied by
														the C++ language).

														In C, you have to "roll your own" constantly.

														Of course, I'm sure there are cases where you can't use
														the C++ standard library, but that rarely happens.

														Here is a real, honest-to-God production code sample
														that illustrates what I'm talking about:

														// Solenoid valves #23, 24, 25, and 26.
														else if(std::find(m_solenoids_changers.begin(),
														m_solenoids_changers.end(), pDeviceProxy) !=
														m_solenoids_changers.end())
														{
														parallelWiredSolenoids = m_solenoids_changers;
														}

														Look Ma! No "break" statement!
													*/
												}
											}

											pToken->Release();
											pToken = NULL;
											i++;
										}
										else
										{
											blnStatus = false;
										}
									}

									/*
										The destruction of the CSpDynamicString pointer szDescription
										seems to destroy an array of objects. Taken verbatim from the
										SAPI code sample. Am blindly trusting Microsoft.
									*/
									delete [] szDescription;
								}
								else
								{
									blnStatus = false;
								}
							}
							else
							{
								blnStatus = false;
							}
						}
						else
						{
							blnStatus = false;
						}
					}
				}

				pVoice->Release();
				pVoice = NULL;
			}
			else
			{
				blnStatus = false;
			}
		}
	}

	//Clean up, required:
	env->ReleaseStringUTFChars(strInput, str);
	return blnStatus;
}


JNIEXPORT jstring JNICALL Java_speechplugin_quadmore_QuadmoreTTS_getVoiceToken(JNIEnv *env, jobject obj)
{
	CString strConcatenateXML = "<?xml version=\"1.0\"?>";
	int intLenght;

	ISpVoice * pVoice = NULL;

	HRESULT hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void **)&pVoice);
	if(SUCCEEDED(hr))
	{
		WCHAR   **m_ppszTokenIds;
		USES_CONVERSION;
		CComPtr<IEnumSpObjectTokens>    cpEnum;
		CSpDynamicString*				szDescription;
		ISpObjectToken                  *pToken = NULL;
		CComPtr<ISpObjectToken>         cpVoiceToken; //the token is the voice
		CComPtr<ISpVoice>               cpVoice;
		ULONG                           ulCount = 0;

		if(SUCCEEDED(hr))
		{
			hr = cpVoice.CoCreateInstance( CLSID_SpVoice );

			if(SUCCEEDED(hr))
			{
				WCHAR *pszCurTokenId = NULL;
				ULONG ulIndex = 0, ulNumTokens = 0, ulCurToken = -1;

				hr = SpEnumTokens(SPCAT_VOICES, NULL, NULL, &cpEnum);

				if (hr == S_OK)
				{
					hr = cpEnum->GetCount( &ulNumTokens );

					if (SUCCEEDED(hr) && ulNumTokens != 0)
					{
						szDescription = new CSpDynamicString [ulNumTokens];
						m_ppszTokenIds = new WCHAR* [ulNumTokens];

						ZeroMemory(m_ppszTokenIds, ulNumTokens * sizeof(WCHAR *));

						UINT i =0;
						while (cpEnum->Next(1, &pToken, NULL) == S_OK)
						{
							//Don't care about return value in next line:
							hr = SpGetDescription(pToken, &szDescription[ulIndex]);
							ulIndex++;

							strConcatenateXML += "<voice>" + CString(szDescription[i]) + "</voice>";

							pToken->Release();
							pToken = NULL;
							i++;
						}

						delete [] szDescription;
					}
					else
					{
						strConcatenateXML = "No voice found. (5)";
					}
				}
				else
				{
					strConcatenateXML = "No voice found. (4)";
				}
			}
			else
			{
				strConcatenateXML = "No voice found. (3)";
			}
		}
		else
		{
			strConcatenateXML = "No voice found. (2)";
		}
	}
	else
	{
		strConcatenateXML = "No voice found. (1)";
	}

	intLenght = strConcatenateXML.GetLength();
	char* buf = strConcatenateXML.GetBuffer(intLenght + 1);
	strConcatenateXML.ReleaseBuffer();
	return env->NewStringUTF(buf);
}
