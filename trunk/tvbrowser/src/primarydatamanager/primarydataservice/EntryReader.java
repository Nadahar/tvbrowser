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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */


package primarydatamanager.primarydataservice;

import java.io.*;


import primarydatamanager.primarydataservice.util.Entities;

public class EntryReader {
 
  public BufferedReader mReader;
 
  public EntryReader(BufferedReader reader) {
    mReader=reader;    
  }
  
  private void replace(StringBuffer buf, String s, char c) {
       int inx;
       inx=buf.indexOf(s);
       while (inx>=0) {
         buf.replace(inx,inx+s.length(),""+c);
         inx=buf.indexOf(s);
       }    
     }
  
   
  
  private String replaceEntity(String line, int from, int to) {
    String entity=line.substring(from,to+1);
    StringBuffer newLine=new StringBuffer(line);
    newLine.replace(from,to+1,Entities.decode(entity));
    return newLine.toString();
  }
  
  private String convertString(String line) {
    //String oldLine=line;
    int from=0;
    while (from<line.length()) {
      
      if (line.charAt(from)=='&') {
        int to=from+1;
        while (to<line.length()) {
          if (line.charAt(to)==';') {
            line=replaceEntity(line,from,to);
            break;
          }
          to++;
        }        
      }
      from++;
      
    }
    return line;
     }
  
  public Entry next() throws IOException {
    
    int style;
    String line;
    line=mReader.readLine();
    if (line==null) return null;
    try {
      style=Integer.parseInt(line);
    }catch(NumberFormatException e) {
      return null;
    }    
    line=mReader.readLine();  
    if (line==null) return null;
    return new Entry(style,convertString(line));   
    
  }
  
 
  
  public void close() {
    try {
      mReader.close();
    }catch(IOException e) {}
  }
  
}