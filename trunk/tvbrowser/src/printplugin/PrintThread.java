
package printplugin;

import util.ui.UiUtilities;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.awt.print.*;
import javax.print.*;
import javax.print.DocFlavor.*;
import java.io.*;
import java.awt.print.*;

import javax.swing.*;

import devplugin.*;

public class PrintThread implements Runnable
{
	private PrintDialog mPrintDialog;

	public PrintThread(PrintDialog printDialog)
	{
		mPrintDialog = printDialog;
	}

	public void run()
	{
		mPrintDialog.print();
	}
}