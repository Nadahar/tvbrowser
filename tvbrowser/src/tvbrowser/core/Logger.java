/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package tvbrowser.core;
import java.io.*;

/**
 * A basic class for do some logging - not fully implemented yet.
 */


public class Logger {

  private static final int FILE=1, STDOUT=2, OFF=0;
  private static final int mode=2;
  private static PrintWriter out;
  private static String curCaller;
  static {

    if (mode==STDOUT) {
      out=new PrintWriter(System.out);
    }
    else {
      try {
        out=new PrintWriter(new FileOutputStream("logging.dat"));
      }catch(IOException e) {
        e.printStackTrace();
      }
    }
  }
  public static void exception(Exception e) {
    e.printStackTrace(out);
  }

  public static void message(String caller, String msg) {

    if (!caller.equals(curCaller)) {
       out.println(caller);
       curCaller=caller;
    }
    out.println("   "+msg);

  }

}