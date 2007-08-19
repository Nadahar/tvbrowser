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

/**
 * Representation of a Rating
 * 
 * @author bodo tasche
 */
public class Rating implements Serializable {
	/** Used to serialize this Object */
	static final long serialVersionUID = -6175532584789444451L;
	
	/** Overall-Rating */
	public static final String OVERALL = "overall";
	/** Action-Rating */
	public static final String ACTION = "action";
	/** Fun-Rating */
	public static final String FUN = "fun";
	/** Erotic-Rating */
	public static final String EROTIC = "erotic";
	/** Tension-Rating */
	public static final String TENSION = "tension";
	/** Entitlement-Rating */
	public static final String ENTITLEMENT = "entitlement";

	/** ID of the rating in the online-database */
	public static final String ID = "id";
	/** The Genre */
	public static final String GENRE = "genre";	
	/** How many Users have rated this Entry */
	public static final String COUNT = "count";	
	
	/** Title of the Program this rating is about */
	private String _title;
	/** Values in this Rating-Element */
	private HashMap<Object, Integer> _values;

	/**
	 * Creates a empty Rating
	 */
	public Rating() {
		_values = new HashMap<Object, Integer>();
	}

	/**
	 * Creates a empty Rating
	 * 
	 * @param title Title of the Program this rating is about
	 */
	public Rating(String title) {
		_title = title;
		_values = new HashMap<Object, Integer>();
	}

	/**
	 * Creates a Rating
	 * 
	 * @param title Title of the Program this rating is about
	 * @param values Values for the Rating
	 */
	public Rating(String title, HashMap<Object, Integer> values) {
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
	public int getIntValue(Object key) {
	    
		if (_values.get(key) == null) {
		    
			return -1;
		}

		Object value = _values.get(key);
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}

		return -1;
	}

	/**
	 * Sets an Int-Value in this Rating
	 * @param key Possible Keys are FUN, EROTIC...
	 * @param value Int-Value for this Key
	 */
	public void setValue(Object key, int value) {
		_values.put(key, new Integer(value));
	}

	/**
	 * Used to serialize this Object
	 * @param s Stream
	 * @throws IOException possible Error
	 */
	private synchronized void writeObject(java.io.ObjectOutputStream s) throws IOException {
		s.writeInt(1);
		s.writeObject(_title);
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
    s.readInt(); // read version, unused
		_title = (String) s.readObject();
		_values = (HashMap<Object, Integer>) s.readObject();
	}
}