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
 */
package util.program;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import util.io.IOUtilities;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ImportanceValue;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * Provides utilities for program stuff.
 *
 * @author René Mach
 *
 */
public class ProgramUtilities {

  private static String ACTOR_ROLE_SEPARATOR = "\t\t-\t\t";

  /**
   * Helper method to check if a program runs.
   *
   * @param p
   *          The program to check.
   * @return True if the program runs.
   */
  public static boolean isOnAir(Program p) {
    Date currentDate = Date.getCurrentDate();
    if (currentDate.compareTo(p.getDate()) < 0) {
      return false;
    }
    int programNextDayEqualsToday = p.getDate().addDays(1).compareTo(currentDate);
    if (programNextDayEqualsToday < 0) {
      return false;
    }

    int time = IOUtilities.getMinutesAfterMidnight();
    if (programNextDayEqualsToday == 0) {
      time += 24 * 60;
    }
    if (p.getStartTime() <= time && (p.getStartTime() + p.getLength()) > time) {
      return true;
    }
    return false;
  }

  /**
   * comparator to sort programs by date, time and position in channel list
   * @return The comparator for programs.
   */
  public static Comparator<Program> getProgramComparator() {
    return sProgramComparator;
  }

  /**
   * Comparator to sort programs by date, time and position in channel list
   */
  private static Comparator<Program> sProgramComparator = new Comparator<Program>(){
    public int compare(Program p1, Program p2) {
      int res=p1.getDate().compareTo(p2.getDate());
      if (res!=0) {
        return res;
      }

      int minThis=p1.getStartTime();
      int minOther=p2.getStartTime();

      if (minThis<minOther) {
        return -1;
      }else if (minThis>minOther) {
        return 1;
      }

      int pos1 = ChannelList.getPos(p1.getChannel());
      int pos2 = ChannelList.getPos(p2.getChannel());
      if (pos1 < pos2) {
        return -1;
      }
      else if (pos1 > pos2) {
        return 1;
      }

      return 0;

    }
  };
  private static ArrayList<String> mListFirst;
  private static ArrayList<String> mListSecond;

  /**
   * A helper method to get if a program is not in a time range.
   *
   * @param timeFrom The beginning of the time range to check
   * @param timeTo The ending of the time range
   * @param p The program to check
   * @return If the program is not in the given time range.
   * @since 2.2.2
   */
  public static boolean isNotInTimeRange(int timeFrom, int timeTo, Program p) {
    int timeFromParsed = timeFrom;

    if(timeFrom > timeTo) {
      timeFromParsed -= 60*24;
    }

    int startTime = p.getStartTime();

    if(timeFrom > timeTo && startTime >= timeFrom) {
      startTime -= 60*24;
    }

    return (startTime < timeFromParsed || startTime > timeTo);
  }

  /**
   * get the actors and roles of a program
   *
   * @param program the program containing the actors
   * @return array of 2 lists, where one contains roles and the other actors
   * @since 2.6
   */
  public static ArrayList<String>[] splitActors(Program program) {
    String actorsField = program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE);
    if (actorsField != null) {
      String[] actors;
      // actor list separated by newlines
      if (actorsField.contains("\n")) {
        actors = actorsField.split("\n");
      }
      // actor list separated by colon
      else if (actorsField.contains(",")) {
        actors = actorsField.split(",");
      }
      // single actor
      else if (actorsField.contains("\t")) {
        actors = new String[1];
        actors[0] = actorsField;
      }
      // unknown format
      else {
        return null;
      }
      mListFirst = new ArrayList<String>();
      mListSecond = new ArrayList<String>();
      for (int i = 0; i < actors.length; i++) {
        String actor = actors[i];
        if (!actor.startsWith(ACTOR_ROLE_SEPARATOR)) {
          actor = actor.trim();
        }
        if (actor.endsWith(",")) {
          actor = actor.substring(0, actor.length() - 1).trim();
        }
        // if this actor has been deleted, do the next iteration
        if (StringUtils.isEmpty(actor)) {
          continue;
        }
        if (actor.contains(ACTOR_ROLE_SEPARATOR)) {
          addNames(nameFrom(StringUtils.substringBefore(actor, ACTOR_ROLE_SEPARATOR)),
          nameFrom(StringUtils.substringAfter(actor, ACTOR_ROLE_SEPARATOR)));
        }
        // actor and role separated by tab
        else if (actor.contains("\t")) {
          addNames(nameFrom(StringUtils.substringBefore(actor, "\t")),
          nameFrom(StringUtils.substringAfter(actor, "\t")));
        }
        // actor and role separated by colon
        else if (actor.contains(":")) {
          addNames(nameFrom(StringUtils.substringBefore(actor, ":")),
          nameFrom(StringUtils.substringAfter(actor, ":")));
        }
        // actor and role separated by brackets
        else if (actor.contains("(") || actor.contains(")")) {
          // maybe the splitting went wrong because of commas inside brackets
          if (actor.contains("(") && !actor.contains(")")) {
            if (i+1 < actors.length && actors[i+1].contains(")") && !actors[i+1].contains("(")) {
              actor = actor + "," + actors[i+1];
              actors[i+1] = "";
            }
          }
          if (actor.contains("(") && actor.contains(")") && actor.lastIndexOf(')') > actor.indexOf('(')) {
            String secondPart = nameFrom(actor.substring(
                actor.indexOf('(') + 1, actor.lastIndexOf(')')));
            // there are multiple brackets, lets look for something like "actor (age) (role)"
            if (secondPart.contains("(")) {
              Pattern agePattern = Pattern.compile(".*(\\(\\d+\\)).*");
              Matcher matcher = agePattern.matcher(actor);
              if (matcher.matches()) {
                String age = matcher.group(1);
                actor = nameFrom(StringUtils.substringBefore(actor, age) + StringUtils.substringAfter(actor, age));
                secondPart = nameFrom(actor.substring(actor.indexOf('(') + 1,
                    actor.lastIndexOf(')')));
              }
            }
            // only use a name with multiple brackets, if they are nested in the role part
            int indexOpen = secondPart.indexOf('(');
            int indexClose = secondPart.indexOf(')');
            if ((indexOpen == -1 && indexClose == -1) || (indexOpen < indexClose)) {
              addNames(nameFrom(StringUtils.substringBefore(actor, "(")),secondPart);
            }
            else {
              return null; // error: multiple brackets in one name
            }
          }
          else {
            return null; // error: only a left or only a right bracket
          }
        }
        else {
          mListFirst.add(nameFrom(actor));
        }
      }
      @SuppressWarnings("unchecked")
      ArrayList<String>[] lists = new ArrayList[2];
      lists[0] = mListFirst;
      lists[1] = mListSecond;
      return lists;
    }
    return null;
  }

  private static void addNames(final String firstName, final String secondName) {
    if (firstName.equalsIgnoreCase("und andere") || secondName.equalsIgnoreCase("und andere")) {
      return;
    }
    // avoid duplicates in the list (sometimes duplicates occur in the fields)
    int firstIndex = mListFirst.indexOf(firstName);
    if (firstIndex >= 0 && firstIndex == mListSecond.indexOf(secondName)) {
      return;
    }
    mListFirst.add(firstName);
    mListSecond.add(secondName);
  }

  /**
   * extract the actor names from the actor field
   *
   * @param program the program to work on
   * @return list of real actor names or null (if it can not be decided)
   * @since 2.6
   */
  public static String[] getActorNames(Program program) {
    String actorsField = program.getTextField(ProgramFieldType.ACTOR_LIST_TYPE);
    if (actorsField != null) {
      ArrayList<String>[] lists = splitActors(program);
      if (lists == null) {
        return null;
      }
      ArrayList<String> result;
      // use first list if the field has special formatting
      if (actorsField.contains(ACTOR_ROLE_SEPARATOR)) {
        result = lists[0];
      }
      // otherwise do a statistical investigation
      else {
        result = separateRolesAndActors(lists, program);
      }
      if (result != null) {
        String[] array = new String[result.size()];
        result.toArray(array);
        return array;
      }
    }
    return null;
  }

  private static String nameFrom(String name) {
    name = name.trim();
    if (name.endsWith(",")) {
      name = name.substring(0, name.length() - 1);
    }
    // remove surrounding brackets
    if (name.length() > 1 && name.startsWith("(") && name.endsWith(")")
        && name.indexOf('(', 1) < 0
        && name.lastIndexOf(')', name.length() - 2) < 0) {
      name = name.substring(1, name.length()-1);
    }
    // filter wrong dataservice actors
    if (name.equals("null")) {
      return "";
    }
    if (name.equals("-")) {
      return "";
    }
    return name;
  }

  /**
   * decide which of the 2 lists contains the real actor names and which
   * the role names by using statistical methods
   *
   * @param program
   * @param listFirst first list of names
   * @param listSecond second list of names
   * @since 2.6
   */
  private static ArrayList<String> separateRolesAndActors(ArrayList<String>[] list, Program program) {
    // return first list, if only one name per actor is available
    if (list[1].size() == 0) {
      return list[0];
    }
    // get directors names
    String[] directors = new String[0];
    String directorField = program.getTextField(ProgramFieldType.DIRECTOR_TYPE);
    if (directorField != null) {
      directors = directorField.split(",");
    }
    // get script writers
    String[] scripts = new String[0];
    String scriptField = program.getTextField(ProgramFieldType.SCRIPT_TYPE);
    if (scriptField != null) {
      scripts = scriptField.split(",");
    }
    String lowerTitle = program.getTitle().toLowerCase();
    for (ArrayList<String> element : list) {
      // search for director in actors list
      for (String director : directors) {
        if (element.contains(director)) {
          return element;
        }
      }
      // search for script in actors list
      for (String script : scripts) {
        if (element.contains(script)) {
          return element;
        }
      }
    }
    // which list contains more names with one part only (i.e. no family name) -> role names
    int[] singleName = new int[list.length];
    // which list contains more abbreviations at the beginning -> role names
    int[] abbreviation = new int[list.length];
    // which list contains more slashes -> double roles for a single actor
    int[] slashes = new int[list.length];
    // which list has duplicate family names -> roles
    @SuppressWarnings("unchecked")
    HashMap<String,Integer>[] familyNames = new HashMap[list.length];
    int[] maxNames = new int[list.length];
    // which list contains strings with consecutive uppercase letters -> roles
    int[] uppercase = new int[list.length];
    Pattern consecUpper = Pattern.compile(".*[A-Z]{2,}.*");
    // which list contains more role indication words
    int[] roleWord = new int[list.length];
    String[] roleIndications = new String[] {"der", "die", "das"};
    for (int i = 0; i < list.length; i++) {
      familyNames[i] = new HashMap<String, Integer>();
      for (String name : list[i]) {
        if (!name.contains(" ")) {
          singleName[i]++;
        }
        else {
          String familyName = StringUtils.substringAfter(name, " ");
          Integer count = 1;
          if (familyNames[i].containsKey(familyName)) {
            count = familyNames[i].get(familyName);
            count = count.intValue() + 1;
          }
          familyNames[i].put(familyName, count);
        }
        // only count abbreviations at the beginning, so we do not count a middle initial like in "Jon M. Doe"
        if (name.contains(".") && (name.indexOf(".") < name.indexOf(" "))) {
          abbreviation[i]++;
        }
        if (name.contains("/")) {
          slashes[i]++;
        }
        Matcher matcher = consecUpper.matcher(name);
        if (matcher.matches()) {
          uppercase[i]++;
        }
        // check if there are words which should never occur in an actor name
        String[] nameParts = name.split(" ");
        for (String namePart: nameParts) {
          for (String roleIndication : roleIndications) {
            if (namePart.equalsIgnoreCase(roleIndication)) {
              roleWord[i]++;
            }
          }
        }
      }
      for (Integer familyCount : familyNames[i].values()) {
        if (familyCount.intValue() > maxNames[i]) {
          maxNames[i] = familyCount.intValue();
        }
      }
    }
    // now evaluate our statistics
    if (roleWord[0] < roleWord[1]) {
      return list[0];
    }
    else if (roleWord[1] < roleWord[0]) {
      return list[1];
    }
    else if (slashes[0] < slashes[1]) {
      return list[0];
    }
    else if (slashes[1] < slashes[0]) {
      return list[1];
    }
    else if (singleName[0] < singleName[1]) {
      return list[0];
    }
    else if (singleName[1] < singleName[0]) {
      return list[1];
    }
    else if (uppercase[0] < uppercase[1]) {
      return list[0];
    }
    else if (uppercase[1] < uppercase[0]) {
      return list[1];
    }
    else if (abbreviation[0] < abbreviation[1]) {
      return list[0];
    }
    else if (abbreviation[1] < abbreviation[0]) {
      return list[1];
    }
    else if (maxNames[0] < maxNames[1]) {
      return list[0];
    }
    else if (maxNames[1] < maxNames[0]) {
      return list[1];
    }
    else {
      // search for role in program title
      for (int i = 0; i < list.length; i++) {
        for (int j = 0; j < list[i].size(); j++) {
          if (lowerTitle.contains(list[i].get(j).toLowerCase())) {
            if (lowerTitle.contains(" in:")) {
              return list[i]; // "Jon Doe in: Some title"
            }
            else {
              return list[1-i]; // "Indiana Jones 2"
            }
          }
        }
      }
      // search for role part in program title
      String[] titleParts = lowerTitle.split(" ");
      for (String part : titleParts) {
        if (part.length() >= 4) {
          for (int i = 0; i < list.length; i++) {
            for (int j = 0; j < list[i].size(); j++) {
              if (list[i].get(j).toLowerCase().contains(part)) {
                return list[1 - i]; // "Edel & Starck"
              }
            }
          }
        }
      }
      return null;
    }
  }

  /**
   * Gets the time zone corrected program id of the given program id.
   * If the current time zone is the same like the time zone of the given
   * id the given id will be returned.
   * <p>
   * @param progID The id to get the time zone corrected for.
   * @return The time zone corrected program id.
   * @since 2.7
   */
  public static String getTimeZoneCorrectedProgramId(String progID) {
    int index = progID.lastIndexOf('_');
    String timeString = progID.substring(index + 1);
    int hourIndex = timeString.indexOf(':');
    int offsetIndex = timeString.lastIndexOf(':');

    if(hourIndex != offsetIndex) {
      int timeZoneOffset = Integer.parseInt(timeString.substring(offsetIndex + 1));
      int currentTimeZoneOffset = TimeZone.getDefault().getRawOffset()/60000;

      if(timeZoneOffset != currentTimeZoneOffset) {
        String[] hourMinute = timeString.split(":");
        int timeZoneDiff = currentTimeZoneOffset - timeZoneOffset;

        int hour = Integer.parseInt(hourMinute[0]) + (timeZoneDiff/60);
        int minute = Integer.parseInt(hourMinute[1]) + (timeZoneDiff%60);

        if(hour >= 24) {
          hour -= 24;
        }
        else if(hour < 0) {
          hour += 24;
        }

        hourMinute[0] = String.valueOf(hour);
        hourMinute[1] = String.valueOf(minute);
        hourMinute[2] = String.valueOf(currentTimeZoneOffset);

        StringBuilder newId = new StringBuilder(progID.substring(0, index + 1));
        newId.append(hourMinute[0]).append(":").append(hourMinute[1]).append(":").append(hourMinute[2]);

        return newId.toString();
      }
    }
    else {
      String[] hourMinute = timeString.split(":");
      StringBuilder newId = new StringBuilder(progID.substring(0, index + 1));
      newId.append(hourMinute[0]).append(":").append(hourMinute[1]).append(":").append(TimeZone.getDefault().getRawOffset()/60000);

      return newId.toString();
    }

    return progID;
  }

  /**
   * extract a list of person names out of the given string
   *
   * @param field The field.
   * @return list of person names
   */
  public static String[] splitPersons(final String field) {
    if (field == null) {
      return new String[0];
    }
    String[] items;
    if (field.contains("\n")) {
      items = field.split("\n|( und | and | \\& )");
    }
    else if (field.contains(",")) {
      items = field.split(",|( und | and | \\& )");
    }
    else if (field.contains(" und ") || field.contains(" and ") || field.contains(" & ")) {
      items = field.split(" und | and | \\& ");
    }
    else {
      items = new String[1];
      items[0] = field;
    }
    for (int i = 0; i < items.length; i++) {
      items[i] = items[i].trim();
      if (items[i].endsWith(",") || items[i].endsWith(".")) {
        items[i] = items[i].substring(0, items[i].length() - 1);
      }
    }
    return items;
  }

  /**
   * get the age limit for a given textual rating
   *
   * @param rating The rating.
   * @return age limit or -1
   * @since 3.0
   */
  public static int getAgeLimit(final String rating) {
    if (rating == null || rating.isEmpty()) {
      return -1;
    }
    if (rating.contains(",")) {
      String[] ratings = rating.split(",");
      int result = -1;
      for (String r : ratings) {
        result = Math.max(result, getAgeLimit(r.trim()));
      }
      return result;
    }
    if (rating.equalsIgnoreCase("NR") || rating.equalsIgnoreCase("Unrated")) {
      return -1;
    }
    // MPAA
    if (rating.equalsIgnoreCase("G")) {
      return 0;
    }
    if (rating.equalsIgnoreCase("PG-13")) {
      return 13;
    }
    if (rating.equalsIgnoreCase("NC-17")) {
      return 18;
    }
    if (rating.equalsIgnoreCase("M")) {
      return 15;
    }
    // X-Rating
    if (rating.startsWith("X")) {
      return 18;
    }
    // FCC
    if (rating.startsWith("TV-Y7")) {
      return 7;
    }
    if (rating.startsWith("TV-Y")) {
      return 0;
    }
    if (rating.startsWith("TV-14")) {
      return 14;
    }
    if (rating.startsWith("TV-M")) {
      return 17;
    }
    // BBFC, Great Britain
    if (rating.equalsIgnoreCase("UC")) {
      return 0;
    }
    if (rating.equalsIgnoreCase("U")) {
      return 3;
    }
    if (rating.equalsIgnoreCase("PG")) {
      return 7;
    }
    if (rating.startsWith("R18") || rating.equals("R")) {
      return 18;
    }
    // Italy
    if (rating.equalsIgnoreCase("T")) {
      return 0;
    }
    if (rating.equalsIgnoreCase("VM14")) {
      return 14;
    }
    if (rating.equalsIgnoreCase("VM18")) {
      return 18;
    }
    // MPAA/TV ratings without age
    if (rating.equals("TV-G") || rating.equals("TV-PG")) {
      return -1;
    }
    // German FSK
    if (rating.toUpperCase().startsWith("FSK")) {
      String num = rating.substring(3).trim();
      if (num.startsWith("-")) {
        num = num.substring(1).trim();
        try {
          int number = Integer.parseInt(num);
          return number;
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
      }
    }
    // numerical codes
    try {
      int number = Integer.parseInt(rating);
      return number;
    } catch (NumberFormatException e) {
      // ignore, this wasn't a numerical code
    }
    System.out.println("Unknown rating code: " + rating);
    return -1;
  }

  /**
   * Creates a color that uses the program importance for alpha value.
   * <p>
   * @param color The color to use for alpha value change.
   * @param program The program to get the changed color for.
   * @return The color that uses the program importance alpha value.
   * @since 3.1
   */
  public static Color getColorForProgramImportance(Color color, Program program) {
    return getColorForProgramImportance(color, getProgramImportance(program));
  }
  
  /**
   * Creates a color that uses the program importance for alpha value.
   * <p>
   * @param color The color to use for alpha value change.
   * @param importance The importance to get the changed color for.
   * @return The color that uses the program importance alpha value.
   * @since 3.1
   */
  public static Color getColorForProgramImportance(Color color, byte importance) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha()*importance/10.));
  }
  
  /**
   * Gets the program importance of the given program.
   * <p>
   * @param program The program to get the program importance for
   * @return The program importance from 0 to 10 for the given program.
   * @since 3.1
   */
  public static byte getProgramImportance(Program program) {
    if (program.getProgramState() == Program.STATE_IS_VALID &&
        Settings.propProgramPanelAllowTransparency.getBoolean()) {
      int count = 0;
      int addValue = 0;

      PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();

      for(PluginProxy plugin : plugins) {
        ImportanceValue value = plugin.getImportanceValueForProgram(program);

        if(value.getWeight() > 0 && value.getTotalImportance() >= Program.PRIORITY_MARK_MIN) {
          count += value.getWeight();
          addValue += value.getTotalImportance();
        }
      }

      if(count > 0) {
        return (byte)Math.max(addValue/count, Program.PRIORITY_MARK_MIN);
      }
    }

    return Program.IMPORTANCE_PROGRAM_MAX;
  }
  
  /** 
   * Gets an iterator that contains all programs for the given date of the given
   * channel and it's joint channel if there one or it's base channel if there one. 
   * <p>
   * @param date The date to get the iterator for.
   * @param channel The channel to get the iterator for.
   * @return An iterator of programs.
   * @since 3.2.1
   */
  public static Iterator<Program> getJointProgramIteratorFor(Date date, Channel channel) {
    Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(date, channel);
    
    if(channel.getJointChannel() != null || channel.getBaseChannel() != null) {      
      ArrayList<Program> progList = new ArrayList<Program>();
      
      if(it != null) {
        while(it.hasNext()) {
          progList.add(it.next());
        }
      }
      
      it = Plugin.getPluginManager().getChannelDayProgram(date, (channel.getJointChannel() != null ? channel.getJointChannel() : channel.getBaseChannel()));

      if(it != null) {
        while(it.hasNext()) {
          progList.add(it.next());
        }
      }
      
      Collections.sort(progList, ProgramUtilities.getProgramComparator());
      
      it = progList.iterator();
    }
    
    return it;
  }
  
  /**
   * Gets combined day program for yesterday, today and tomorrow.
   * <p>
   * @param today The date of today.
   * @param channel The channel to get the programs for
   * @param excludeExpired If expired programs should be excluded
   * @return An ArrayList with the found programs.
   * @since 3.4.1
   */
  public static ArrayList<Program> getJointProgramListForYesterdayTodayTomorrow(Date today, Channel channel, boolean excludeExpired) {
    ArrayList<Program> progList = new ArrayList<Program>();
    
    Iterator<Program> yesterday = getJointProgramIteratorFor(today.addDays(-1),channel);
    Iterator<Program> todayIterator = getJointProgramIteratorFor(today,channel);
    Iterator<Program> tomorrow = getJointProgramIteratorFor(today.addDays(1),channel);
    
    addProgramsFromIteratorToList(yesterday,progList,excludeExpired);
    addProgramsFromIteratorToList(todayIterator,progList,excludeExpired);
    addProgramsFromIteratorToList(tomorrow,progList,excludeExpired);
    
    return progList;
  }
  
  private static void addProgramsFromIteratorToList(Iterator<Program> iterator, ArrayList<Program> toAdd, boolean excludeExpired) {
    if(iterator != null) {
      while(iterator.hasNext()) {
        Program test = iterator.next();
        
        if(!excludeExpired || !test.isExpired()) {
          toAdd.add(test);
        }
      }
    }
  }
}