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

import javax.swing.Icon;

public final class MediathekProgramItem {
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
}
