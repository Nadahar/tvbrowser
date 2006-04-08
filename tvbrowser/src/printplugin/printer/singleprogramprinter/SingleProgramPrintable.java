/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
package printplugin.printer.singleprogramprinter;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * This is a Printable that prints a single Program-Item
 * 
 * @author bodum
 */
public class SingleProgramPrintable implements Printable {
  /** Footer-Font */
  private static final Font FOOTER_FONT = new Font("Dialog",Font.ITALIC,6);
 
  /** Print this Program */
  private Program mProgram;
  /** Use this Font */
  private Font mFont;
  /** Print only these FieldTypes */
  private ProgramFieldType[] mTypes;
  
  /**
   * Create a Printable for a single Program
   * 
   * @param program Program to print
   * @param font Font to use
   * @param types Types to print
   */
  public SingleProgramPrintable(Program program, Font font, ProgramFieldType[] types) {
    mProgram = program;
    mFont = font;
    mTypes = types;
  }

  /*
   * (non-Javadoc)
   * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
   */
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
   
    if (pageIndex < 3) {
      int x0 = (int)pageFormat.getImageableX();
      int y0 = (int)pageFormat.getImageableY();

      graphics.setFont(FOOTER_FONT);
      graphics.drawString("FOOTER", x0, y0 + (int)pageFormat.getImageableHeight()-3);

      return PAGE_EXISTS;
    }
    
    return NO_SUCH_PAGE;
  }

  /**
   * Try to find out the max. Number of Pages to Print
   *  
   * @param format Page-Format to use
   * @return Number of Pages
   */
  public int getNumOfPages(PageFormat format) {
    Graphics2D g = new DummyGraphics2D();
    int i = 0;
    try {
      while (print(g, format, i) != NO_SUCH_PAGE) {
        i++;
      }
      return i;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }
  
  
}