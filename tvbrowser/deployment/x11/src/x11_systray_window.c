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

#include <jni.h>
#include <jawt_md.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xatom.h>

#include "x11_systray_window.h"
#include "logger/logger.h"

#define SYSTEM_TRAY_REQUEST_DOCK 0

JAWT_DrawingSurface *ds;
JAWT_DrawingSurfaceInfo *dsi;
JAWT awt;
jint lock;

/**
 * some x error handling functions from:
 * https://svn.musicpd.org/kmp/trunk/trayicon_x11.cpp
 * thank you! :)
 */
static XErrorHandler old_handler = 0;
static int dock_xerror = 0;
int dock_xerrhandler(Display* dpy, XErrorEvent* err)
{
    dock_xerror = err->error_code;
    return old_handler(dpy, err);
}

static void trap_errors()
{
    dock_xerror = 0;
    old_handler = XSetErrorHandler(dock_xerrhandler);
}

static int untrap_errors()
{
    XSetErrorHandler(old_handler);
    return dock_xerror;
}

/**
 * more informations about that:
 * http://standards.freedesktop.org/systemtray-spec/0.2/ar01s04.html
 */
int sendDockRequest(SystemTray *sysTray)
{
    XClientMessageEvent ev;
    
    ev.type         = ClientMessage;
    ev.window       = sysTray->managerWindow;
    ev.message_type = XInternAtom(sysTray->display, "_NET_SYSTEM_TRAY_OPCODE", False);
    ev.format       = 32;
    ev.data.l[0]    = CurrentTime;
    ev.data.l[1]    = SYSTEM_TRAY_REQUEST_DOCK;
    ev.data.l[2]    = sysTray->window;
    ev.data.l[3]    = 0;
    ev.data.l[4]    = 0;

    trap_errors();
    
    XSendEvent(sysTray->display, sysTray->managerWindow, False, NoEventMask, (XEvent*)&ev);
    XSync(sysTray->display, False);

    if ( untrap_errors() )
    {
        return -1;
    }
    return 1;
}

/**
 * sets the Window of the system tray for the default screen
 * of the x window to the given SystemTray structure
 */
void loadManagerWindow(SystemTray *sysTray)
{
    Screen *screen;
    Window managerWindow;    
    char msgBuffer[32];
    
    int screenId;
    
    screen   = XDefaultScreenOfDisplay(sysTray->display);
    screenId = XScreenNumberOfScreen(screen);

    snprintf(msgBuffer, sizeof(msgBuffer), "_NET_SYSTEM_TRAY_S%d", screenId);

    XGrabServer(sysTray->display);
    managerWindow = XGetSelectionOwner(sysTray->display, XInternAtom(sysTray->display, msgBuffer, False));
    if ( sysTray->managerWindow != None )
    {
        XSelectInput(sysTray->display, sysTray->managerWindow, StructureNotifyMask);
    }
    XUngrabServer(sysTray->display);
    XFlush(sysTray->display);
    
    sysTray->managerWindow = managerWindow;
}

/**
 * whenever changing something belonging to our
 * drawing surface we have to lock it
 * otherwise not changes are possible
 */
static int lockDrawingSurface()
{
    lock = ds->Lock(ds);
    if ( (lock & JAWT_LOCK_ERROR) != 0 )
    {
        logError("could not lock drawing surface");
        return -1;
    }
    
    logDebug("locked drawing surface");
    return 1;
}

/**
 * unlocks the before locked drawing surface
 */
static int unlockDrawingSurface()
{
    ds->FreeDrawingSurfaceInfo(dsi);
    ds->Unlock(ds);
    logDebug("unlocked drawing surface");
    return 1;
}

int showSystrayWindow(JNIEnv *env, jobject *manager, jobject *component, SystemTray *sysTray)
{
    JAWT_X11DrawingSurfaceInfo *dsi_x11;
    
    /**
     * now we need to get the Window and Screen of our component
     */
    awt.version = JAWT_VERSION_1_4;
    if ( JAWT_GetAWT(env, &awt) == JNI_FALSE )
    {
        logError("could not get awt version");
        return -1;
    }
    
    ds = awt.GetDrawingSurface(env, *component);
    if ( ds == NULL )
    {
        logError("could not get drawing surface");
        return -1;
    }
    
    if ( lockDrawingSurface() < 0 )
    {
        return -1;
    }
    
    dsi = ds->GetDrawingSurfaceInfo(ds);
    
    dsi_x11 = (JAWT_X11DrawingSurfaceInfo*)dsi->platformInfo;

    sysTray->display = dsi_x11->display;
    sysTray->window  = dsi_x11->drawable;

    logDebug("got screen and window ^^");
    
    /**
     * and the window of the system tray area
     */
    loadManagerWindow(sysTray);

    if ( sendDockRequest(sysTray) < 0 )
    {
        return -1;
    }
    
    if ( unlockDrawingSurface() < 0 )
    {
        return -1;
    }

    return 1;
}

int hideSystrayWindow(SystemTray *sysTray)
{
    XGrabServer(sysTray->display);
    
    XDestroyWindow(sysTray->display, sysTray->window);
    
    XUngrabServer(sysTray->display);
    XFlush(sysTray->display);
    
    return 1;
}
