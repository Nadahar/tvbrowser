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
	private HashMap _overalrating = new HashMap();
	/** The personal Rating */
	private HashMap _personalrating = new HashMap();
	/** The changes since the last contact with the Server */
	private ArrayList _changedpersonal = new ArrayList();
	
	/**
	 * Gets the overall Ratings in this Database
	 * 
	 * @return all overall Ratings
	 */
	public Collection getOverallRating() {
		return _overalrating.values();
	}
	
	/**
	 * Gets one overall Rating for a specific Program
	 * 
	 * @param program get Rating for this Program
	 * @return the overall Rating
	 */
	public Rating getOverallRating(Program program) {
		return getOverallRating(program.getTitle().toLowerCase());
	}
	
	/**
	 * Gets one overall Rating for a specific title
	 * 
	 * @param title get rating for this Title
	 * @return the overall Rating
	 */
	public Rating getOverallRating(String title) {
		if (_overalrating.get(title.toLowerCase()) != null) {
			return (Rating) _overalrating.get(title.toLowerCase());
		}
		return null;
	}
	
	/**
	 * Sets the Rating
	 * 
	 * @param rating
	 */
	public void setOverallRating(Rating rating) {
		_overalrating.put(rating.getTitle().toLowerCase(), rating);
	}
	
	/**
	 * Gets the personal Ratings in this Database
	 * 
	 * @return all personal Ratings
	 */
	public Collection getPersonalRating() {
		return _personalrating.values();
	}
	
	/**
	 * Gets one personal Rating for a specific Program
	 * 
	 * @param program get Rating for this Program
	 * @return the personal Rating
	 */
	public Rating getPersonalRating(Program program) {
		Rating rating = getPersonalRating(program.getTitle().toLowerCase());
		if (rating == null) {
			rating = getPersonalRating(program.getTitle());
			_personalrating.remove(rating);
			_personalrating.put(rating.getTitle().toLowerCase(), rating);
		}
		return rating;
	}
	
	/**
	 * Gets one personal Rating for a specific title
	 * 
	 * @param title get rating for this Title
	 * @return the personal Rating
	 */
	public Rating getPersonalRating(String title) {
		Rating rating = (Rating) _personalrating.get(title.toLowerCase()); 
		
		if (rating == null) {
			rating = (Rating) _personalrating.get(title);
			_personalrating.remove(rating);
			_personalrating.put(rating.getTitle().toLowerCase(), rating);
		}
		
		return rating;
	}
	
	/**
	 * Saves the personal Rating in the Database
	 * 
	 * @param rating save this Rating
	 */
	public void setPersonalRating(Rating rating) {
		if (_personalrating.get(rating.getTitle().toLowerCase()) == null) {
			_personalrating.put(rating.getTitle().toLowerCase(), rating);
		}
		if (!_changedpersonal.contains(rating)) {
			_changedpersonal.add(rating);
		}
	}
	
	public ArrayList getChangedPersonal() {
		return _changedpersonal;
	}
	
	/**
	 * Empties the ChangedPersonal List
	 */
	public void clearChangedPersonal() {
		_changedpersonal = new ArrayList();
	}
	
	/**
	 * Empties the ChangedPersonal List
	 */
	public void clearOverall() {
		_overalrating = new HashMap();
	}
	
	/**
	 * Called by the host-application during start-up. Loads the data.
	 * 
	 * @see #writeData(ObjectOutputStream)
	 */
	public void readData(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		_personalrating = (HashMap) in.readObject();
		_overalrating = (HashMap) in.readObject();
		_changedpersonal = (ArrayList) in.readObject();
	}
	
	/**
	 * Counterpart to loadData. Called when the application shuts down. Saves
	 * the data.
	 * 
	 * @see #readData(ObjectInputStream)
	 */
	public void writeData(ObjectOutputStream out) throws IOException {
		out.writeObject(_personalrating);
		out.writeObject(_overalrating);
		out.writeObject(_changedpersonal);
	}
}