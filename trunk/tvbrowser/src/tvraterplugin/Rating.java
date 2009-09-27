/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 */

package tvraterplugin;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Representation of a rating
 * <p>
 * all rating values are represented by integer values 0 to 5,
 * where 0 means worst and 5 means best in the respective category
 * 
 * @author bodo tasche
 */
public class Rating implements Serializable {
  
  public static final int BEST_RATING = 5;
  
	/** Used to serialize this Object */
	static final long serialVersionUID = -6175532584789444451L;
	
	/**
	 * overall rating
	 * @deprecated 
	 */
	private static final String OVERALL = "overall";
	/**
	 * overall rating
	 */
	protected static final byte OVERALL_RATING_KEY = 0;

	/**
	 * action rating
	 * @deprecated
	 */
	private static final String ACTION = "action";
  /**
   * action rating
   */
  protected static final byte ACTION_RATING_KEY = 1;

	/**
	 * fun rating
	 * @deprecated
	 */
	private static final String FUN = "fun";
  /**
   * fun rating
   */
  protected static final byte FUN_RATING_KEY = 2;

	/**
	 * erotic rating
	 * @deprecated
	 */
	private static final String EROTIC = "erotic";
  /**
   * erotic rating
   */
  protected static final byte EROTIC_RATING_KEY = 3;

	/**
	 * tension rating
	 * @deprecated
	 */
	private static final String TENSION = "tension";
  /**
   * tension rating
   */
  protected static final byte TENSION_RATING_KEY = 4;

	/**
	 * entitlement rating
	 * @deprecated
	 */
	private static final String ENTITLEMENT = "entitlement";
  /**
   * entitlement rating
   */
  protected static final byte ENTITLEMENT_RATING_KEY = 5;

  private static final int RATING_ENTRY_COUNT = ENTITLEMENT_RATING_KEY + 1;

  /**
   * ID of the rating in the online database
   * @deprecated
   */
	private static final String ID = "id";
  /**
   * ID of the rating in the online database
   */
  private int onlineID;

  /**
   * genre code
   * @deprecated
   */
	private static final String GENRE = "genre";
  /**
   * genre code
   */
  private int genre = -1;

	/**
	 * user count of online rating
	 * @deprecated
	 */
	private static final String COUNT = "count";
  /**
   * number of users which rated this entry
   */
  private int userCount;

  /** Title of the program this rating is about */
	private String mTitle;
	
	/** Values in this rating-Element */
	private byte[] mValues;

	/**
	 * Creates an empty rating
	 */
	public Rating() {
	  mValues = new byte[RATING_ENTRY_COUNT];
    for (int i = 0; i < RATING_ENTRY_COUNT; i++) {
       mValues[i] = -1;
    }
  }

	/**
	 * Creates a empty Rating
	 * 
	 * @param title Title of the Program this rating is about
	 */
	public Rating(String title) {
    this();
		mTitle = title;
  }

	/**
	 * Creates a Rating
	 * 
	 * @param title Title of the Program this rating is about
	 * @param values Values for the Rating
	 */

  public Rating(String title, byte[] values) {
    mTitle = title;
    mValues = values;
  }

  /**
	 * Gets the Title
	 * @return title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Sets the Title
	 * @param string Title of the Program
	 */
	public void setTitle(String string) {
		mTitle = string;
	}
	
	/**
	 * Returns a String-Representation of this Object 
	 */
	public String toString() {
		return mTitle;
	}

	/**
	 * Gets an Int-Value from this Rating
	 * @param key Possible Keys are FUN, EROTIC...
	 * @return Int-Value
	 */
	public int getIntValue(int key) {
		return mValues[key];
	}

	/**
	 * Used to serialize this Object
	 * @param s Stream
	 * @throws IOException possible Error
	 */
	private synchronized void writeObject(java.io.ObjectOutputStream s) throws IOException {
		s.writeInt(2);
		s.writeObject(mTitle);
		s.writeInt(onlineID);
		s.writeInt(userCount);
		s.writeInt(genre);
		s.writeObject(mValues);
	}

	/**
	 * Used to deserialize this Object
	 * 
	 * @param s Stream
	 * @throws IOException possible Error
	 * @throws ClassNotFoundException possible Error
	 */
  private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
    int version = s.readInt();
		mTitle = (String) s.readObject();
		if (version == 1) {
		  @SuppressWarnings("unchecked")
		  HashMap<Object, Integer> map = (HashMap<Object, Integer>) s.readObject();
      convertVersion1(map);
		}
		else {
	    onlineID = s.readInt();
	    userCount = s.readInt();
	    genre = s.readInt();
		  mValues = (byte[]) s.readObject();
		}
	}
	
	private void convertVersion1(HashMap<Object, Integer> oldMap) {
	  mValues = new byte[RATING_ENTRY_COUNT];
	  for (Iterator<Entry<Object, Integer>> iterator = oldMap.entrySet().iterator(); iterator.hasNext();) {
      Entry<Object, Integer> entry = iterator.next();
      Object key = entry.getKey();
      int oldValue = entry.getValue();
      if (key.equals(OVERALL)) {
        mValues[OVERALL_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(ACTION)) {
        mValues[ACTION_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(FUN)) {
        mValues[FUN_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(EROTIC)) {
        mValues[EROTIC_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(TENSION)) {
        mValues[TENSION_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(ENTITLEMENT)) {
        mValues[ENTITLEMENT_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(ID)) {
        onlineID = oldValue;
      }
      else if (key.equals(GENRE)) {
        genre = oldValue;
      }
      else if (key.equals(COUNT)) {
        userCount = oldValue;
      }
    }
	}
	
	public int getRatingId() {
	  return onlineID;
	}

  public int getRatingCount() {
    return userCount;
  }

  public int getGenre() {
    return genre;
  }

  public int getOverallRating() {
    return mValues[OVERALL_RATING_KEY];
  }

  public int getActionRating() {
    return mValues[ACTION_RATING_KEY];
  }

  public int getEntitlementRating() {
    return mValues[ENTITLEMENT_RATING_KEY];
  }

  public int getFunRating() {
    return mValues[FUN_RATING_KEY];
  }

  public int getTensionRating() {
    return mValues[TENSION_RATING_KEY];
  }

  public int getEroticRating() {
    return mValues[EROTIC_RATING_KEY];
  }

  public void setOverallRating(int overall) {
    mValues[OVERALL_RATING_KEY] = (byte) overall;
  }

  public void setActionRating(int action) {
    mValues[ACTION_RATING_KEY] = (byte) action;
  }

  public void setEntitlementRating(int entitlement) {
    mValues[ENTITLEMENT_RATING_KEY] = (byte) entitlement;
  }

  public void setFunRating(int fun) {
    mValues[FUN_RATING_KEY] = (byte) fun;
  }

  public void setTensionRating(int tension) {
    mValues[TENSION_RATING_KEY] = (byte) tension;
  }

  public void setEroticRating(int erotic) {
    mValues[EROTIC_RATING_KEY] = (byte) erotic;
  }

  public void setUserCount(int userCountArg) {
    userCount = userCountArg;
  }

  public void setGenre(int genreArg) {
    genre = genreArg;
  }

  public void setOnlineID(int onlineIDArg) {
    onlineID = onlineIDArg;
  }
}
