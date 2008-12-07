/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date: 2007-10-02 10:19:08 +0200 (Di, 02 Okt 2007) $
 *   $Author: Bananeweizen $
 * $Revision: 3966 $
 */
package movieawardplugin;

public class Award {
  public static enum Status {
    NOMINATED, WINNER, HONORED
  }

  private int mAwardYear;
  private String mCategory;
  private Status mStatus;
  private String mMovieId;
  private String mRecipient;

  public Award(String category, String status, String movieId, int awardYear, String recipient) {
    mCategory = category;
    if (status.equalsIgnoreCase("winner")) {
      mStatus = Status.WINNER;
    } else if (status.equalsIgnoreCase("nominated")) {
      mStatus = Status.NOMINATED;
    } else if (status.equalsIgnoreCase("honored")) {
      mStatus = Status.HONORED;
    }
    mMovieId = movieId;
    mAwardYear = awardYear;
    mRecipient = recipient;
  }

  public String getMovieId() {
    return mMovieId;
  }

  public int getAwardYear() {
    return mAwardYear;
  }

  public void setAwardYear(int awardYear) {
    mAwardYear = awardYear;
  }

  public String getCategory() {
    return mCategory;
  }

  public void setCategorie(String category) {
    mCategory = category;
  }

  public Status getStatus() {
    return mStatus;
  }

  public void setStatus(Status status) {
    mStatus = status;
  }

  public void setMovieId(String movieId) {
    mMovieId = movieId;
  }

  public String getRecipient() {
    return mRecipient;
  }

  public void setRecipient(String recipient) {
    mRecipient = recipient;
  }


}
