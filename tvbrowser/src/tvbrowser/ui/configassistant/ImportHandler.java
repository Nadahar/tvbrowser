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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.io.IOUtilities;


public class ImportHandler {


  private File mSrcDirectory;
  private int mNumOfChannels;

  public ImportHandler(File srcDirectory) {
    int cnt;


    cnt = readContent(srcDirectory);
    if (cnt<=0) {
      cnt = tryTheParentFolder(srcDirectory);
      if (cnt <=0) {
        cnt = tryOneLevelDepth(srcDirectory);
      }

    }
    else {
      mSrcDirectory = srcDirectory;
    }

    mNumOfChannels = cnt;
  }

  private int readContent(File root) {
    Pattern pattern = Pattern.compile("^(\\p{Alpha}{2})_(\\p{Alnum}+)_\\p{Alnum}+\\.\\p{Digit}{8}$");
    Matcher matcher;
    HashSet<String> channelSet = new HashSet<String>();
    String[] files = root.list();
    for (int i=0; i<files.length; i++) {
      matcher = pattern.matcher(files[i]);
      if (matcher.find()) {
        String country = matcher.group(1);
        String channelId = matcher.group(2);
        channelSet.add(country+"_"+channelId);
      }
    }
    return channelSet.size();
  }


  private int tryTheParentFolder(File f) {
    File parent = f.getParentFile();
    if (parent != null) {
      mSrcDirectory = f;
      return readContent(parent);
    }
    return 0;
  }

  private int tryOneLevelDepth(File root) {
    File[] files = root.listFiles();
    for (int i=0; i<files.length; i++) {
      File f = files[i];
      if (f.isDirectory()) {
        int cnt = readContent(f);
        if (cnt >0) {
          mSrcDirectory = f;
          return cnt;
        }
      }
    }
    return 0;
  }

  public int getChannelCount() {
     return mNumOfChannels;
  }

  public void importTo(File destination) throws IOException {
    if (mSrcDirectory.equals(destination)) {
      throw new IOException("source and destination are equal");
    }
		if (!destination.exists()) {
			if (!destination.mkdirs()) {
        throw new IOException("Could not create directory '"+destination.getAbsolutePath()+"'");
			}
		}
    IOUtilities.copy(mSrcDirectory.listFiles(new FilenameFilter(){
      public boolean accept(File dir, String name) {
        return !".DS_Store".equals(name);
      }
    }), destination);
  }
}
