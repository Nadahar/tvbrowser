/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * PrintPlugin
 * Copyright (C) 08-2003 Robert Inzinger (yxterwd@users.sourceforge.net)
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

package printplugin;

import java.awt.*;
import java.util.Iterator;
import devplugin.*;
import java.util.ArrayList;
import tvbrowser.ui.programtable.*;
import javax.swing.*;

/**
 * Print Channel information.
 *
 * @author Robert Inzinger
 */

public class PrintFilter
{
   private String mFontName;
   private int mFontSize;
   private int mProgramFontSize;
   private ArrayList mProgramList;
   private ProgramFilter mProgramFilter;
   private int mProgramCounter;

   public PrintFilter(String fontName, int fontSize, int programFontSize, ProgramFilter programFilter)
   {
      mFontName = fontName;
      mFontSize = fontSize;
      mProgramFontSize = programFontSize;
      mProgramFilter = programFilter;
      mProgramCounter = 0;
   }

   public int readPrograms(Date fromDate, Date untilDate)
   {
      Date countDate;

      mProgramList = new ArrayList();
      countDate = new Date(fromDate);

      Channel [] channels = Plugin.getPluginManager().getSubscribedChannels();

      if (channels.length > 0)
      {

         while(countDate.getValue() <= untilDate.getValue())
         {
            for(int i = 0; i < channels.length; i++)
            {
               Iterator programIter = Plugin.getPluginManager().getChannelDayProgram(countDate, channels[i]);

               if (programIter != null)
               {
                  while (programIter.hasNext())
                  {
                     Program prog = (Program) programIter.next();

                     if (mProgramFilter.accept(prog))
                     {
                        addProgramToList(prog);
                        mProgramCounter++;
                     }
                  }
               }
            }
            countDate = countDate.addDays(1);
         }
      }
/*
      for (int i = 0; i < mProgramList.size(); i++)
      {
         JOptionPane.showMessageDialog(null, ((Program)mProgramList.get(i)).getTitle());
      }
*/

      return mProgramCounter;
   }

   private void addProgramToList(Program program)
   {
//      JOptionPane.showMessageDialog(null, program.getTitle());

      if (mProgramList.size() == 0)
      {
         mProgramList.add(program);
         return;
      }
      for (int i = 0; i < mProgramList.size(); i++)
      {
         Program pl = (Program) mProgramList.get(i);

         if (program.getDate().getValue() < pl.getDate().getValue())
         {
            mProgramList.add(i, program);
            return;
         }
         else if (program.getDate().getValue() == pl.getDate().getValue())
         {
            if (program.getHours() < pl.getHours())
            {
               mProgramList.add(i, program);
               return;
            }
            else if (program.getHours() == pl.getHours())
            {
               if (program.getMinutes() < pl.getMinutes())
               {
                  mProgramList.add(i, program);
                  return;
               }
            }
         }
      }

      mProgramList.add(program);
      return;
   }

   public int draw (int x , int y, int width, int height, Graphics g, int programIndex)
   {
      int programY, pi;

      pi = programIndex;

      Font font = new Font(mFontName, Font.BOLD, mFontSize);

      if (pi < mProgramList.size())
      {

         programY = y + mFontSize;

         Date countDate = new Date(((Program)mProgramList.get(pi)).getDate());

         g.setFont(font);
         g.drawString(countDate.toString(), x, programY);
         programY += font.getSize() + font.getSize() * 0.4;

         while (pi < mProgramList.size())
         {
            Program p = (Program) mProgramList.get(pi);
            if (countDate.getValue() < p.getDate().getValue())
            {
               countDate = new Date(p.getDate());
               g.setFont(font);
               g.drawString(countDate.toString(), x, programY);
               programY += font.getSize() + font.getSize() * 0.4;
            }

            PrintProgram pp = new PrintProgram(p, mFontName, mProgramFontSize, width, g);
            pp.setChannel(true);
            pp.setMaxInfo(true);

            if (programY + pp.getHeight() > y + height)
               break;

            programY = pp.draw(x, programY);

            pi++;
         }
      }

      return pi;
   }
}
