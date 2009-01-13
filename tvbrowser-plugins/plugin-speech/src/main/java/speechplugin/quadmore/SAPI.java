/*
 * SpeechPlugin for TV-Browser
 * Copyright Michael Keppler
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
 * VCS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package speechplugin.quadmore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import speechplugin.SpeechPlugin;

public class SAPI {
  private QuadmoreTTS quadmore;
  private String mExtractedLib;

  public SAPI() {
    if (mExtractedLib == null) {
      mExtractedLib = extractLibrary();
      System.load(mExtractedLib);
    }
    quadmore = new QuadmoreTTS();
  }
  
  public boolean speak(String text) {
    return quadmore.SpeakDarling(text);
  }
  
  public List<String> getVoices() {
    ArrayList<String> result = new ArrayList<String>();
//    <?xml version="1.0"?><voice>Microsoft Sam</voice><voice>Microsoft Mike</voice><voice>Microsoft Mary</voice><voice>Sample TTS Voice</voice>
    String voices = quadmore.getVoiceToken();
    while (voices.indexOf("<voice>") > -1) {
      int start = voices.indexOf("<voice>") + 7;
      int end = voices.indexOf("</voice>");
      result.add(voices.substring(start, end));
      voices = voices.substring(end + 8);
    }
    return result;
  }
  
  public boolean setVoice(String voiceName) {
    return quadmore.setVoiceToken(voiceName);
  }

  private String extractLibrary() {
    File dll;
    try {
      // Get input stream from jar resource
      InputStream inputStream = getClass().getResource("QuadTTS.dll")
          .openStream();

      // Copy resource to file system in a temporary folder with a unique name
      dll = File.createTempFile("tvbrowser_speech_", ".dll");
      FileOutputStream outputStream = new FileOutputStream(dll);
      byte[] array = new byte[8192];
      int read = 0;
      while ((read = inputStream.read(array)) > 0) {
        outputStream.write(array, 0, read);
      }
      outputStream.close();

      // Delete on exit
      dll.deleteOnExit();
      SpeechPlugin.getInstance();
      SpeechPlugin.getPluginManager().deleteFileOnNextStart(dll.getPath());
    } catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
    return dll.getPath();
  }

}
