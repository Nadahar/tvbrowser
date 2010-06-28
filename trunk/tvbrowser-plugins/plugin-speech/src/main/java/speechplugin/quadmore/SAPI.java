/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package speechplugin.quadmore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import devplugin.Plugin;

final public class SAPI {
  private QuadmoreTTS quadmore;
  private static String mExtractedLib = null;

  public SAPI() {
    if (mExtractedLib == null) {
      mExtractedLib = extractLibrary();
      System.load(mExtractedLib);
    }
    quadmore = new QuadmoreTTS();
  }

  public boolean speak(final String text) {
    return quadmore.SpeakDarling(text);
  }

  public List<String> getVoices() {
    final ArrayList<String> result = new ArrayList<String>();
    // <?xml version="1.0"?><voice>Microsoft Sam</voice><voice>Microsoft
    // Mike</voice><voice>Microsoft Mary</voice><voice>Sample TTS Voice</voice>
    String voices = quadmore.getVoiceToken();
    while (voices.indexOf("<voice>") > -1) {
      final int start = voices.indexOf("<voice>") + 7;
      final int end = voices.indexOf("</voice>");
      result.add(voices.substring(start, end));
      voices = voices.substring(end + 8);
    }
    return result;
  }

  public boolean setVoice(final String voiceName) {
    return quadmore.setVoiceToken(voiceName);
  }

  private String extractLibrary() {
    File dll;
    try {
      // Get input stream from jar resource
      final InputStream inputStream = getClass().getResource("QuadTTS.dll")
          .openStream();

      // Copy resource to file system in a temporary folder with a unique name
      dll = File.createTempFile("tvbrowser_speech_", ".dll");
      final FileOutputStream outputStream = new FileOutputStream(dll);
      final byte[] array = new byte[8192];
      int read = 0;
      while ((read = inputStream.read(array)) > 0) {
        outputStream.write(array, 0, read);
      }
      outputStream.close();

      // Delete on exit
      dll.deleteOnExit();
      Plugin.getPluginManager().deleteFileOnNextStart(dll.getPath());
    } catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
    return dll.getPath();
  }

}
