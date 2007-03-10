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

import javax.swing.event.ChangeListener;
import java.util.Iterator;

/**
 * This interface provides a view of the program object in the host-application.
 *
 * @author Martin Oberhauser
 */
public interface Program {

  public static final int INFO_VISION_BLACK_AND_WHITE  = 1 << 1;
  public static final int INFO_VISION_4_TO_3           = 1 << 2;
  public static final int INFO_VISION_16_TO_9          = 1 << 3;
  public static final int INFO_AUDIO_MONO              = 1 << 4;
  public static final int INFO_AUDIO_STEREO            = 1 << 5;
  public static final int INFO_AUDIO_DOLBY_SURROUND    = 1 << 6;
  public static final int INFO_AUDIO_DOLBY_DIGITAL_5_1 = 1 << 7;
  public static final int INFO_AUDIO_TWO_CHANNEL_TONE  = 1 << 8;

  public static final int IS_VALID_STATE = 0;
  public static final int WAS_UPDATED_STATE = 1;
  public static final int WAS_DELETED_STATE = 2;
  
  public static final int DEFAULT_MARK_PRIORITY = 0;
  public static final int MIN_MARK_PRIORITY = 1;
  public static final int MEDIUM_MARK_PRIORITY = 2;
  public static final int MAX_MARK_PRIORITY = 3;
  
  /**
   * A subtitle for aurally handicapped. The subtitle is in the same language as
   * the audio.
   * 
   * @see #INFO_ORIGINAL_WITH_SUBTITLE
   */
  public static final int INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED = 1 << 9;
  public static final int INFO_LIVE                    = 1 << 10;
  /**
   * Original with subtitle. The subtitle is in another language as
   * the audio.
   *
   * @see #INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
   */
  public static final int INFO_ORIGINAL_WITH_SUBTITLE  = 1 << 11;
  
  /**
   * @deprecated Use {@link #INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED}
   *             or {@link #INFO_ORIGINAL_WITH_SUBTITLE} instead.
   */
  public static final int INFO_SUBTITLE = INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED;
  
  
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
   *
   * @return The ID of this program.
   */
  public String getID();

  public String getTitle();
  public String getShortInfo();
  public String getDescription();

  /**
   * Gets the the start time of the program in minutes after midnight.
   *
   * @return the start time.
   */
  public int getStartTime();

  /**
   * Gets the hour-component of the start time of the program.
   *
   * @return the hour-component of the start time.
   */
  public int getHours();

  /**
   * Gets the minute-component of the start time of the program.
   *
   * @return the minute-component of the start time.
   */
  public int getMinutes();
  
  /**
   * Gets the length of this program in minutes.
   *
   * @return the length.
   */
  public int getLength();
  
  public int getInfo();
  public String getTimeString();
  public String getDateString();

  public Channel getChannel();
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
  public Iterator getFieldIterator();

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
   * Gets all {@link PluginAccess}s that have marked this program.
   * @deprecated Use {@link #getMarkerArr}
   */
  public PluginAccess[] getMarkedByPlugins();

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
}