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
package movieawardplugin;

import java.util.Locale;

import org.xml.sax.SAXException;

public class AwardNameHandler extends MovieAwardHandler {

	private String mAwardName;
	private String mSecondName;
	private String mAwardFileName;

	public AwardNameHandler(String awardName) {
		super(new MovieAward(null));
		mAwardFileName = awardName;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if (NAME.equals(qName)) {
			if (Locale.getDefault().getCountry().equalsIgnoreCase(mAttributes.getProperty(LANGUAGE))) {
				mAwardName = mText.toString();
				throw new StopParserException();
			}
			mSecondName = mText.toString();
		}
		if (URL.equals(qName)) {
			throw new StopParserException();
		}
	}

	public String getAwardName() {
		if (mAwardName == null) {
			mAwardName = mSecondName;
		}
		if (mAwardName == null) {
			mAwardName = mAwardFileName;
		}
		return mAwardName;
	}
}
