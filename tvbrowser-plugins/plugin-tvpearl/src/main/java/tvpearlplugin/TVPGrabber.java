/*
 * TV-Pearl by Reinhard Lehrbaum
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
package tvpearlplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TVPGrabber
{
  /**
   * German three letter abbreviation of the month names
   */
  private static String[] MONTH_ABBREVIATION = { "Jan", "Feb", "Mrz", "Apr",
      "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez" };
  
  /**
   * German weekday names
   */
  private static String[] WEEKDAY_NAME = { "Montag", "Dienstag", "Mittwoch",
      "Donnerstag", "Freitag", "Samstag", "Sonntag" };

  /**
   * regular expression to grab the content of a TV pearl
   */
  private static Pattern PATTERN_CONTENT = Pattern
      .compile("<p class=\"author\"><a href=\"([^\"]*)\">.*?<a href=\"./memberlist.php?[^\"]*\"[^>]*>(.*?)</a></strong> am (.*?)</p>[\\r\\n\\t ]*?<div class=\"content\">([\\w\\W]*?)(<dl class=\"postprofile\"|<div[^>]*class=\"signature\">|<div class=\"notice\")");

  /**
   * regular expression to grab the URL of the next forum page
   */
  private static Pattern PATTERN_NEXT_URL = Pattern
      .compile("<a href=\"([^\"]*?)\"[^>]*>Nächste</a>");

  private boolean mRecursiveGrab = true;
  private boolean mOnlyProgrammInFuture = true;
	private String lastUrl = "";
	private HTTPConverter mConverter;

	public TVPGrabber()
	{
		mConverter = new HTTPConverter();
	}

	public boolean getOnlyProgrammInFuture()
	{
		return mOnlyProgrammInFuture;
	}

	public void setOnlyProgrammInFuture(boolean onlyProgrammInFuture)
	{
		mOnlyProgrammInFuture = onlyProgrammInFuture;
	}

	public boolean getRecusiveGrab()
	{
		return mRecursiveGrab;
	}

	public void setRecusiveGrab(boolean recursive)
	{
		mRecursiveGrab = recursive;
	}

	public String getLastUrl()
	{
		return lastUrl;
	}

	public List<TVPProgram> Parse(String url)
	{
		List<TVPProgram> programList = new ArrayList<TVPProgram>();

		if (url.length() > 0)
		{
			String workingUrl = url.trim();
			do
			{
				if (workingUrl.length() > 0)
				{
					lastUrl = workingUrl;
				}
				String webContent = downloadUrl(workingUrl);

				workingUrl = extentUrl(getNextUrl(webContent), url);
				parseContent(webContent, programList, url);
			}
			while (workingUrl.length() > 0 && mRecursiveGrab);

			if (workingUrl.length() > 0)
			{
				lastUrl = workingUrl;
			}
		}
		return programList;
	}

	private String downloadUrl(String webUrl)
	{
		String result = "";

		try
		{
			URL url = new URL(webUrl);

			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8")); //ISO-8859-1
			String str;
			while ((str = in.readLine()) != null)
			{
				result += str + "\n";
			}
			in.close();
		}
		catch (MalformedURLException e)
		{}
		catch (IOException e)
		{}

		return result;
	}

	private String getNextUrl(String content)
	{
		//Pattern pattern = Pattern.compile("<a href=\"([^\"]*?)\">Weiter</a></b><br />");
    Matcher matcher = PATTERN_NEXT_URL.matcher(content);

		String resultUrl = "";

		if (matcher.find())
		{
			resultUrl = matcher.group(1).trim();
		}

		return resultUrl;
	}

	private String extentUrl(String url, String originalUrl)
	{
		String resultUrl = url;

		if (resultUrl.startsWith("."))
		{
			resultUrl = resultUrl.substring(1);
		}

		if (resultUrl.length() > 0)
		{
			int index = resultUrl.indexOf('.');
			index = originalUrl.indexOf(resultUrl.substring(0, index));
			resultUrl = originalUrl.substring(0, index) + resultUrl;
		}
		return resultUrl.replaceAll("&amp;", "&");
	}

	private void parseContent(String content, List<TVPProgram> programList, String originalUrl)
	{
		//Pattern pattern = Pattern.compile("<td.*?class=\"ro[\\w\\W]*?<b>(.*?)</b>.*?</td>[\\w\\W]*?<a href=\"([^\"]*?)\"><img src=\"templates/subSilver/images/icon_minipost.gif\".*?<span class=\"postdetails\">[^:]*:(.*?)<[\\w\\W]*?<span class=\"postbody\">([\\w\\W]*?)</td>[\\n\\t\\r ]+</tr>[\\n\\t\\r ]+</table>");
    Matcher matcher = PATTERN_CONTENT.matcher(content);

		while (matcher.find())
		{
			String author = matcher.group(2).trim();
			String contentUrl = extentUrl(matcher.group(1).trim(), originalUrl);
			SimpleDateFormat createDateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.GERMAN);
			Date createDate = null;
			try
			{
				createDate = createDateFormat.parse(matcher.group(3).trim());
			}
			catch (Exception e)
			{
				TVPearlPlugin.getLogger().warning("Wrong dateformat (" + matcher.group(3).trim() + ")");
			}
			String itemContent = matcher.group(4);
			//itemContent = itemContent.replaceAll("_+__<br[\\w\\W]+$", "");
			itemContent = itemContent.replace("\n", "").replace("<br />", "\n").replaceAll("<.*?>", "");

			if (createDate != null)
			{
				containsInfo(itemContent, author, contentUrl, createDate, programList);
			}
		}
	}

	private void containsInfo(String value, String author, String contentUrl,
      Date createDate, List<TVPProgram> programList)
	{
		Calendar today = Calendar.getInstance();
		String programName = "";
		String channel = "";
		Calendar start = Calendar.getInstance();
		String programInfo = "";

		String newChannel = "";
		Calendar newStart = null;
		boolean foundProgram = false;

		for (String line : value.split("\n"))
		{
			boolean isHeader = false;

			String[] items = line.split("[,|·]");
			if (items.length == 4)
			{

				if (isWeekday(items[0]))
				{
					newChannel = items[3].trim();
					newStart = parseStart(items[1].trim(), items[2].trim(), createDate);
					if (newChannel.length() > 0 && newStart != null)
					{
						if (programName.length() > 0)
						{
							programList.add(createProgram(author, contentUrl, createDate, programName, channel, start, programInfo));
						}
						foundProgram = true;
						channel = newChannel;
						start = newStart;
						programInfo = "";
						isHeader = true;
					}
				}
			}
			if (!isHeader)
			{
				if (foundProgram)
				{
					if (line.trim().length() > 0)
					{
						programName = line.trim();
						foundProgram = false;
					}
				}
				else
				{
					programInfo += line + "\n";
				}
			}
		}
		if (programName.length() > 0)
		{
			if ((mOnlyProgrammInFuture && today.compareTo(start) < 0) || !mOnlyProgrammInFuture)
			{
				programList.add(createProgram(author, contentUrl, createDate, programName, channel, start, programInfo));
			}
		}
	}

	private TVPProgram createProgram(String author, String contentUrl, Date createDate, String programName, String channel, Calendar start, String programInfo)
	{
		TVPProgram program = null;

		if (programName.length() > 0)
		{
			program = new TVPProgram();
			program.setAuthor(author);
			program.setContentUrl(contentUrl);
			Calendar cal = Calendar.getInstance();
			cal.setTime(createDate);
			program.setCreateDate(cal);
			program.setTitle(mConverter.convertToString(programName));
			program.setChannel(channel);
			program.setStart(start);
			program.setInfo(programInfo.trim().replaceAll("\\n.*:$", "").trim());
			program.setProgramID("");
		}
		return program;
	}

	private boolean isWeekday(String value)
	{
	  value = value.trim();
	  for (String weekDay : WEEKDAY_NAME) {
      if (value.equalsIgnoreCase(weekDay)
          || value.equalsIgnoreCase(weekDay.substring(0, 2))) {
        return true;
      }
    }
    return false;
	}

	private Calendar parseStart(String date, String time, Date createDate)
	{
		Calendar result = null;
		Calendar create = Calendar.getInstance();
		create.setTime(createDate);

		try
		{
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("d. MMM", Locale.GERMAN);
			Date d = dateFormat1.parse(date);
			result = Calendar.getInstance();
			result.setTime(d);
			result.set(Calendar.YEAR, create.get(Calendar.YEAR));
		}
		catch (Exception e)
		{
			result = null;
		}

		if (result == null)
		{
			try
			{
				SimpleDateFormat dateFormat1 = new SimpleDateFormat("d.MMM", Locale.GERMAN);
				Date d = dateFormat1.parse(date);
				result = Calendar.getInstance();
				result.setTime(d);
				result.set(Calendar.YEAR, create.get(Calendar.YEAR));
			}
			catch (Exception e)
			{
				result = null;
			}
		}

		if (result == null)
		{
			int index = date.indexOf('.');
			int month = getMonth(date.substring(index + 1).trim());

			if (month >= 0 && month < 12)
			{
				result = Calendar.getInstance();
				result.set(create.get(Calendar.YEAR), month, Integer.parseInt(date.substring(0, index)), 0, 0, 0);
			}
			else
			{
				result = null;
			}
		}

		if (result != null)
		{
			Calendar limit = Calendar.getInstance();
			limit.setTime(createDate);
			limit.set(Calendar.DAY_OF_MONTH, limit.get(Calendar.DAY_OF_MONTH) - 1);

			if (result.compareTo(limit) < 0)
			{
				result.set(Calendar.YEAR, result.get(Calendar.YEAR) + 1);
			}

			String[] splitTime = time.split("[: (-/]");
			if (splitTime.length > 1)
			{
				int delta = 0;
				try
				{
					Integer.parseInt(splitTime[0]);
				}
				catch (Exception e)
				{
					delta++;
				}

				try
				{
					result.set(Calendar.HOUR, Integer.parseInt(splitTime[0 + delta]));
					result.set(Calendar.MINUTE, Integer.parseInt(splitTime[1 + delta]));
				}
				catch (Exception e)
				{
					result = null;
				}
			}
			else
			{
				result = null;
			}
		}

		if (result == null)
		{
			TVPearlPlugin.getLogger().warning("Problem with start datetime [" + date + "][" + time + "]");
		}
		return result;
	}

	private int getMonth(String value)
	{
    value = value.trim();
    for (int i = 0; i < MONTH_ABBREVIATION.length; i++) {
      if (value.equalsIgnoreCase(MONTH_ABBREVIATION[i])) {
        return i;
      }
    }
    return -1;
	}
}
