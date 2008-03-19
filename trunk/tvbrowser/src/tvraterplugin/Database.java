/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package tvraterplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import devplugin.Program;

/**
 * This class holds the "Database" for the Ratings. Could be replaced by a real
 * Database in a later Version...
 * 
 * @author bodo tasche
 */
public class Database {
  /** The overall Rating */
  private HashMap<String, Rating> mServerRating = new HashMap<String, Rating>();
  /** The personal Rating */
  private HashMap<String, Rating> mPersonalRating = new HashMap<String, Rating>();
  /** The changes since the last contact with the Server */
  private ArrayList<Rating> mChangedPersonal = new ArrayList<Rating>();

  /**
   * Gets the server ratings in this database
   * 
   * @return all server ratings
   */
  public synchronized Collection<Rating> getServerRatings() {
    return mServerRating.values();
  }

  /**
   * Gets the server rating for a specific program
   * 
   * @param program
   *          get rating for this program
   * @return the overall rating
   */
  public synchronized Rating getServerRating(Program program) {
    if (program == null) {
      return null;
    }
    String title = program.getTitle();
    if (title == null) {
      return null;
    }

    return getServerRating(title);
  }

  /**
   * Gets one server rating for a specific title
   * 
   * @param title
   *          get rating for this Title
   * @return the server Rating
   */
  public synchronized Rating getServerRating(String title) {
    if (title == null) {
      return null;
    }

    return mServerRating.get(title.toLowerCase());
  }

  /**
   * Sets the Rating
   * 
   * @param rating
   */
  public synchronized void setServerRating(Rating rating) {
    mServerRating.put(rating.getTitle().toLowerCase(), rating);
  }

  /**
   * Gets the personal Ratings in this Database
   * 
   * @return all personal Ratings
   */
  public synchronized Collection<Rating> getPersonalRatings() {
    return mPersonalRating.values();
  }

  /**
   * Gets one personal Rating for a specific Program
   * 
   * @param program
   *          get Rating for this Program
   * @return the personal Rating
   */
  public synchronized Rating getPersonalRating(Program program) {
    if (program == null) {
      return null;
    }
    String title = program.getTitle();
    if (title == null) {
      return null;
    }

    return getPersonalRating(title);
  }

  /**
   * Gets one personal Rating for a specific title
   * 
   * @param title
   *          get rating for this Title
   * @return the personal Rating
   */
  public synchronized Rating getPersonalRating(String title) {
    if (title == null) {
      return null;
    }

    String lowerCaseTitle = title.toLowerCase();
    Rating rating = mPersonalRating.get(title);
    if (rating != null) {
      mPersonalRating.remove(title);
      mPersonalRating.put(lowerCaseTitle, rating);
      return rating;
    }

    rating = mPersonalRating.get(lowerCaseTitle);
    return rating;
  }

  /**
   * Saves the personal rating in the database
   * 
   * @param rating
   *          save this Rating
   */
  public synchronized void setPersonalRating(Rating rating) {
    if (mPersonalRating.get(rating.getTitle()) != null) {
      mPersonalRating.remove(rating.getTitle());
    }

    if (mPersonalRating.get(rating.getTitle().toLowerCase()) == null) {
      mPersonalRating.put(rating.getTitle().toLowerCase(), rating);
    }

    if (!mChangedPersonal.contains(rating)) {
      mChangedPersonal.add(rating);
    }
  }

  public synchronized ArrayList<Rating> getChangedPersonal() {
    return mChangedPersonal;
  }

  /**
   * Empties the ChangedPersonal List
   */
  public synchronized void clearChangedPersonal() {
    mChangedPersonal = new ArrayList<Rating>();
  }

  /**
   * Empties the server based ratings list
   */
  public synchronized void clearServer() {
    mServerRating = new HashMap<String, Rating>();
  }

  /**
   * Called by the host-application during start-up. Loads the data.
   * 
   * @throws IOException
   * @throws ClassNotFoundException
   * 
   * @see #writeData(ObjectOutputStream)
   */
  @SuppressWarnings("unchecked")
  public synchronized void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    mPersonalRating = (HashMap<String, Rating>) in.readObject();
    mServerRating = (HashMap<String, Rating>) in.readObject();
    mChangedPersonal = (ArrayList<Rating>) in.readObject();
  }

  /**
   * Counterpart to loadData. Called when the application shuts down. Saves the
   * data.
   * 
   * @throws IOException
   * 
   * @see #readData(ObjectInputStream)
   */
  public synchronized void writeData(ObjectOutputStream out) throws IOException {
    out.writeObject(mPersonalRating);
    out.writeObject(mServerRating);
    out.writeObject(mChangedPersonal);
  }
}