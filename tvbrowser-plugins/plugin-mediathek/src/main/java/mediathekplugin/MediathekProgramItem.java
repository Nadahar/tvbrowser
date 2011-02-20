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
package mediathekplugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

import javax.swing.Icon;
import javax.swing.ProgressMonitor;

import org.apache.commons.lang.StringUtils;

import util.browserlauncher.Launch;
import util.io.ExecutionHandler;
import util.ui.UIThreadRunner;

public final class MediathekProgramItem {
  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MediathekProgramItem.class);

  private static final int TYPE_LINK = 0;
  private static final int TYPE_AUDIO = 1;
  private static final int TYPE_IMAGE = 2;
  private static final int TYPE_VIDEO = 3;
  private static final Icon ICON_AUDIO = MediathekPlugin.getInstance()
      .createImageIcon("mimetypes", "audio-x-generic", 16);
  private static final Icon ICON_IMAGE = MediathekPlugin.getInstance()
      .createImageIcon("mimetypes", "image-x-generic", 16);
  private static final Icon ICON_VIDEO = MediathekPlugin.getInstance()
      .createImageIcon("mimetypes", "video-x-generic", 16);

  private String mTitle;
  private String mUrl;
  private int mType = TYPE_LINK;

  public MediathekProgramItem(final String title, final String url,
      final String contentType) {
    assert url != null;
    assert title != null;
    this.mTitle = title;
    this.mUrl = url;
    if (contentType != null) {
      if (contentType.contains("video")) {
        this.mType = TYPE_VIDEO;
      } else if (contentType.contains("audio")) {
        this.mType = TYPE_AUDIO;
      }
    }
  }

  public String getUrl() {
    return mUrl;
  }

  public String getTitle() {
    return mTitle;
  }

  public Icon getIcon() {
    switch (mType) {
    case TYPE_VIDEO:
      return ICON_VIDEO;
    case TYPE_AUDIO:
      return ICON_AUDIO;
    case TYPE_IMAGE:
      return ICON_IMAGE;
    }
    return MediathekPlugin.getInstance().getWebIcon();
  }

  public void show() {
    if (StringUtils.isBlank(mUrl)) {
      return;
    }
    if (StringUtils.containsIgnoreCase(mUrl, "--host")) {
      openStream();
    }
    else {
      Launch.openURL(mUrl);
    }
  }

  private void openStream() {
    Thread streamThread = new Thread("Stream copy") {
      ProgressMonitor monitor = null;
      @Override
      public void run() {
        try {
          monitor = new ProgressMonitor(MediathekPlugin.getInstance().getFrame(),mLocalizer.msg("store","Getting local stream copy...")," ", 0, 3);
          monitor.setMillisToDecideToPopup(0);
          setNote(0, mLocalizer.ellipsisMsg("temp", "Starting flvstreamer"));
          File tempFile = File.createTempFile("mediathek", ".flv");
          String fileName = tempFile.toString();
          tempFile.delete();
          String streamParams = mUrl +" --flv " + fileName;
          ExecutionHandler streamer = new ExecutionHandler(streamParams, "flvstreamer");
          streamer.execute();
          DecimalFormat format = new DecimalFormat( "0.00" );
          setNote(1, mLocalizer.ellipsisMsg("wait", "Waiting for flvstreamer to get some data ({0} MB)", format.format(0.0)));
          int time = 0;
          long fileSize = 0;
          try {
            while (time < 20 && fileSize < 10 * 1024 * 1024 && !monitor.isCanceled()) {
              Thread.sleep(500);
              time++;
              fileSize = tempFile.length();
              double mb = fileSize / 1048576.0;
              setNote(1, mLocalizer.ellipsisMsg("wait", "Waiting for flvstreamer to get some data ({0} MB)", format.format(mb)));
            }
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          if (!monitor.isCanceled()) {
            setNote(2, mLocalizer.ellipsisMsg("player", "Starting player"));
            ExecutionHandler player = new ExecutionHandler(fileName, "vlc");
            player.execute();
            setNote(3, "");
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        if (monitor != null) {
          monitor.close();
        }
      }
      private void setNote(final int progress, final String note) {
        try {
          UIThreadRunner.invokeAndWait(new Runnable() {

            public void run() {
              monitor.setProgress(progress);
              monitor.setNote(note);
            }
          });
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    streamThread.start();
  }
}
