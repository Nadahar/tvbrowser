/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package devplugin;

import java.util.Iterator;

import javax.swing.event.ChangeListener;

/**
 * This interface provides a view of the program object in the host-application.
 *
 * @author Martin Oberhauser
 */
public interface Program {

  /**
   * black and white, no color
   */
  public static final int INFO_VISION_BLACK_AND_WHITE  = 1 << 1;
  
  /**
   * video aspect ratio 4:3
   */
  public static final int INFO_VISION_4_TO_3           = 1 << 2;
  
  /**
   * video aspect ratio 16:9 (widescreen)
   */
  public static final int INFO_VISION_16_TO_9          = 1 << 3;
  
  /**
   * single channel audio
   */
  public static final int INFO_AUDIO_MONO              = 1 << 4;
  
  /**
   * double channel audio
   */
  public static final int INFO_AUDIO_STEREO            = 1 << 5;
  
  /**
   * dolby surround audio
   */
  public static final int INFO_AUDIO_DOLBY_SURROUND    = 1 << 6;
  
  /**
   * dolby digital 5.1 audio
   */
  public static final int INFO_AUDIO_DOLBY_DIGITAL_5_1 = 1 << 7;
  
  /**
   * Audio channels with different languages are available.
   */
  public static final int INFO_AUDIO_TWO_CHANNEL_TONE  = 1 << 8;

  /**
   * A subtitle for aurally handicapped. The subtitle is in the same language as
   * the audio.
   * 
   * @see #INFO_ORIGINAL_WITH_SUBTITLE
   */
  public static final int INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED = 1 << 9;
  /**
   * Program is live.
   */
  public static final int INFO_LIVE                    = 1 << 10;
  /**
   * Original with subtitle. The subtitle is in another language than
   * the audio.
   *
   * @see #INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
   */
  public static final int INFO_ORIGINAL_WITH_SUBTITLE  = 1 << 11;
  
  /**
   * This program is a movie.
   * @since 2.6/2.2.4
   */
  public static final int INFO_CATEGORIE_MOVIE = 1 << 12;

  /**
   * This program is a series.
   * @since 2.6/2.2.4
   */
  public static final int INFO_CATEGORIE_SERIES = 1 << 13;

  /**
   * This program has not been on air before.
   * @since 2.6/2.2.4
   */
  public static final int INFO_NEW = 1 << 14;

  /**
   * Audio description is available for people with limited vision.
   * @since 2.6/2.2.4
   */
  public static final int INFO_AUDIO_DESCRIPTION = 1 << 15;

  /**
   * This program is News
   * @since 2.6/2.2.4
   */
  public static final int INFO_CATEGORIE_NEWS = 1 << 16;

  /**
   * This program is Show
   * @since 2.6/2.2.4
   */
  public static final int INFO_CATEGORIE_SHOW = 1 << 17;

  /**
   * This program is a magazine or infotainment
   * @since 2.6/2.2.4
   */
  public static final int INFO_CATEGORIE_MAGAZINE_INFOTAINMENT = 1 << 18;

  /**
   * video high definition quality
   * @since 2.7
   */
  public static final int INFO_VISION_HD          = 1 << 19;

  /**
   * This program is a documentary or a feature
   * @since 2.7
   */
  public static final int INFO_CATEGORIE_DOCUMENTARY = 1 << 20;
  
  /**
   * This program is arts, theater, music
   * @since 2.7
   */
  public static final int INFO_CATEGORIE_ARTS = 1 << 21;
  
  /**
   * This program is sports
   * @since 2.7
   */
  public static final int INFO_CATEGORIE_SPORTS = 1 << 22;
  
  /**
   * This program is especially suitable for children
   * @since 2.7
   */
  public static final int INFO_CATEGORIE_CHILDRENS = 1 << 23;
  
  /**
   * This program does not fit in any of the other categories
   * @since 2.7
   */
  public static final int INFO_CATEGORIE_OTHERS = 1 << 24;

  /**
   * This program has sign language
   * @since 2.7
   */
  public static final int INFO_SIGN_LANGUAGE = 1 << 25;

  public static final int IS_VALID_STATE = 0;
  public static final int WAS_UPDATED_STATE = 1;
  public static final int WAS_DELETED_STATE = 2;

  public static final int NO_MARK_PRIORITY = -1;
  public static final int MIN_MARK_PRIORITY = 0;
  public static final int LOWER_MEDIUM_MARK_PRIORITY = 1;
  public static final int MEDIUM_MARK_PRIORITY = 2;
  public static final int HIGHER_MEDIUM_MARK_PRIORITY = 3;
  public static final int MAX_MARK_PRIORITY = 4;
  
  public static final byte DEFAULT_PROGRAM_IMPORTANCE = -1;
  public static final byte MIN_PROGRAM_IMPORTANCE = 1;
  public static final byte LOWER_MEDIUM_PROGRAM_IMPORTANCE = 3;
  public static final byte MEDIUM_PROGRAM_IMPORTANCE = 5;
  public static final byte HIGHER_MEDIUM_PROGRAM_IMPORTANCE = 7;
  public static final byte MAX_PROGRAM_IMPORTANCE = 10;
  
  /** The key for the value of an action, to put the mark priority in
   * @since 2.6 */
  public static final String MARK_PRIORITY = "MARK_PRIORITY";

  /**
   * Adds a ChangeListener to the program.
   *
   * @param listener the ChangeListener to add
   * @see #removeChangeListener
   */
  public void addChangeListener(ChangeListener listener);
  
  /**
   * Removes a ChangeListener from the program.
   *
   * @param listener the ChangeListener to remove
   * @see #addChangeListener
   */
  public void removeChangeListener(ChangeListener listener);
  
  /**
   * Gets the ID of this program. This ID is unique for a certain date.
   * This value becomes invalid when changing the application time zone.
   *
   * @return The ID of this program.
   */
  public String getID();
  
  /**
   * Gets the unique ID of this program. In contrast to getID() this method
   * returns an ID, which is unique for all programs.
   * This value becomes invalid when changing the application time zone.
   *
   * @return An unique ID of this program.
   */
  public String getUniqueID();

  public String getTitle();
  public String getShortInfo();
  
  /**
   * Gets the description of the program
   * 
   * @return the description or <code>null</code>, if no description is available
   */
  public String getDescription();

  /**
   * Gets the the start time of the program in minutes after midnight.
   *
   * @return the start time.
   */
  public int getStartTime();

  /**
   * Gets the hour-component of the start time of the program.
   * This is local time (i.e. for the current time zone).
   * This value becomes invalid when changing the time zone!
   *
   * @return the hour-component of the start time.
   */
  public int getHours();

  /**
   * Gets the minute-component of the start time of the program.
   * This is local time (i.e. for the current time zone).
   * This value becomes invalid when changing the time zone!
   * 
   * @return the minute-component of the start time.
   */
  public int getMinutes();

  /**
   * Gets the length of this program in minutes.
   * 
   * @return the length in minutes or -1, if the length is unknown.
   */
  public int getLength();
  
  public int getInfo();
  
  /**
   * get the start time of this program as nicely formatted string
   * @return the start time string
   */
  public String getTimeString();
  
  /**
   * get the date of this program as nicely formatted string
   * @return the date string
   */
  public String getDateString();
  
  /**
   * get the end time of this program as nicely formatted string
   * @return the end time string
   * @since 2.5.3
   */
  public String getEndTimeString();

  public Channel getChannel();
  
  /**
   * get the date when the program starts.
   * @return the date
   */
  public Date getDate();

  /**
   * Gets the value of a binary field from the program.
   * 
   * @param type The type of the wanted field. Must have a binary format.
   * @return The value of the field or <code>null</code>, if there is no
   *         value for this field.
   */
  public byte[] getBinaryField(ProgramFieldType type);

  /**
   * Gets the value of a text field from the program.
   * 
   * @param type The type of the wanted field. Must have a text format.
   * @return The value of the field or <code>null</code>, if there is no
   *         value for this field.
   */
  public String getTextField(ProgramFieldType type);

  /**
   * Gets the value of a int field from the program.
   * 
   * @param type The type of the wanted field. Must have a int format.
   * @return The value of the field or <code>-1</code>, if there is no
   *         value for this field.
   */
  public int getIntField(ProgramFieldType type);

  /**
   * Gets the value of a int field as String.
   * 
   * @param type The type of the wanted field. Must have a int format.
   * @return The value of the field as String or <code>null</code>, if there is
   *         no value for this field.
   */
  public String getIntFieldAsString(ProgramFieldType type);

  /**
   * Gets the value of a time field from the program.
   * 
   * @param type The type of the wanted field. Must have a time format.
   * @return The value of the field or <code>-1</code>, if there is no
   *         value for this field.
   */
  public int getTimeField(ProgramFieldType type);

  /**
   * Gets the value of a time field as String of the pattern "h:mm".
   * 
   * @param type The type of the wanted field. Must have a time format.
   * @return The value of the field as String or <code>null</code>, if there is
   *         no value for this field.
   */
  public String getTimeFieldAsString(ProgramFieldType type);
  
  /**
   * Gets the number of fields this program has.
   * 
   * @return the number of fields this program has.
   */
  public int getFieldCount();
  
  /**
   * Gets an iterator over the types of all fields this program has.
   * 
   * @return an iterator over {@link ProgramFieldType}s.
   */
  public Iterator<ProgramFieldType> getFieldIterator();

  /**
   * Marks the program for a Java plugin.
   * 
   * @param javaPlugin The plugin to mark the program for.
   */
  public void mark(Plugin javaPlugin);

  /**
   * Removes the marks from the program for a Java plugin.
   * <p>
   * If the program wasn't marked for the plugin, nothing happens.
   * 
   * @param javaPlugin The plugin to remove the mark for.
   */
  public void unmark(Plugin javaPlugin);

  /**
   * Marks the program for a plugin.
   * 
   * @param plugin The plugin to mark the program for.
   */
  public void mark(Marker plugin);

  /**
   * Removes the marks from the program for a plugin.
   * <p>
   * If the program wasn't marked for the plugin, nothing happens.
   * 
   * @param plugin The plugin to remove the mark for.
   */
  public void unmark(Marker plugin);
  
  /**
   * Gets whether this program is marked as "on air".
   */
  public boolean isOnAir();

  /**
   * Gets all {@link Marker}s that have marked this program.
   */
  public Marker[] getMarkerArr();

  /**
   * Gets whether this program is expired.
   */
  public boolean isExpired();
  
  /**
   * Returns the state of this program.
   * 
   * @return The program state.
   * @since 2.2
   */
  public int getProgramState();
  
  /**
   * Informs the ChangeListeners for repainting if a Plugin
   * uses more than one Icon for the Program.
   *
   * @see Plugin#getMarkIconsForProgram(Program)
   * @since 2.5
   */
  public void validateMarking();
  
  /**
   * Gets the priority of the marking of this program.
   * 
   * @return The mark priority.
   * @since 2.5.1
   */
  public int getMarkPriority();
  
  /**
   * checks if the given field has a non-<code>null</code> value. use this
   * method if you don't need the value of the field, but only the knowledge
   * about existence
   * 
   * @param type
   *          field type
   * @return field is set
   * @since 3.0
   */
  public boolean hasFieldValue(ProgramFieldType type);
}
