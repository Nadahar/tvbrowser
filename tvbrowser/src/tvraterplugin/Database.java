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
     private  HashMap<String, Rating> _overalrating = new HashMap<String, Rating>();
	/** The personal Rating */
	private HashMap<String, Rating> _personalrating = new HashMap<String, Rating>();
	/** The changes since the last contact with the Server */
	private ArrayList<Rating> _changedpersonal = new ArrayList<Rating>();
	
	/**
	 * Gets the overall Ratings in this Database
	 * @return 
	 * 
	 * @return all overall Ratings
	 */
	public synchronized Collection<Rating> getOverallRating() {
		return _overalrating.values();
	}
	
	/**
	 * Gets one overall Rating for a specific Program
	 * 
	 * @param program get Rating for this Program
	 * @return the overall Rating
	 */
	public synchronized Rating getOverallRating(Program program) {
		if (program == null) { 
			return null;
		}
		String title = program.getTitle();
		if (title == null) {
			return null;
		}
		
		return getOverallRating(title);
	}
	
	/**
	 * Gets one overall Rating for a specific title
	 * 
	 * @param title get rating for this Title
	 * @return the overall Rating
	 */
	public synchronized Rating getOverallRating(String title) {
		if (title == null) {
			return null;
		}
		
		return _overalrating.get(title.toLowerCase());
	}
	
	/**
	 * Sets the Rating
	 * 
	 * @param rating
	 */
	public synchronized void setOverallRating(Rating rating) {
		_overalrating.put(rating.getTitle().toLowerCase(), rating);
	}
	
	/**
	 * Gets the personal Ratings in this Database
	 * 
	 * @return all personal Ratings
	 */
	public synchronized Collection<Rating> getPersonalRating() {
		return _personalrating.values();
	}
	
	/**
	 * Gets one personal Rating for a specific Program
	 * 
	 * @param program get Rating for this Program
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
	 * @param title get rating for this Title
	 * @return the personal Rating
	 */
	public synchronized Rating getPersonalRating(String title) {
		if (title == null) {
			return null;
		}
		
		String lowerCaseTitle = title.toLowerCase();
		Rating rating = _personalrating.get(title);
		if (rating != null) {
			_personalrating.remove(title);
			_personalrating.put(lowerCaseTitle, rating);
			return rating;
		}
		
		rating = _personalrating.get(lowerCaseTitle); 
		return rating;
	}
	
	/**
	 * Saves the personal Rating in the Database
	 * 
	 * @param rating save this Rating
	 */
	public synchronized void setPersonalRating(Rating rating) {
		if (_personalrating.get(rating.getTitle()) != null) {
			_personalrating.remove(rating.getTitle());
		}
		
		if (_personalrating.get(rating.getTitle().toLowerCase()) == null) {
			_personalrating.put(rating.getTitle().toLowerCase(), rating);
		}
		
		if (!_changedpersonal.contains(rating)) {
			_changedpersonal.add(rating);
		}
	}
	
	public synchronized ArrayList<Rating> getChangedPersonal() {
		return _changedpersonal;
	}
	
	/**
	 * Empties the ChangedPersonal List
	 */
	public synchronized void clearChangedPersonal() {
		_changedpersonal = new ArrayList<Rating>();
	}
	
	/**
	 * Empties the ChangedPersonal List
	 */
	public synchronized void clearOverall() {
		_overalrating = new HashMap<String, Rating>();
	}
	
	/**
	 * Called by the host-application during start-up. Loads the data.
	 * 
	 * @see #writeData(ObjectOutputStream)
	 */
	public synchronized void readData(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		_personalrating = (HashMap<String, Rating>) in.readObject();
		_overalrating = (HashMap<String, Rating>) in.readObject();
		_changedpersonal = (ArrayList<Rating>) in.readObject();
	}
	
	/**
	 * Counterpart to loadData. Called when the application shuts down. Saves
	 * the data.
	 * 
	 * @see #readData(ObjectInputStream)
	 */
	public synchronized void writeData(ObjectOutputStream out) throws IOException {
		out.writeObject(_personalrating);
		out.writeObject(_overalrating);
		out.writeObject(_changedpersonal);
	}
}