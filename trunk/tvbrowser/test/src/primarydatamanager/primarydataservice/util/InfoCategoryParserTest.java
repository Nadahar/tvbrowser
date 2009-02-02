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
package primarydatamanager.primarydataservice.util;

import junit.framework.TestCase;
import devplugin.Program;


public class InfoCategoryParserTest extends TestCase {
  private InfoCategoryParser mParser = new InfoCategoryParser();

  public void testCategories() {
    assertCategory("akjdlajskdajsfh", 0);
    assertCategory("Wrestling", Program.INFO_CATEGORIE_SPORTS);
    assertCategory("Kinder-Serie", Program.INFO_CATEGORIE_SERIES);
    assertCategory("Oper", Program.INFO_CATEGORIE_ARTS);
    assertCategory("Western", Program.INFO_CATEGORIE_MOVIE);
    assertCategory("Western, Schicksalsdrama, Melodram",
        Program.INFO_CATEGORIE_MOVIE);
  }

  private void assertCategory(String genre, int category) {
    assertEquals(mParser.getCategory(genre), category);
  }
}
