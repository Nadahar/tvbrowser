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

package tvbrowser.ui.configassistant;

import util.io.IOUtilities;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;


public class ImportHandler {


  private File mSrcDirectory;
  private int mNumOfChannels;

  public ImportHandler(File srcDirectory) {
    mSrcDirectory = srcDirectory;
    readContent();
  }

  private void readContent() {
    Pattern pattern = Pattern.compile("^(\\p{Alpha}{2})_(\\p{Alnum}+)_\\p{Alnum}+\\.\\p{Digit}{8}$");
    Matcher matcher;
    HashSet channelSet = new HashSet();
    String[] files = mSrcDirectory.list();
    for (int i=0; i<files.length; i++) {
      System.out.println(files[i]);
      matcher = pattern.matcher(files[i]);
      if (matcher.find()) {
        String country = matcher.group(1);
        String channelId = matcher.group(2);
        channelSet.add(country+"_"+channelId);
      }
    }
    mNumOfChannels = channelSet.size();
  }

  public int getChannelCount() {
     return mNumOfChannels;
  }

  public void importTo(File destination) throws IOException {
    System.out.println("importing...");
    IOUtilities.copy(mSrcDirectory.listFiles(), destination);
  }
}
