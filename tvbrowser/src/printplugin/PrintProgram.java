
package printplugin;

import java.awt.*;
import devplugin.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class PrintProgram
{
	private Program mProgram;
	private int mMaxTextWidth, mMaxWidth, mTitleheight, mInfoheight;
	private String mStartTime;
	private ArrayList mTitle;
	private ArrayList mInfo;
	private Graphics mg;
	private Font mFontTitle, mFontInfo;
	private boolean mCompact;

	public PrintProgram(Program program, String fontName, int fontSize, int maxWidth, Graphics g)
	{
		mProgram = program;
		mMaxWidth = maxWidth;
		mg = g;
		mInfoheight = 0;
		mCompact = false;

		mFontTitle = new Font(fontName, Font.BOLD, fontSize);
		mFontInfo  = new Font(fontName, Font.PLAIN, fontSize);

		mTitle = new ArrayList();
		mInfo = new ArrayList();

		mStartTime = mProgram.getTimeString();
		mMaxTextWidth = mMaxWidth - getMaxStartTimeWidth(mFontTitle);

		splitStringtoList(mTitle, mProgram.getTitle(), mFontTitle);
		mTitleheight = (int) ((mFontTitle.getSize() + mFontTitle.getSize() * 0.4) * mTitle.size());

		splitStringtoList(mInfo, mProgram.getShortInfo(), mFontInfo);
		mInfoheight = (int) ((mFontInfo.getSize() + mFontInfo.getSize() * 0.4) * mInfo.size());
	}

	public void setCompact(boolean compact)
	{
		mCompact = compact;
	}

	public int draw(int x, int y)
	{
		int i, ypos, textX;
		ypos = y;
		textX = x + getMaxStartTimeWidth(mFontTitle);

		mg.setFont(mFontTitle);

		mg.drawString(mStartTime, x, ypos);

		for (i = 0; i < mTitle.size(); i++)
		{
			mg.drawString((String) mTitle.get(i), textX, ypos);
			ypos = (int)(ypos + mFontTitle.getSize() + mFontTitle.getSize() * 0.4);
		}

		if (!mCompact)
		{
			mg.setFont(mFontInfo);
			for (i = 0; i < mInfo.size(); i++)
			{
				mg.drawString((String) mInfo.get(i), textX, ypos);
				ypos = (int)(ypos + mFontInfo.getSize() + mFontInfo.getSize() * 0.4);
			}
		}
		return ypos;
	}

	public int getHeight()
	{
		if (mCompact)
		{
			return mTitleheight;
		}
		else
		{
			return mTitleheight + mInfoheight;
		}
	}
	public Program getProgram()
	{
		return mProgram;
	}

	private void splitStringtoList(ArrayList arrayList, String str, Font font)
	{
		StringTokenizer strtoken = new StringTokenizer(str);
		String line;
		FontMetrics metrics = mg.getFontMetrics(font);

		line = "";
		arrayList.clear();

		while (strtoken.hasMoreTokens())
		{
			String nextToken = strtoken.nextToken();

			if (metrics.stringWidth(line + " " + nextToken) < mMaxTextWidth)
			{
				if (line.length() == 0)
					line = nextToken;
				else
					line += " " + nextToken;
			}
			else
			{
				if (line.length() > 0)
				{
					arrayList.add(line);
					line = nextToken;
				}
			}
		}
		if (line.length() > 0)
		{
			arrayList.add(line);
		}

	}
	private int getMaxStartTimeWidth(Font font)
	{
		FontMetrics metrics = mg.getFontMetrics(font);
		return metrics.stringWidth("88:88 ");
	}
}
