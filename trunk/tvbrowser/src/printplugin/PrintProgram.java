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
import devplugin.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Print Program information.
 *
 * @author Robert Inzinger
 */

public class PrintProgram
{
   private Program mProgram;
   private int mMaxTextWidth, mMaxWidth, mTitleheight, mInfoheight, mInfoDetailheight;
   private String mStartTime;
   private ArrayList mTitle;
   private ArrayList mInfo;
   private ArrayList mInfoDetail;
   private Graphics mg;
   private Font mFontTitle, mFontInfo;
   private boolean mCompact;
   private boolean mChannel;
   private boolean mMaxInfo;

   public PrintProgram(Program program, String fontName, int fontSize, int maxWidth, Graphics g)
   {
      mProgram = program;
      mMaxWidth = maxWidth;
      mg = g;
      mInfoheight = 0;
      mInfoDetailheight = 0;
      mCompact = false;
      mChannel = false;
      mMaxInfo = false;

      mFontTitle = new Font(fontName, Font.BOLD, fontSize);
      mFontInfo  = new Font(fontName, Font.PLAIN, fontSize);

      mTitle = new ArrayList();
      mInfo = new ArrayList();
      mInfoDetail = new ArrayList();

      mStartTime = mProgram.getTimeString();
      mMaxTextWidth = mMaxWidth - getMaxStartTimeWidth(mFontTitle);

      splitStringtoList(mTitle, mProgram.getTitle(), mFontTitle);
      mTitleheight = (int) ((mFontTitle.getSize() + mFontTitle.getSize() * 0.4) * mTitle.size());

      splitStringtoList(mInfo, mProgram.getShortInfo(), mFontInfo);
      mInfoheight = (int) ((mFontInfo.getSize() + mFontInfo.getSize() * 0.4) * mInfo.size());

      splitStringtoList(mInfoDetail, mProgram.getDescription(), mFontInfo);
      mInfoDetailheight = (int) ((mFontInfo.getSize() + mFontInfo.getSize() * 0.4) * mInfoDetail.size());
   }

   public void setCompact(boolean compact)
   {
      mCompact = compact;
   }

   public void setChannel(boolean channel)
   {
      mChannel = channel;
   }

   public void setMaxInfo(boolean maxInfo)
   {
      mMaxInfo = maxInfo;
   }

   public int draw(int x, int y)
   {
      int i, ypos, textX;
      ypos = y;
      textX = x + getMaxStartTimeWidth(mFontTitle);

      mg.setFont(mFontTitle);

      mg.drawString(mStartTime, x, ypos);

      if (mChannel)
      {
         mg.drawString((String) mProgram.getChannel().getName(), textX, ypos);
         ypos = (int)(ypos + mFontTitle.getSize() + mFontTitle.getSize() * 0.4);
      }

      for (i = 0; i < mTitle.size(); i++)
      {
         mg.drawString((String) mTitle.get(i), textX, ypos);
         ypos = (int)(ypos + mFontTitle.getSize() + mFontTitle.getSize() * 0.4);
      }

      if (!mCompact)
      {
         if (mMaxInfo)
         {
            mg.setFont(mFontInfo);
            for (i = 0; i < mInfoDetail.size(); i++)
            {
               mg.drawString((String) mInfoDetail.get(i), textX, ypos);
               ypos = (int)(ypos + mFontInfo.getSize() + mFontInfo.getSize() * 0.4);
            }
         }
         else
         {
            mg.setFont(mFontInfo);
            for (i = 0; i < mInfo.size(); i++)
            {
               mg.drawString((String) mInfo.get(i), textX, ypos);
               ypos = (int)(ypos + mFontInfo.getSize() + mFontInfo.getSize() * 0.4);
            }
         }
      }
      return ypos;
   }

   public int getHeight()
   {
      if (mCompact)
      {
         return mTitleheight;
      }
      else
      {
         if (mMaxInfo)
            return mTitleheight + mInfoDetailheight;
         else
            return mTitleheight + mInfoheight;
      }
   }
   public Program getProgram()
   {
      return mProgram;
   }

   private void splitStringtoList(ArrayList arrayList, String str, Font font)
   {
      StringTokenizer strtoken = new StringTokenizer(str);
      String line;
      FontMetrics metrics = mg.getFontMetrics(font);

      line = "";
      arrayList.clear();

      while (strtoken.hasMoreTokens())
      {
         String nextToken = strtoken.nextToken();

         if (metrics.stringWidth(line + " " + nextToken) < mMaxTextWidth)
         {
            if (line.length() == 0)
               line = nextToken;
            else
               line += " " + nextToken;
         }
         else
         {
            if (line.length() > 0)
            {
               arrayList.add(line);
               line = nextToken;
            }
         }
      }
      if (line.length() > 0)
      {
         arrayList.add(line);
      }

   }
   private int getMaxStartTimeWidth(Font font)
   {
      FontMetrics metrics = mg.getFontMetrics(font);
      return metrics.stringWidth("88:88 ");
   }
}
