/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * VCS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package speechplugin.quadmore;

/* 
 * DO NOT CHANGE ANYTHING IN THIS CLASS AS THIS
 * IS A BRIDGE TO THE NATIVE WINDOWS DLL AND MUST
 * REMAIN BYTE CODE COMPLIANT FOR THE NEEDED METHODS!
 * 
 * instead change the SAPI class which is a wrapper
 * around this class
 */

public class QuadmoreTTS
{
    public static void main(String args[])
    { }
    public native boolean SpeakDarling(String strInput);
    public native boolean setVoiceToken(String strVoiceToken);
    public native String getVoiceToken();
}