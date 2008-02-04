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

/**
 * Representation of a Rating
 * 
 * @author bodo tasche
 */
public class Rating implements Serializable {
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
	public static final byte OVERALL_RATING_KEY = 0;

	/**
	 * action rating
	 * @deprecated
	 */
	private static final String ACTION = "action";
  /**
   * action rating
   */
  public static final byte ACTION_RATING_KEY = 1;

	/**
	 * fun rating
	 * @deprecated
	 */
	private static final String FUN = "fun";
  /**
   * fun rating
   */
  public static final byte FUN_RATING_KEY = 2;

	/**
	 * erotic rating
	 * @deprecated
	 */
	private static final String EROTIC = "erotic";
  /**
   * erotic rating
   */
  public static final byte EROTIC_RATING_KEY = 3;

	/**
	 * tension rating
	 * @deprecated
	 */
	private static final String TENSION = "tension";
  /**
   * tension rating
   */
  public static final byte TENSION_RATING_KEY = 4;

	/**
	 * entitlement rating
	 * @deprecated
	 */
	private static final String ENTITLEMENT = "entitlement";
  /**
   * entitlement rating
   */
  public static final byte ENTITLEMENT_RATING_KEY = 5;

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
  private int genre;

	/**
	 * user count of online rating
	 * @deprecated
	 */
	public static final String COUNT = "count";
  /**
   * number of users which rated this entry
   */
  private int userCount;

  /** Title of the Program this rating is about */
	private String _title;
	
	/** Values in this Rating-Element */
	private byte[] _values;

	/**
	 * Creates a empty Rating
	 */
	public Rating() {
	  _values = new byte[RATING_ENTRY_COUNT];
	}

	/**
	 * Creates a empty Rating
	 * 
	 * @param title Title of the Program this rating is about
	 */
	public Rating(String title) {
    this();
		_title = title;
	}

	/**
	 * Creates a Rating
	 * 
	 * @param title Title of the Program this rating is about
	 * @param values Values for the Rating
	 */

  public Rating(String title, byte[] values) {
    _title = title;
    _values = values;
  }

  /**
	 * Gets the Title
	 * @return title
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * Sets the Title
	 * @param string Title of the Program
	 */
	public void setTitle(String string) {
		_title = string;
	}
	
	/**
	 * Returns a String-Representation of this Object 
	 */
	public String toString() {
		return _title;
	}

	/**
	 * Gets an Int-Value from this Rating
	 * @param key Possible Keys are FUN, EROTIC...
	 * @return Int-Value
	 */
	public int getIntValue(int key) {
		return _values[key];
	}

	/**
	 * Used to serialize this Object
	 * @param s Stream
	 * @throws IOException possible Error
	 */
	private synchronized void writeObject(java.io.ObjectOutputStream s) throws IOException {
		s.writeInt(2);
		s.writeObject(_title);
		s.writeInt(onlineID);
		s.writeInt(userCount);
		s.writeInt(genre);
		s.writeObject(_values);
	}

	/**
	 * Used to deserialize this Object
	 * 
	 * @param s Stream
	 * @throws IOException possible Error
	 * @throws ClassNotFoundException possible Error
	 */
	private synchronized void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
    int version = s.readInt();
		_title = (String) s.readObject();
		if (version == 1) {
		  convertVersion1((HashMap<Object, Integer>) s.readObject());
		}
		else {
	    onlineID = s.readInt();
	    userCount = s.readInt();
	    genre = s.readInt();
		  _values = (byte[]) s.readObject();
		}
	}
	
	private void convertVersion1(HashMap<Object, Integer> oldMap) {
	  _values = new byte[RATING_ENTRY_COUNT];
	  for (Iterator iterator = oldMap.keySet().iterator(); iterator.hasNext();) {
      Object key = iterator.next();
      int oldValue = oldMap.get(key);
      if (key.equals(OVERALL)) {
        _values[OVERALL_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(ACTION)) {
        _values[ACTION_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(FUN)) {
        _values[FUN_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(EROTIC)) {
        _values[EROTIC_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(TENSION)) {
        _values[TENSION_RATING_KEY] = (byte) oldValue;
      }
      else if (key.equals(ENTITLEMENT)) {
        _values[ENTITLEMENT_RATING_KEY] = (byte) oldValue;
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
    return _values[OVERALL_RATING_KEY];
  }

  public int getActionRating() {
    return _values[ACTION_RATING_KEY];
  }

  public int getEntitlementRating() {
    return _values[ENTITLEMENT_RATING_KEY];
  }

  public int getFunRating() {
    return _values[FUN_RATING_KEY];
  }

  public int getTensionRating() {
    return _values[TENSION_RATING_KEY];
  }

  public int getEroticRating() {
    return _values[EROTIC_RATING_KEY];
  }

  public void setOverallRating(int overall) {
    _values[OVERALL_RATING_KEY] = (byte) overall;
  }

  public void setActionRating(int action) {
    _values[ACTION_RATING_KEY] = (byte) action;
  }

  public void setEntitlementRating(int entitlement) {
    _values[ENTITLEMENT_RATING_KEY] = (byte) entitlement;
  }

  public void setFunRating(int fun) {
    _values[FUN_RATING_KEY] = (byte) fun;
  }

  public void setTensionRating(int tension) {
    _values[TENSION_RATING_KEY] = (byte) tension;
  }

  public void setEroticRating(int erotic) {
    _values[EROTIC_RATING_KEY] = (byte) erotic;
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