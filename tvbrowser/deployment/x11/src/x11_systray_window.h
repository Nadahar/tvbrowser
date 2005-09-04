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

#ifndef X11_SYSTRAY_WINDOW_H
#define X11_SYSTRAY_WINDOW_H

    #include <X11/Xlib.h>
    #include <jni.h>
    
    /**
    * this struct is used to provide all necessary information
    * about our system tray window
    */
    typedef struct
    {
        Display *display;
        Window window;
        Window managerWindow;
    } SystemTray;

    /* these are the two "public" functions that may be called from "outside" */
    int showSystrayWindow(JNIEnv *env, jobject *manager, jobject *component, SystemTray *sysTray);
    int hideSystrayWindow(SystemTray *sysTray);

#endif
