using System;
using System.Collections.Generic;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace PocketTVBrowserCF2
{
    public partial class Reminder : Form
    {
        private TVBrowserControll con;
        private ArrayList memos;
        private ArrayList broadcasts;
        private DateTime lastUpdated;
        private CustomTVBrowserList listviewBroadcasts;

        public Reminder(TVBrowserControll con)
        {
            this.con = con;
            this.lastUpdated = new DateTime();
            InitializeComponent();
            this.pastInitializeComponent();
            this.initVideoMode();
            this.refreshLanguage();
            this.ControlBox = true;
            this.memos = new ArrayList();
            this.broadcasts = new ArrayList();
            this.DialogResult = DialogResult.OK;
        }

        private void pastInitializeComponent()
        {
            this.listviewBroadcasts = new CustomTVBrowserList(new SizeF(this.Width, this.Height));
            this.listviewBroadcasts.Click += new System.EventHandler(this.listViewBroadcasts_Click);
            this.Controls.Add(this.listviewBroadcasts);
        }

        private void initVideoMode()
        {
            Rectangle screen = Screen.PrimaryScreen.Bounds;
            this.Location = new Point((screen.Width - this.Width) / 2, (screen.Height - this.Height) / 2);
            this.lRemember.Bounds = new Rectangle(1, 2, this.Width-2, this.lRemember.Height);
            this.listviewBroadcasts.Bounds = new Rectangle(2, this.lRemember.Bottom +2, this.Width-4, this.Height-this.lRemember.Height-this.buttonOK.Height-8);
            this.buttonOK.Bounds = new Rectangle(this.listviewBroadcasts.Bounds.Right - this.buttonOK.Width, this.listviewBroadcasts.Bottom+2, this.buttonOK.Width, this.buttonOK.Height);
        }

        protected override void OnResize(EventArgs e)
        {
            Rectangle screen = Screen.PrimaryScreen.Bounds;
            this.Location = new Point((screen.Width - this.Width) / 2, (screen.Height - this.Height) / 2);
            base.OnResize(e);
        }

        private void getRemindersFromDB()
        {
            this.memos.Clear();
            this.lastUpdated = DateTime.Now;
            ArrayList broadcasts = this.con.getBroadcastsBySearch("SELECT b.id as id, c.name as channel, b.channel_id as channel_id, b.title as title, strftime('%Y-%m-%d-%H-%M', b.start) as start, strftime('%Y-%m-%d-%H-%M', b.end) as end, b.favorite as favorite, b.reminder as reminder FROM channel c, broadcast b where c.id = b.channel_id AND (b.start BETWEEN datetime('now', 'localtime', '-3 hours') AND datetime('now', 'localtime', '+24 hours')) AND b.reminder='1'");
            for (int i=0;i<broadcasts.Count; i++)
            {
                Broadcast temp = (Broadcast) broadcasts[i];
                if (temp.getStart()>=DateTime.Now && temp.getStart() < DateTime.Now.AddDays(1))
                {
                    this.memos.Add(new Memo(temp,temp.getStart()));
                }
                else if (temp.getStart() <= DateTime.Now && temp.getEnd() >= DateTime.Now)
                {
                    this.memos.Add(new Memo(temp, temp.getStart()));
                }
            }
        }

        public void createMessage()
        {
            this.sortMemos();
            this.listviewBroadcasts.Items.Clear();
            this.broadcasts.Clear();
            for (int i = 0; i < memos.Count; i++)
            {
                Memo temp = (Memo)memos[i];
                if (temp.getTimer().AddMinutes(-(this.con.getSoundReminderTime())) <= DateTime.Now)
                {
                    if (temp.getBroadcastEnd() > DateTime.Now)
                    {
                        temp.setAlreadyDone();
                        this.broadcasts.Add(temp.getBroadcast());
                        try
                        {
                            this.listviewBroadcasts.Items.Add(this.con.createColoredListItem(temp.getBroadcast(), this.getReducedString(temp.getBroadcast().getChannel() + "|" + temp.getBroadcast().ToString(), this.listviewBroadcasts.Width)));
                        }
                        catch
                        {
                        }
                    }
                }
            }
            this.Refresh();
        }

        private void sortMemos()
        {
            this.memos.Sort(new MemoComparer());
        }

        public DateTime getNextAlarm()
        {
            this.sortMemos();
            for (int i = 0; i < memos.Count; i++)
            {
                Memo temp = (Memo)memos[i];
                if (temp.getTimer().AddMinutes(-(this.con.getSoundReminderTime())) <= DateTime.Now)
                {
                    if (!temp.isAlreadyDone())
                    {
                        temp.setAlreadyDone();
                        return temp.getTimer().AddMinutes(-(this.con.getSoundReminderTime()));
                    }
                }
            }
            return DateTime.Now.AddDays(30);//Value out of range
        }

        public DateTime getLastUpdateDateTime()
        {
            return this.lastUpdated;
        }

        public void updateReminders()
        {
            this.getRemindersFromDB();
        }
        
        private void buttonOK_Click(object sender, EventArgs e)
        {
            for (int i = 0; i < this.memos.Count; i++)
            {
                Memo temp = (Memo)this.memos[i];
                for (int j=0; j<this.listviewBroadcasts.Items.Count; j++)
                {
                    Broadcast b = (Broadcast)this.broadcasts[j];
                    if (temp.getBroadcast().Equals(b))
                        temp.setAlreadyDone();
                }
            }
            this.con.getMainform().Show();
            this.DialogResult = DialogResult.OK;
        }

        private void refreshLanguage()
        {
            this.lRemember.Text = this.con.getLanguageElement("Reminder.Label", "Remember!");
            this.Text = this.con.getLanguageElement("Reminder.Text", "Reminder");
        }

        protected override void OnGotFocus(EventArgs e)
        {
            ArrayList backupItems = this.listviewBroadcasts.Items;
            Rectangle backupBounds = this.listviewBroadcasts.Bounds;
            this.listviewBroadcasts.Dispose();
            this.listviewBroadcasts = new CustomTVBrowserList(new SizeF(this.Width, this.Height));
            this.listviewBroadcasts.Bounds = backupBounds;
            this.listviewBroadcasts.Items.AddRange(backupItems);
            this.listviewBroadcasts.Click += new System.EventHandler(this.listViewBroadcasts_Click);
            this.Controls.Add(this.listviewBroadcasts);
        }

        private void listViewBroadcasts_Click(object sender, EventArgs e)
        {
            try
            {
                if (this.con.hasDB())
                {
                    Broadcast temp = (Broadcast)this.broadcasts[this.listviewBroadcasts.SelectedIndex];
                    Cursor.Current = Cursors.WaitCursor;
                    Details d = new Details(temp, this.con);
                    Cursor.Current = Cursors.Default;
                    d.Show();
                    d.BringToFront();
                }
                else
                    MessageBox.Show(this.con.getLanguageElement("Mainform.Directory", "Please specify the path to your tvdata.tvd (Menu - Configuration)"), this.con.getLanguageElement("Mainform.Directory.Warning", "WARNING - No TV-Data"));
            }
            catch
            {
                //Nothing - user just klicked in a free area of the screen
            }
        }

        void buttonOK_KeyDown(object sender, System.Windows.Forms.KeyEventArgs e)
        {
            base.OnKeyDown(e);
        }

        private String getReducedString(String original, int width)
        {
            String result = "";
            Graphics g = this.CreateGraphics();
            SizeF size = g.MeasureString(original, this.Font);
            int subrakter = 15;
            if (width > 400)
                subrakter = 30;
            if (size.Width > width - subrakter)
            {
                result = original;
                while (g.MeasureString(result, this.Font).Width >= width - subrakter)
                {
                    result = result.Substring(0, result.Length - 1);
                }
                result = result.Substring(0, result.Length - 3);
                result += "...";
            }
            else
            {
                result = original;
            }
            return result;
        }

        protected override void OnPaint(PaintEventArgs e)
        {

            Rectangle rc = this.ClientRectangle;
            rc.Height--;
            rc.Width--;
            Graphics g = this.CreateGraphics();
            g.DrawRectangle(new Pen(Color.Black), rc);

            base.OnPaint(e);
        }

        protected override void OnKeyDown(KeyEventArgs e)
        {
            if (e.KeyValue == 40)
            {
                if (this.listviewBroadcasts.SelectedIndex == -1)
                {
                    this.listviewBroadcasts.SelectedIndex = 0;
                }
                else
                {
                    if (this.listviewBroadcasts.SelectedIndex + 1 == this.listviewBroadcasts.Items.Count)
                        this.listviewBroadcasts.SelectedIndex = 0;
                    else
                        this.listviewBroadcasts.SelectedIndex = this.listviewBroadcasts.SelectedIndex + 1;
                }

                e.Handled = true;
            }
            else if (e.KeyValue == 38)
            {
                if (this.listviewBroadcasts.SelectedIndex == -1)
                {
                    this.listviewBroadcasts.SelectedIndex = 0;
                }
                else
                {
                    if (this.listviewBroadcasts.SelectedIndex - 1 == -1)
                        this.listviewBroadcasts.SelectedIndex = this.listviewBroadcasts.Items.Count-1;
                    else
                        this.listviewBroadcasts.SelectedIndex = this.listviewBroadcasts.SelectedIndex - 1;
                }
                e.Handled = true;
            }
            else if (e.KeyValue == 36)
            {
                this.buttonOK.Focus();
                this.listviewBroadcasts.SelectedIndex = -1;
                e.Handled = true;
            }
            else if (e.KeyValue == 39)
            {
                this.buttonOK.Focus();
                this.listviewBroadcasts.SelectedIndex = -1;
                e.Handled = true;
            }
            else if (e.KeyValue == 37)
            {
                this.buttonOK.Focus();
                this.listviewBroadcasts.SelectedIndex = -1;
                e.Handled = true;
            }
            else if (e.KeyValue == 13)
            {
                if (this.listviewBroadcasts.SelectedIndex != -1 && this.con.hasDB())
                {
                    Broadcast temp = (Broadcast)this.broadcasts[this.listviewBroadcasts.SelectedIndex];
                    Cursor.Current = Cursors.WaitCursor;
                    Details d = new Details(temp, this.con);
                    Cursor.Current = Cursors.Default;
                    d.Show();
                    d.BringToFront();
                    e.Handled = true;
                }
                else
                {
                    this.buttonOK.Focus();
                    this.DialogResult = DialogResult.OK;
                    base.OnKeyDown(e);
                }
                e.Handled = true;
            }
            //base.OnKeyDown(e);
        }
    }
}