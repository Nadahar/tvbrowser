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

package arddigitaldataservice; 


import java.io.*;


public class RtfFilterReader extends FilterReader {
  
  private int depth=0;
	private static int DEPTH=2;
	
  public RtfFilterReader (Reader in) {
    super(in);
    depth=0;
		
    
  }

  public boolean markSupported () {
    return false;
  }
 
  public int read(char[] cbuf, int off, int len) throws IOException {
    
    int in=read();
    if (in==-1) {
      return -1;
    }
    cbuf[off]=(char)in;
    for (int i=1;i<len;i++) {
      in=read();
      cbuf[off+i]=(char)in;
      if (in==-1) {
        return i;
      }      
    }
    return len;
    
  }
  
  private int ignoreGroup(int ch) throws IOException {
    
    while (ch!=-1 && ch!='{') {
      ch=in.read();
      
    }
    int depth=1;
    while (ch!=-1 && depth>0) {
          ch=in.read();
          if (ch=='{') {
            depth++;
          }
          else if (ch=='}') {
            depth--;
          }
    }
    return ch;
    
  }



	public int read() throws IOException {

		int ch=getNext();

		while (ch=='\\' && ch!=-1) {

      StringBuffer buf=new StringBuffer();

			ch=getNext();

			if (ch=='\'') {
				buf.append((char)getNext());
				buf.append((char)getNext());
				String specChar=buf.toString();
				if (specChar.equals("fc")) {
					
					return '\u00fc';
				}
			}
			else {
				do {
					buf.append((char)ch);
					ch=getNext();
				}while (ch!=-1 && ch!=' ');

				String specChar=buf.toString();
				if (specChar.equals("tab")) {
					return '\t';
				}
				if (specChar.equals("par")) {
					return '\n';
				}
			}	
			
			ch=getNext();
		}
		return ch;
}

	
	private int getNext() throws IOException {

	//	int ch=in.read();
		int ch;
		do {
		//while (depth!=DEPTH && ch!=-1) {
			do {
			ch=in.read();
			if (ch=='{') {
				depth++;
		//		ch=in.read();
			}
			else if (ch=='}') {
				depth--;
			//	ch=in.read();
			}

			/*
			while (depth==DEPTH && ch=='\\') {
				do {
					ch=in.read();
				} while (ch!=' ' && ch!=-1);
				ch=in.read();
			}
*/
			/*if (depth==DEPTH && ch=='\\') {
		 		do {
		 			ch=in.read();
		 		}while (ch!=' ' && ch!=-1);

			}*/
		
			}while (ch=='{' || ch=='}');


			
	
			
			
		//	ch=in.read();
	//	}
		}while (depth!=DEPTH && ch!=-1);
		return ch;
				
	

		
		
	}
	
  public int readOLD() throws IOException {
    
    int ch=in.read();
  
    for(;;) {
      if (ch=='{' || ch=='}') {
        ch=in.read();
        continue;
      }
			//return ch;

			while (ch=='\\') {
				ch=in.read();

				while (ch!=-1 && ch!=' ' && ch!='{') {
					ch=in.read();
				}
				if (ch=='{') {
					ch=in.read();
				}
			}
				
				return ch;
				
			
			
			
  /*  if (ch=='\\') {
      
      ch=in.read();
     
     // if (ch=='*') {  // ignore
     //   ch=ignoreGroup('{');
     //   continue;
     // }
      
      StringBuffer buf=new StringBuffer();
      
      do {
        buf.append((char)ch);
        ch=in.read();
        if (ch=='{') {
          ch=ignoreGroup(ch);
        }
      }while (ch!=-1 && ch!=' '); 
      String specialChar=buf.toString();
      
      if (ch==-1) {
        return -1;
      }
      
      if (specialChar.equals("tab")) {
        return '\t';
      }
      
      if (specialChar.equals("par")) {
        return '\n';
      }
      
      else {  
        ch=in.read();
        continue;
      }
    }
    else { // plain
       return ch;
    }*/
    }
    
  }
  
  public boolean ready () {
    return true;
  }

}