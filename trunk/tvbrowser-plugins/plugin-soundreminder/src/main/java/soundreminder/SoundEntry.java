/*
 * SoundReminder - Plugin for TV-Browser
 * Copyright (C) 2009 Ren� Mach
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
 * SVN information:
 *     $Date: 2009-03-01 09:56:39 +0100 (So, 01 Mrz 2009) $
 *   $Author: ds10 $
 * $Revision: 5521 $
 */
package soundreminder;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * The entry class for the sound files.
 * <p>
 * @author René Mach
 */
public class SoundEntry {
  static final String DUMMY_ENTRY = "DUMMY-ENTRY";
  static final String DUMMY_FILE = "DUMMY-FILE"; 
  
  private String mPath;
  
  private boolean mCaseSensitive = false;
  private SearchValue[] mSearchValues;
  
  protected SoundEntry(final String searchText, final boolean caseSensitive,
      final String path) {
    setValues(searchText, caseSensitive, path);
  }

  protected SoundEntry(final ObjectInputStream in, final int version) throws IOException {
    if(version <= 4) {
      String searchText = in.readUTF();
      mPath = in.readUTF();
      
      if(version < 4) {
        mPath = checkForRelativePath(mPath);
      }
      
      String preSearchPart = null;
      
      if(in.readBoolean()) {
        preSearchPart = in.readUTF();
      }
      
      mSearchValues = new SearchValue[] {
          new SearchValue(searchText, preSearchPart, mCaseSensitive)
      };
    }
    else {
      mPath = in.readUTF();
      
      if(version < 4) {
        mPath = checkForRelativePath(mPath);
      }
      
      mCaseSensitive = in.readBoolean();
      
      mSearchValues = new SearchValue[in.readInt()];
      
      for(int i = 0; i < mSearchValues.length; i++) {
        mSearchValues[i] = new SearchValue(in, version, mCaseSensitive);
      }
    }
  }

  /* Copied from IDontWant2SeeListEntry */
  protected String getSearchText() {
    StringBuilder builder = new StringBuilder();
    
    for(int i = 0; i < mSearchValues.length-1; i++) {
      builder.append(mSearchValues[i].mSearchText).append(";");
    }
    
    if(mSearchValues.length > 0) {
      builder.append(mSearchValues[mSearchValues.length-1].mSearchText);
    }
    
    return builder.toString();
  }
  
  private static String translateRelativePath(String path) {
    if(path.startsWith("..") || path.startsWith(".")) {
      if(path.startsWith("./")) {
        path = path.substring(2);
      }
      
      String[] pathParts = path.replace("\\", "/").split("/");
      String[] homeParts = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome().replace("\\", "/").split("/");
      
      int homePartsToUse = homeParts.length;
      int i = 0;
      
      while(i < pathParts.length && pathParts[i].equals("..")) {
        i++;
        homePartsToUse--;
      }
      
      if(homePartsToUse >= 0 && pathParts.length > i) {
        StringBuilder pathToUse = new StringBuilder();
        
        for(int j = 0; j < homePartsToUse; j++) {
          pathToUse.append(homeParts[j]).append("/");
        }
        for(int j = i; j < pathParts.length; j++) {
          pathToUse.append(pathParts[j]).append("/");
        }
        
        if(pathToUse.length() > 1 && pathToUse.toString().endsWith("/")) {
          pathToUse.deleteCharAt(pathToUse.length()-1);
          
          path = pathToUse.toString();
        }
      }
    }
    
    return path;
  }

  protected String getPath() {
    return translateRelativePath(mPath);
  }

  /* Copied from IDontWant2SeeListEntry */
  protected boolean isCaseSensitive() {
    return mCaseSensitive;
  }

  private String checkForRelativePath(String path) {
    if((path.contains("\\") || path.contains("/"))) {
      File audioPath = new File(path);
      
      String[] homeParts = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome().replace("\\", "/").split("/");
      String[] pathParts = audioPath.getParent().replace("\\", "/").split("/");
      
      int i = 0;
      
      while(homeParts.length > i && pathParts.length > i && homeParts[i].equals(pathParts[i])) {
        i++;
      }
      
      if(i > 0) {
        StringBuilder relativeValue = new StringBuilder();
        
        if(i < homeParts.length) {
          for(int j = i; j < homeParts.length; j++) {
            relativeValue.append("../");
          }
        }
        else {
          relativeValue.append("./");
        }
        
        for(int j = i; j < pathParts.length; j++) {
          relativeValue.append(pathParts[j]).append("/");
        }
        
        relativeValue.append(audioPath.getName());
        
        return relativeValue.toString();
      }
    }
    
    return path;
  }
  
  /* Copied from IDontWant2SeeListEntry */
  protected void setValues(final String searchText, final boolean caseSensitive, final String path) {
    mSearchValues = null;
    
    mPath = checkForRelativePath(path);

    String[] searchValues = searchText.split(";");
    
    mSearchValues = new SearchValue[searchValues.length];
    mCaseSensitive = caseSensitive;

    for(int i = 0; i < searchValues.length; i++) {
      mSearchValues[i] = new SearchValue(searchValues[i], caseSensitive);
    }
  }

  /* Copied from IDontWant2SeeListEntry */
  protected boolean matches(final Program p) {
    boolean matches = false;
    
    for(SearchValue value : mSearchValues) {
      if(value.matches(p,mCaseSensitive)) {
        matches = true;
        break;
      }
    }
    
    return matches;
  }
  
  protected Object playSound() {
    return playSound(mPath);
  }

  /* Copied from ReminderPlugin */
  protected static Object playSound(String fileName) {
    fileName = translateRelativePath(fileName);
    
    try {
      if (fileName.toLowerCase().endsWith(".mid")) {
        final Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();

        final InputStream midiFile = new FileInputStream(fileName);
        sequencer.setSequence(MidiSystem.getSequence(midiFile));

        sequencer.start();

        new Thread("Reminder MIDI sequencer") {
          public void run() {
            setPriority(Thread.MIN_PRIORITY);
            while (sequencer.isRunning()) {
              try {
                Thread.sleep(100);
              } catch (Exception ee) {
                // ignore
              }
            }

            try {
              sequencer.close();
              midiFile.close();
            } catch (Exception ee) {
              // ignore
            }
          }
        }.start();

        return sequencer;
      } else {
        final AudioInputStream ais = AudioSystem.getAudioInputStream(new File(
            fileName));

        final AudioFormat format = ais.getFormat();
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        
        if(AudioSystem.isLineSupported(info)) {
          final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

          line.open(format);
          line.start();

          new Thread("Reminder audio playing") {
            private boolean stopped;
            public void run() {
              try {
                final byte[] myData = new byte[1024 * format.getFrameSize()];
                final int numBytesToRead = myData.length;
                int numBytesRead = 0;
                int total = 0;
                final int totalToRead = (int) (format.getFrameSize() * ais
                    .getFrameLength());
                stopped = false;
  
                line.addLineListener(new LineListener() {
                  public void update(final LineEvent event) {
                    if(event.getType() != Type.START && line != null && (!line.isRunning() || event.getType() == Type.STOP)) {
                      stopped = true;
                      
                      try {
                        ais.close();
                      }catch(Exception ee) {
                        // ignore
                        
                        ee.printStackTrace();
                      }
                    }
                  }
                });
                
                try {
                  while (total < totalToRead && !stopped) {
                    numBytesRead = ais.read(myData, 0, numBytesToRead);
                    
                    if (numBytesRead == -1) {
                      break;
                    }
  
                    total += numBytesRead;
                    
                    line.write(myData, 0, numBytesRead);
                  }
                }catch(Throwable e) {e.printStackTrace();}
               
                if(line.isRunning()) {
                  line.drain();
                }
                
                line.stop();
                
                if(line != null) {
                  line.close();
                }
              }catch(Throwable ex) {
                ex.printStackTrace();
              }
            }
          }.start();

          return line;
        }else {
          final URL url = new File(fileName).toURI().toURL();
          final AudioClip clip = Applet.newAudioClip(url);
          clip.play();
        }
      }

    } catch (Exception e) {
      if((new File(fileName)).isFile()) {
        try {
          final URL url = new File(fileName).toURI().toURL();
          final AudioClip clip = Applet.newAudioClip(url);
          clip.play();
        } catch (MalformedURLException e1) {
        }
      }
      else {
        final String msg = SoundReminder.mLocalizer.msg("playError",
          "Error loading sound reminder file!\n({0})" , fileName);
        JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(SoundReminder.getInstance().getSuperFrame()),msg,Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.ERROR_MESSAGE);
      }
    }

    return null;
  }

  /* Copied from IDontWant2SeeListEntry */
  protected void writeData(final ObjectOutputStream out) throws IOException {    
    out.writeUTF(mPath);
    out.writeBoolean(mCaseSensitive);
    
    out.writeInt(mSearchValues.length);
    
    for(SearchValue value : mSearchValues) {
      value.writeData(out);
    }
  }

  private final static class SearchValue {
    private String mPreSearchPart;
    private Pattern mSearchPattern;
    private String mSearchText;
    
    public SearchValue(String searchText, boolean caseSensitive) {
      mSearchText = searchText;
      
      if (!searchText.trim().startsWith("Filter:") && searchText.indexOf('*') != -1) {
        final String[] searchParts = searchText.split("\\*");

        if(searchParts != null && searchParts.length > 0) {
          mPreSearchPart = searchParts[0];
          for(int i = 1; i < searchParts.length; i++) {
            if(mPreSearchPart.length() < searchParts[i].length()) {
              mPreSearchPart = searchParts[i];
            }
          }

          if(!caseSensitive) {
            mPreSearchPart = mPreSearchPart.toLowerCase();
          }

          mSearchPattern = createSearchPattern(searchText,caseSensitive);
        }
      }
    }
    
    public SearchValue(String searchText, String preSearchPart, boolean caseSensitive) {
      mSearchText = searchText;
      mPreSearchPart = preSearchPart;
      
      if(preSearchPart != null) {
        mSearchPattern = createSearchPattern(searchText, caseSensitive);
      }
    }
    
    SearchValue(ObjectInputStream in, int version, boolean caseSensitve) throws IOException {
      mSearchText = in.readUTF();
      
      if(in.readBoolean()) {
        mPreSearchPart = in.readUTF();
        mSearchPattern = createSearchPattern(mSearchText, caseSensitve);
      }
    }
    
    void writeData(final ObjectOutputStream out) throws IOException {
      out.writeUTF(mSearchText);
      
      out.writeBoolean(mPreSearchPart != null);

      if(mPreSearchPart != null) {
        out.writeUTF(mPreSearchPart);
      }
    }
    
    /* Copied from IDontWant2SeeListEntry */
    private Pattern createSearchPattern(final String searchText, final boolean caseSensitive) {
      int flags = Pattern.DOTALL;
      if (! caseSensitive) {
        flags |= Pattern.CASE_INSENSITIVE;
        flags |= Pattern.UNICODE_CASE;
      }

      // Comment copied from tvbrowser.core.search.regexsearch.RegexSearcher.java:
      // NOTE: All words are quoted with "\Q" and "\E". This way regex code will
      //       be ignored within the search text. (A search for "C++" will not
      //       result in an syntax error)
      return Pattern.compile("\\Q" + searchText.replace("*","\\E.*\\Q") + "\\E",flags);
    }
    
    /* Copied from IDontWant2SeeListEntry */
    private boolean matchesTitle(final String title, boolean caseSensitive) {
      boolean matches = false;

      if(mPreSearchPart == null) {
        // match full title
        matches = caseSensitive ? title.equals(mSearchText) : title
            .equalsIgnoreCase(mSearchText);
      } else {
        // or match with wild card
        final String preSearchValue = caseSensitive ? title : title
            .toLowerCase();
        if (preSearchValue.indexOf(mPreSearchPart) != -1) {
          final Matcher match = mSearchPattern.matcher(title);
          matches = match.matches();
        }
      }

      return matches;
    }
    
    /* Copied from IDontWant2SeeListEntry */
    protected boolean matches(final Program p, boolean caseSensitive) {
      String title = p.getTitle();
      
      if(mSearchText.trim().startsWith("Filter:")) {
        title = mSearchText.substring(mSearchText.indexOf(":")+1, mSearchText.length()).trim();
        
        ProgramFilter[] filters = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
        
        for(ProgramFilter filter : filters) {
          if(title.equals(filter.getName())) {
            return filter.accept(p);
          }
        }
      }
      
      boolean found = matchesTitle(title, caseSensitive);
      final String suffix = " (Fortsetzung)";
      if ((!found) && title.endsWith(suffix)) {
        found = matchesTitle(title.substring(0, title.length() - suffix.length()), caseSensitive);
      }
      
      return found;
    }
  }
}
