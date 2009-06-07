package de.misi.tvbrowser.activities.settings;

import de.misi.tvbrowser.data.Group;

public class Settings {

   private static Group[] groups = new Group[0];

   public static Group[] getGroups() {
      return groups;
   }

   public static void setGroups(Group[] groups) {
      Settings.groups = groups;
   }
}
