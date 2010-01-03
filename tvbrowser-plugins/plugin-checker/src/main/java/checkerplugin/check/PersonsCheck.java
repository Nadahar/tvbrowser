/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package checkerplugin.check;

import java.util.Arrays;
import java.util.HashSet;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class PersonsCheck extends AbstractCheck {

	@Override
	protected void doCheck(final Program program) {
    checkPersonFields(ProgramFieldType.ACTOR_LIST_TYPE, program);
    checkPersonFields(ProgramFieldType.DIRECTOR_TYPE, program);
    checkPersonFields(ProgramFieldType.CAMERA_TYPE, program);
    checkPersonFields(ProgramFieldType.CUTTER_TYPE, program);
    checkPersonFields(ProgramFieldType.MODERATION_TYPE, program);
    checkPersonFields(ProgramFieldType.ADDITIONAL_PERSONS_TYPE, program);
	}

  private void checkPersonFields(final ProgramFieldType fieldType, final Program program) {
    String field = program.getTextField(fieldType);
    if (field != null) {
      // TODO use ProgramUtilities instead
      //String[] persons = ProgramUtilities.splitPersons(field);
      String[] persons = splitPersons(field);
      if (persons != null) {
        for (String person : persons) {
          if (person.isEmpty()) {
            addError(mLocalizer.msg("name.empty", "Person name is empty in {0}", fieldType.getLocalizedName()));
          } else {
            char firstChar = person.charAt(0);
            if (! (Character.isLetter(firstChar) || firstChar == '\'')) {
              addError(mLocalizer.msg("name.starts", "Person name starts with non whitespace in {0}", fieldType.getLocalizedName()));
            }
          }
          if (person.contains("und andere")) {
          	addError(mLocalizer.msg("name.andothers", "Actor list contains 'and others'"));
          }
        }
        HashSet<String> set = new HashSet<String>(Arrays.asList(persons));
        if (set.size() != persons.length) {
          addError(mLocalizer.msg("name.duplicate", "Person name duplicate in {0}", fieldType.getLocalizedName()));
        }
      }
    }
  }

  /**
   * extract a list of person names out of the given string
   * 
   * @param field
   * @return list of person names
   */
  private static String[] splitPersons(final String field) {
    if (field == null) {
      return new String[0];
    }
    String[] items;
    if (field.contains("\n")) {
      items = field.split("\n|( und )");
    }
    else if (field.contains(",")) {
      items = field.split(",|( und )");
    }
    else if (field.contains(" und ")) {
      items = field.split(" und ");
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

}
