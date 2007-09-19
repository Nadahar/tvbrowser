using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Collections;
using System.Security.Cryptography;
using System.IO;
using System.Text.RegularExpressions;
using Microsoft.WindowsMobile.PocketOutlook;
using Microsoft.WindowsCE.Forms;

namespace PocketTVBrowserCF2
{
    public partial class Details : Form
    {
        private int rowhight;
        private int width;
        private TVBrowserControll con;
        private ArrayList elements;
        private Broadcast broadcast;
        private int yPosition = 0;
        private int formHeight = 0;
        private int scrollStep;
        
        public Details(Broadcast broadcast, TVBrowserControll con)
        {
            this.broadcast = broadcast;
            this.con = con;
            InitializeComponent();
            this.Text = "PocketTVBrowser: " + this.broadcast.getTitle();
            this.refreshLanguage();
            this.MinimizeBox = false;
            this.getInformation();
            int osVersion = Int32.Parse(Environment.OSVersion.Version.ToString().Split('.')[0].ToString());
            if (osVersion < 5)
            {
                this.menuItemOutlook.Enabled = false;
            }
            Graphics g = this.CreateGraphics();
            SizeF size = g.MeasureString("G", this.Font);
            this.width = (int)(this.Width - size.Height);
            this.rowhight = 14;
            if (this.con.getCurrentScreen().Width >= 192)
                this.rowhight = 28;
            this.scrollStep = this.Height-5;
            this.write();
            int osVersionSub = 0;
            try
            {
                osVersionSub = Int32.Parse(Environment.OSVersion.Version.ToString().Split('.')[1].ToString());
            }
            catch
            {
            }
            int h = this.Height;
            int w = this.Width;
            if (w < h)
            {
                int t = w;
                w = h;
                h = t;
            }
            if ((w == 240 && h == 188) || (w == 480 && h == 376))
            {
                this.menuItemRotate.Enabled = false;
            }
            else if (osVersion < 5)
            {
                if (osVersion == 4 && osVersionSub >= 21)
                {
                }
                else
                {
                    this.menuItemRotate.Enabled = false;
                }
            }
        }

        private void write()
        {
            int yPosition = 0; 
            //Date and channel
            Label lDateChannelContent = new Label();
            lDateChannelContent.ForeColor = System.Drawing.Color.Red;
            lDateChannelContent.Location = new System.Drawing.Point(0, yPosition);
            lDateChannelContent.TextAlign = ContentAlignment.TopCenter;
            lDateChannelContent.Text = this.broadcast.getStart().ToShortDateString();
            lDateChannelContent.Text += " | ";
            lDateChannelContent.Text += this.broadcast.getStart().ToShortTimeString();
            lDateChannelContent.Text += " | ";
            lDateChannelContent.Text += this.broadcast.getChannel();
            lDateChannelContent.Size = new System.Drawing.Size(this.width, getNeededHeight(lDateChannelContent.Text));//28
            this.Controls.Add(lDateChannelContent);
            yPosition += lDateChannelContent.Height + 5;

            //Titel
            Label lTitelContent = new Label();
            lTitelContent.Text = this.broadcast.getTitle().Replace("&", "&&");
            lTitelContent.Font = new System.Drawing.Font("Tahoma", 11F, System.Drawing.FontStyle.Bold);
            lTitelContent.Location = new System.Drawing.Point(0, yPosition);
            lTitelContent.TextAlign = ContentAlignment.TopCenter;
            lTitelContent.Size = new System.Drawing.Size(this.width, getNeededHeight(lTitelContent.Text, lTitelContent.Font));//20
            this.Controls.Add(lTitelContent);
            yPosition += lTitelContent.Height + 5;

            //Additional Information
            for (int i = 0; i < this.elements.Count; i++)
            {
                String[] element = (String[])this.elements[i];
                Label lName = new Label();
                Label lContent = new Label();
                lName.Text = this.con.getLanguageElement("Database.Broadcast." + element[0].ToString(), element[0].ToString()).Replace("&", "&&");
                lName.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
                lName.ForeColor = System.Drawing.Color.Silver;
                lName.Location = new System.Drawing.Point(0, yPosition);
                lName.Size = new System.Drawing.Size(this.width, rowhight + 3);
                yPosition += lName.Height - 2;
                if (element[0].ToString().Equals("form"))
                {
                    String decoded = this.decode(element[1].ToString()).Replace("&", "&&");
                    lContent.Text = this.translateForminformation(decoded);
                }
                else
                {
                    lContent.Text = this.decode(element[1].ToString()).Replace("&", "&&");
                }
                lContent.Location = new System.Drawing.Point(0, yPosition);
                lContent.Size = new System.Drawing.Size(this.width, getNeededHeight(lContent.Text));//28
                yPosition += lContent.Height + 5;
                this.Controls.Add(lName);
                this.Controls.Add(lContent);
            }

            //Duration
            Label lDuration = new Label();
            lDuration.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
            lDuration.ForeColor = System.Drawing.Color.Silver;
            lDuration.Location = new System.Drawing.Point(0, yPosition);
            lDuration.Size = new System.Drawing.Size(this.width, rowhight + 2);
            lDuration.Text = this.con.getLanguageElement("Details.Duration","duration");
            yPosition += lDuration.Height - 3;
            this.Controls.Add(lDuration);
            int duration = this.broadcast.getLength();
            Label lDurationContent = new Label();
            lDurationContent.Text = duration.ToString() + " " + this.con.getLanguageElement("Details.Ends", "min. (ends") + " " + this.broadcast.getEnd().ToShortTimeString() + " - " + this.broadcast.getEnd().ToShortDateString() + ")";
            lDurationContent.Location = new System.Drawing.Point(0, yPosition);
            lDurationContent.Size = new System.Drawing.Size(this.width, rowhight);
            yPosition += lDurationContent.Height + 5;
            this.Controls.Add(lDurationContent);         
            
            // Bookmark
            int xPosition = 0;
            if (this.broadcast.isFavourite())
            {
                this.pictureBoxBookmark.Visible = true;
                this.pictureBoxBookmark.Location = new System.Drawing.Point(0, yPosition);
                xPosition = 50;
            }

            // Reminder
            if (this.broadcast.isReminder())
            {
                this.pictureBoxReminder.Visible = true;
                this.pictureBoxReminder.Location = new System.Drawing.Point(xPosition, yPosition);
                xPosition = 50;
                this.menuItemReminder.Text = this.con.getLanguageElement("Details.DontRemindMe","don't remind me");
            }
            if (this.broadcast.isReminder() || this.broadcast.isFavourite())
            {
                yPosition += lDurationContent.Height + 10;
            }
            
            // lCopyright
            yPosition += 5;
            Label lCopyright = new Label();
            lCopyright.Location = new System.Drawing.Point(0, yPosition+5);
            lCopyright.Size = new System.Drawing.Size(this.width, rowhight);
            lCopyright.Text = "(c) " + this.broadcast.getChannel() + "/tvbrowser.org";
            lCopyright.TextAlign = ContentAlignment.TopCenter;
            this.Controls.Add(lCopyright);
            this.formHeight = lCopyright.Location.Y;
        }

        private void getInformation()
        {
            try
            {
                this.elements = this.con.getBroadcastInformation(this.broadcast.getID());
            }
            catch
            {
            }
        }

        private void menuItemSearch_Click(object sender, EventArgs e)
        {
            Search search = new Search(this.con, this.broadcast.getTitle(), true, true);
            search.Show();
            search.BringToFront();
        }

        private void menuItemOutlook_Click(object sender, EventArgs e)
        {
            try
            {
                OutlookSession os = new OutlookSession();
                Appointment a = new Appointment();
                a.Subject = this.broadcast.getTitle();
                a.Sensitivity = Sensitivity.Private;
                a.Location = this.broadcast.getChannel();
                a.Categories = "PocketTVBrowser";
                a.Body = "(c) TVBrowser.org / " + this.broadcast.getChannel();
                a.Start = this.broadcast.getStart();
                a.Duration = this.broadcast.getEnd() - this.broadcast.getStart();
                os.Appointments.Items.Add(a);
                a.ShowDialog();
            }
            catch
            {
                MessageBox.Show(this.con.getLanguageElement("Details.FeatureNeedsWM5","This feature needs WM5/WM6"), this.con.getLanguageElement("Details.Warning","Warning"));
            }
        }

        private void menuItemReminder_Click(object sender, EventArgs e)
        {
            Cursor.Current = Cursors.WaitCursor;
            if (!this.broadcast.isReminder())
            {
                if (this.broadcast.getStart() > DateTime.Now)
                {
                    this.broadcast.setReminder(true);
                    this.con.updateBroadcastReminder(this.broadcast.getID(), true);
                    this.con.backupReminder(this.broadcast);
                    this.con.setRefreshReminders(true);
                    Details temp = new Details(this.broadcast, this.con);
                    temp.Show();
                    temp.BringToFront();
                    this.Close();
                }
                else
                {
                    MessageBox.Show(this.con.getLanguageElement("Details.BroadcastInThePast","Sorry - this broadcast is in the past"));
                }
            }
            else
            {
                this.con.updateBroadcastReminder(this.broadcast.getID(), false);
                this.broadcast.setReminder(false);
                this.con.backupReminder(this.broadcast);
                this.con.setRefreshReminders(true);
                Details temp = new Details(this.broadcast, this.con);
                temp.Show();
                temp.BringToFront();
                this.Close();
            }
            Cursor.Current = Cursors.Default;
        }

        private String decode(String text)
        {
            /**
             * Do not modify this part of the software!
             * Remember this Copyrights of tvbrowser.org
             * for further information visit our wiki: http://hilfe.tvbrowser.org
             **/
            String result = "";
            text = text.Replace("@_@", "'");
            for (int i = 0; i < text.Length; i++)
            {
                int position = (int)text.ToCharArray()[i];
                position = position - 7;
                result += (char)position;
            }
            return result;
        }

        private void menuItemOK_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        protected override void OnKeyDown(KeyEventArgs e)
        {
            if (e.KeyValue == 13)
            {
                try
                {
                    this.Close();
                }
                catch
                {
                }
            }
            else if (e.KeyValue == 40)
            {
                yPosition += this.scrollStep;
                if (yPosition >= this.formHeight - this.Height)
                {
                    yPosition = this.formHeight - this.Height;
                }
                e.Handled = true;
            }
            else if (e.KeyValue == 38)
            {
                yPosition -= this.scrollStep;
                if (yPosition < 0)
                {
                    yPosition = 0;
                }
                e.Handled = true;
            }
            else
            {
                base.OnKeyDown(e);
            }
            try
            {
                this.AutoScrollPosition = new Point(0, yPosition);
            }
            catch
            {
            }
        }

        private int getNeededHeight(String text)
        {
            text = prepareString(text);
            Graphics g = this.CreateGraphics();
            int newlines = text.Split('\n').Length;
            SizeF size = g.MeasureString(text, this.Font);
            return (int)size.Height;
        }

        private int getNeededHeight(String text, Font font)
        {
            text = prepareString(text);
            Graphics g = this.CreateGraphics();
            SizeF size = g.MeasureString(text, font);
            if (size.Width>= this.Width-20)
                size.Height= (int)size.Height+(int)g.MeasureString("dummy",font).Height;
            return (int)size.Height;
        }

        private String prepareString(String aString)
        {
            String[] pards = aString.Split(' ');
            Graphics g = this.CreateGraphics();
            String line = "";
            String temp = "";
            StringBuilder result = new StringBuilder("");
            int y = 0;
            Point p = this.AutoScrollPosition;
            for (int i = 0; i < pards.Length; i++)
            {
                temp = line + ' ' + pards[i];
                SizeF sizeTemp = g.MeasureString(temp, this.Font);
                if (sizeTemp.Width < this.width)
                {
                    line += ' ' + pards[i];
                }
                else
                {
                    result.Append(line + "\r\n");
                    y += 12;
                    line = pards[i];
                    temp = pards[i];
                }
            }
            result.Append(line + "\r\n");
            result = result.Remove(0, 1);
            return result.ToString();
        }

        private String translateForminformation(String value)
        {
            String result = "";
            String[] fields = value.Split('|');
            for (int i = 0; i < fields.Length; i++)
            {
                result += "- " + this.con.getLanguageElement("Forminformation." + fields[i], fields[i]);
                result = result.Trim();
                if (i+1 < fields.Length)
                    result += "\r\n";
            }
            return result;
        }

        private void rotate()
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                if (SystemSettings.ScreenOrientation == ScreenOrientation.Angle0)
                {
                    // change to landscape
                    SystemSettings.ScreenOrientation = ScreenOrientation.Angle90;
                }
                else
                {
                    // change to portrait
                    SystemSettings.ScreenOrientation = ScreenOrientation.Angle0;
                }
               
            }
            catch
            {
                MessageBox.Show("Mainform.RotateError", "was't able so rotate screen");
                this.menuItemRotate.Enabled = false;
            }
        }

        private void refreshLanguage()
        {
            this.menuItemMenu.Text = this.con.getLanguageElement("Details.Designer.Menu","Menu");
            this.menuItemSearch.Text = this.con.getLanguageElement("Details.Designer.SearchSimilar","Search similar");
            this.menuItemOutlook.Text = this.con.getLanguageElement("Details.Designer.AddToOutlook","Add to Calendar (WM5)");
            this.menuItemReminder.Text = this.con.getLanguageElement("Details.Designer.AddToReminders","Add to reminders");
            this.menuItemRotate.Text = this.con.getLanguageElement("Details.Rotate", "Rotate");
        }

        private void menuItemRotate_Click(object sender, EventArgs e)
        {
            this.rotate();
            Details temp = new Details(this.broadcast, this.con);
            temp.Show();
            temp.BringToFront();
            this.Close();
            Cursor.Current = Cursors.Default;
        }
    }
}