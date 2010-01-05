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

import java.util.Iterator;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class ArticleCheck extends AbstractCheck {

  private static final String[] ARTICLES = new String[] {", Der", ", Die", ", Das", ", The", ", A"};

  @Override
  protected void doCheck(final Program program) {
    final Iterator<ProgramFieldType> it = ProgramFieldType.getTypeIterator();
    while (it.hasNext()) {
      final ProgramFieldType fieldType = it.next();
      if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
        final String content = program.getTextField(fieldType);
        if (content != null) {
          for (String article : ARTICLES) {
            if (content.endsWith(article)) {
              addError(mLocalizer.msg("article", "Field {0} ends with article", fieldType.getLocalizedName()));
            }
          }
        }
      }
    }
  }

}
