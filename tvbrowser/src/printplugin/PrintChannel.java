package printplugin;

import java.awt.*;
import java.util.Iterator;
import devplugin.*;
import java.util.ArrayList;

public class PrintChannel
{
	private Channel mChannel;
	private Graphics mg;
	private String mFontName;
	private int mFontSize;
	private int mProgramFontSize;
	private Font mChannelFont;
	private ArrayList mPrintPrograms;

	public PrintChannel(Channel channel, String fontName, int fontSize,
							  int programFontSize, Graphics g)
	{
		mChannel = channel;
		mg = g;
		mFontName = fontName;
		mFontSize = fontSize;
		mProgramFontSize = programFontSize;
		mPrintPrograms = new ArrayList();

		mChannelFont = new Font(mFontName, Font.BOLD, mFontSize);
	}

	public boolean draw(int x , int y, int width, int height, Date printDate, boolean lineRight, boolean lineBottom)
	{
		int programY, programWidth, programHeight;
		int lineY1;
		int channelX, channelWidth;
		FontMetrics metrics = mg.getFontMetrics(mChannelFont);
		Date pDate = new Date(printDate);

		mPrintPrograms.clear();

		channelWidth = metrics.stringWidth(mChannel.getName());
		channelX = (int) x + (width / 2) - (channelWidth /2 );

		mg.setFont(mChannelFont);
		mg.drawString(mChannel.getName(), channelX, y + mChannelFont.getSize());

		programY = (int)(y + mChannelFont.getSize() + mChannelFont.getSize() * 0.4 + mProgramFontSize);
		lineY1 = (int)(y + mChannelFont.getSize() + mChannelFont.getSize() * 0.4);;
		programHeight = (int) (height - 15 - (mChannelFont.getSize() + mChannelFont.getSize() * 0.4));
		programWidth = width - 10;

		Iterator programIter = Plugin.getPluginManager().getChannelDayProgram(pDate, mChannel);

		if (programIter != null)
		{
			while (programIter.hasNext())
			{
				Program prog = (Program) programIter.next();
				if (prog.getHours() > 5)
				{
					PrintProgram pProg = new PrintProgram(prog, mFontName, mProgramFontSize, programWidth, mg);
					mPrintPrograms.add(pProg);
				}
			}
		}
		else
		{
			return false;
		}

		pDate = pDate.addDays(1);

		programIter = Plugin.getPluginManager().getChannelDayProgram(pDate, mChannel);

		if (programIter != null)
		{
			while (programIter.hasNext())
			{
				Program prog = (Program) programIter.next();
				if (prog.getHours() < 6)
				{
					PrintProgram pProg = new PrintProgram(prog, mFontName, mProgramFontSize, programWidth, mg);
					mPrintPrograms.add(pProg);
				}
			}
		}

		sizeAdjustment(programHeight);

		for (int i = 0;i < mPrintPrograms.size(); i++)
		{
			PrintProgram printProgram = (PrintProgram) mPrintPrograms.get(i);
			programY = printProgram.draw(x, programY);
		}

		if (lineRight)
		{
			mg.drawLine(x + width - 5, lineY1, x + width - 5, lineY1 + height);
		}

		if (lineBottom)
		{
			mg.drawLine(x, lineY1 + height - 10, x + width - 5, lineY1 + height -10);
		}

		return true;
	}

	private void sizeAdjustment(int programHeight)
	{
		if (getProgramListHeight() < programHeight)
			return;

		for (int i = 0;i < mPrintPrograms.size(); i++)
		{
			PrintProgram printProgram = (PrintProgram) mPrintPrograms.get(i);

			if (printProgram.getProgram().getHours() <= 11)
			{
				printProgram.setCompact(true);
				if (getProgramListHeight() < programHeight)
					return;
			}
		}

		for (int i = 0;i < mPrintPrograms.size(); i++)
		{
			PrintProgram printProgram = (PrintProgram) mPrintPrograms.get(i);
			if (printProgram.getProgram().getHours() > 2 &&
				 printProgram.getProgram().getHours() < 6)
			{
				printProgram.setCompact(true);
				if (getProgramListHeight() < programHeight)
					return;
			}
		}

		for (int i = 0;i < mPrintPrograms.size(); i++)
		{
			PrintProgram printProgram = (PrintProgram) mPrintPrograms.get(i);
			printProgram.setCompact(true);
			if (getProgramListHeight() < programHeight)
				return;

		}

		for (int i = mPrintPrograms.size() - 1;i >= 0; i--)
		{
			PrintProgram printProgram = (PrintProgram) mPrintPrograms.get(i);
			if (printProgram.getProgram().getHours() > 1 &&
				 printProgram.getProgram().getHours() < 6)
			{
				mPrintPrograms.remove(i);
				if (getProgramListHeight() < programHeight)
					return;
			}
		}

		for (int i = 0;i < mPrintPrograms.size(); i++)
		{
			PrintProgram printProgram = (PrintProgram) mPrintPrograms.get(i);
			if (printProgram.getProgram().getHours() < 11 )
			{
				mPrintPrograms.remove(i);
				if (getProgramListHeight() < programHeight)
					return;
			}
		}

	}

	private int getProgramListHeight()
	{
		int retHeight = 0;
		for (int i = 0;i < mPrintPrograms.size(); i++)
		{
			PrintProgram printProgram = (PrintProgram) mPrintPrograms.get(i);
			retHeight += printProgram.getHeight();
		}

		return retHeight;
	}
}
