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

import org.apache.commons.lang3.StringUtils;

import devplugin.Date;
import util.browserlauncher.Launch;

public final class MediathekProgramItem implements Comparable<MediathekProgramItem>{
  
  private String mTitle;
  private String mUrl;
  private MediathekQuality mQuality;
  
  public MediathekQuality getQuality() {
    return mQuality;
  }
  
  
  public Date getDate() {
    return mDate;
  }

  private Date mDate;
  
  
  public MediathekProgramItem(final String title, final String url, final String date,
      final MediathekQuality quality) {
    assert url != null;
    assert title != null;
    this.mTitle = title;
    this.mUrl = url;
    this.mDate = Date.createDDMMYYYY(date, ".");
    if (mDate == null) {
      System.err.println("Mediathek: Error reading Date: " + date);
    }
    this.mQuality = quality;
  }

  public String getUrl() {
    return mUrl;
  }

  public String getTitle() {
    return mTitle;
  }
  
  public String getInfo(){
    return mTitle + " " + mQuality.toAppendix();
  }
  
  public String getInfoDate(){
    return mDate.toString() + " " + mTitle + " " + mQuality.toAppendix();
  }

  public Icon getIcon() {
    return MediathekPlugin.getInstance().getWebIcon();
  }

  public void show() {
    if (StringUtils.isBlank(mUrl)) {
      return;
    } else {
      Launch.openURL(mUrl);
    }
  }

  public int compareTo(MediathekProgramItem o) {
    if (mDate==null){
      return -1;
    }
    if (o.mDate==null){
      return 1;
    }
    int c = this.mDate.compareTo(o.mDate);
    if (c!=0) return c;
    c = mTitle.compareTo(o.mTitle);
    if (c!=0) return c;
    c = mQuality.compareTo(o.mQuality);
    if (c!=0) return c;
    c = mUrl.compareTo(o.mUrl);
    return c;
  }
}
