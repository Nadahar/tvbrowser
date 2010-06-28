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

import java.util.ArrayList;
import java.util.Iterator;

import checkerplugin.ErrorIssue;
import checkerplugin.Issue;
import checkerplugin.WarningIssue;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * @author bananeweizen
 * 
 */
public abstract class AbstractCheck {
  protected static final util.ui.Localizer mLocalizer = util.ui.Localizer
  .getLocalizerFor(AbstractCheck.class);

	private ArrayList<Issue> mIssues;

	protected abstract void doCheck(final Program program);

	protected void addError(final String message) {
		mIssues.add(new ErrorIssue(message));
	}

	protected void addWarning(final String message) {
		mIssues.add(new WarningIssue(message));
	}

	public void check(final Program program, final ArrayList<Issue> issues) {
		mIssues = issues;
		doCheck(program);
	}
	
	protected static ArrayList<ProgramFieldType> getFieldTypes(final int format) {
	  ArrayList<ProgramFieldType> result = new ArrayList<ProgramFieldType>();
    final Iterator<ProgramFieldType> it = ProgramFieldType.getTypeIterator();
    while (it.hasNext()) {
      final ProgramFieldType fieldType = it.next();
      if (fieldType.getFormat() == format) {
        result.add(fieldType);
      }
    }
    return result;
	}
}
