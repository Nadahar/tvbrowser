/*
 * Created on 07.12.2003
 */
package tvraterplugin;

import devplugin.Program;

/**
 * @author bodo
 */
public class TVRaterDB {

	/**
	 * @param _program
	 * @return
	 */
	public int[] getOverallRating(Program _program) {
		int[] rating = {1, 1, 1, 1, 1, 1};
		
		return rating;
	}

	int[] rating = {1, 1, 1, 1, 1, 1};
	

	/**
	 * @param _program
	 * @return
	 */
	public int[] getPersonalRating(Program _program) {
		return rating;
	}

	/**
	 * @param _program
	 * @param values
	 */
	protected void setPersonalRating(Program _program, int[] values) {
		rating = values;
	}

}