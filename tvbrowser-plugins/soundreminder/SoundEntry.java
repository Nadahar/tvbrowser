/*
 * SoundReminder - Plugin for TV-Browser
 * Copyright (C) 2009 René Mach
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
 *     $Date$
 *   $Author$
 * $Revision$
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
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;

import util.ui.Localizer;
import util.ui.UiUtilities;

import devplugin.Program;

/**
 * The entry class for the sound files.
 * <p>
 * @author René Mach
 */
public class SoundEntry {
  private String mSearchText;
  private String mPath;
  
  private String mPreSearchPart;
  private Pattern mSearchPattern;
  private boolean mCaseSensitive;
  
  
  protected SoundEntry(String searchText, boolean caseSensitive, String path) {
    setValues(searchText, caseSensitive, path);
  }
  
  protected SoundEntry(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    mSearchText = in.readUTF();
    mPath = in.readUTF();
    
    if(in.readBoolean()) {
      mPreSearchPart = in.readUTF();
      mSearchPattern = createSearchPattern(mSearchText, mCaseSensitive);
    }
  }
  
  /* Copied from IDontWant2SeeListEntry */
  protected String getSearchText() {
    return mSearchText;
  }
  
  protected String getPath() {
    return mPath;
  }
  
  /* Copied from IDontWant2SeeListEntry */
  protected boolean isCaseSensitive() {
    return mCaseSensitive;
  }
  
  /* Copied from IDontWant2SeeListEntry */
  protected void setValues(String searchText, boolean caseSensitive, String path) {
    mPreSearchPart = null;
    mSearchPattern = null;
    mPath = path;
        
    mSearchText = searchText;
    mCaseSensitive = caseSensitive;
    
    if(searchText.indexOf("*") != -1) {
      String[] searchParts = searchText.split("\\*");
      
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
  
  /* Copied from IDontWant2SeeListEntry */
  private Pattern createSearchPattern(String searchText, boolean caseSensitive) {
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
  private boolean matchesTitle(String title) {
    boolean matches = false;
    
    if(mPreSearchPart == null) {
      // match full title
      matches = mCaseSensitive ? title.equals(mSearchText) : title
          .equalsIgnoreCase(mSearchText);
    } else {
      // or match with wild card
      String preSearchValue = mCaseSensitive ? title : title.toLowerCase();
      if (preSearchValue.indexOf(mPreSearchPart) != -1) {
        Matcher match = mSearchPattern.matcher(title);
        matches = match.matches();
      }
    }
    
    return matches;
  }
  
  /* Copied from IDontWant2SeeListEntry */
  protected boolean matches(Program p) {
    String title = p.getTitle();
    boolean found = matchesTitle(title);
    final String suffix = " (Fortsetzung)";
    if ((!found) && title.endsWith(suffix)) {
      found = matchesTitle(title.substring(0, title.length() - suffix.length()));
    }
    return found;
  }
  protected void playSound() {
    playSound(mPath);
  }
  
  /* Copied from ReminderPlugin */
  protected static Object playSound(String fileName) {
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
              byte[] myData = new byte[1024 * format.getFrameSize()];
              int numBytesToRead = myData.length;
              int numBytesRead = 0;
              int total = 0;
              int totalToRead = (int) (format.getFrameSize() * ais.getFrameLength());
              stopped = false;
              
              line.addLineListener(new LineListener() {
                public void update(LineEvent event) {                  
                  if(line != null && !line.isRunning()) {
                    stopped = true;
                    line.close();
                    try {
                      ais.close();
                    }catch(Exception ee) {
                      // ignore
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
              }catch(Exception e) {}
              
              line.drain();
              line.stop();
            }
          }.start();
          
          return line;         
        }else {
          URL url = new File(fileName).toURI().toURL();
          AudioClip clip= Applet.newAudioClip(url);
          clip.play();
        }
      }

    } catch (Exception e) {e.printStackTrace();
      if((new File(fileName)).isFile()) {
        URL url;
        try {
          url = new File(fileName).toURI().toURL();
          AudioClip clip= Applet.newAudioClip(url);
          clip.play();
        } catch (MalformedURLException e1) {
        }
      }
      else {
        String msg = SoundReminder.mLocalizer.msg( "playError",
          "Error loading sound reminder file!\n({0})" , fileName);
        JOptionPane.showMessageDialog(UiUtilities.getBestDialogParent(SoundReminder.getInstance().getSuperFrame()),msg,Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.ERROR_MESSAGE);
      }
    }
    
    return null;
  }
  
  /* Copied from IDontWant2SeeListEntry */
  protected void writeData(ObjectOutputStream out) throws IOException {
    out.writeUTF(mSearchText);
    out.writeUTF(mPath);
    
    out.writeBoolean(mPreSearchPart != null);
   
    if(mPreSearchPart != null) {
      out.writeUTF(mPreSearchPart);
      mSearchPattern = createSearchPattern(mSearchText, mCaseSensitive);
    }
  }
  
  
}
