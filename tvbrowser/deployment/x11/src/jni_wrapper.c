/***************************************************************************
 *   Copyright (C) 2005 by Stefan Walkner                                  *
 *   walkner.stefan@sbg.at                                                 *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

#include <stdio.h>

#include <jni.h>

#include "eclipse_java/com_gc_systray_X11SystrayManager.h"
#include "x11_systray_window.h"

#include "logger/logger.h"

/**
 * this is THE systray struct we use
 * to store all needed infos about our systray window
 */
SystemTray sysTray;

JNIEXPORT jboolean JNICALL Java_com_gc_systray_X11SystrayManager_nativeDestroyTrayIcon(JNIEnv *env, jobject manager)
{
    logDebug(" EXECUTING nativeDestroy icon: something todo?");
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_gc_systray_X11SystrayManager_nativeHideTrayIcon(JNIEnv *env, jobject manager)
{
    logDebug(" EXECUTING nativeHideTrayIcon\n");
    if ( hideSystrayWindow(&sysTray) > 0 )
    {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gc_systray_X11SystrayManager_nativeShowTrayIcon(JNIEnv *env, jobject manager, jobject component)
{
    logDebug(" EXECUTING nativeShowTrayIcon\n");
    if ( showSystrayWindow(env, &manager, &component, &sysTray) > 0 )
    {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
