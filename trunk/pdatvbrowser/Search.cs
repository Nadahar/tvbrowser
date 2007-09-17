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
    public partial class Search : Form
    {
        private TVBrowserControll con;
        private ArrayList broadcasts;
        private ArrayList searchElements;
        private String lastSearchWord;
        bool favoriteChecked;
        bool reminderChecked;

        public Search(TVBrowserControll con)
        {
            this.con = con;
            this.listViewBroadcasts = new CustomTVBrowserList(new System.Drawing.SizeF(this.Width, this.Height));
            InitializeComponent();
            this.initVideoMode();
            this.refreshLanguage();
            this.fillDates();
            this.fillChannels();
            this.fillElements();
            this.comboBoxDates.SelectedIndex = 0;
            this.comboBoxElement.SelectedIndex = 0;
            this.lastSearchWord = "";
        }
        public Search(TVBrowserControll con, String search, bool exact, bool start)
        {
            this.con = con;
            this.listViewBroadcasts = new CustomTVBrowserList(new System.Drawing.SizeF(this.Width, this.Height));
            InitializeComponent();
            this.initVideoMode();
            this.refreshLanguage();
            this.fillDates();
            this.fillChannels();
            this.fillElements();
            this.comboBoxDates.SelectedIndex = 0;
            this.comboBoxElement.SelectedIndex = 0;
            this.tbSearch.Text = search;
            if (exact)
                this.checkBoxExact.Checked = true;
            if (start)
                this.bSearch_Click(null, null);
            this.lastSearchWord = "";
            this.bSearch.Focus();
            
        }

        private void initVideoMode()
        {
            this.lSearch.Bounds = new Rectangle(0, 0, this.Width, this.lSearch.Height);
            this.tbSearch.Bounds = new Rectangle(0, this.lSearch.Height + 2, (this.Width * 2 / 3) - 1, this.tbSearch.Height);
            this.checkBoxExact.Bounds = new Rectangle(this.tbSearch.Width + 2, this.lSearch.Height + 2, (this.Width * 1 / 3) - 1, this.tbSearch.Height);
            this.comboBoxElement.Bounds = new Rectangle(0, this.tbSearch.Bounds.Bottom + 2, (this.Width * 2 / 3) - 1, this.comboBoxElement.Height);
            this.comboBoxDates.Bounds = new Rectangle(this.comboBoxElement.Width + 2, this.tbSearch.Bounds.Bottom + 2, (this.Width * 1 / 3) - 1, this.comboBoxDates.Height);
            this.comboBoxChannel.Bounds = new Rectangle(0, this.comboBoxElement.Bottom + 2, (this.Width * 2 / 3) - 1, this.comboBoxChannel.Height);
            this.bSearch.Bounds = new Rectangle(this.comboBoxChannel.Bounds.Right + 2, this.comboBoxDates.Bottom + 2, (this.Width * 1 / 3) - 1, this.checkBoxExact.Height);
            this.radioButtonFavorites.Bounds = new Rectangle(0, this.comboBoxChannel.Bounds.Bottom + 2, (this.Width * 1 / 2), this.radioButtonFavorites.Height);
            this.radioButtonReminder.Bounds = new Rectangle(this.radioButtonFavorites.Width, this.comboBoxChannel.Bounds.Bottom + 2, (this.Width * 1 / 2), this.radioButtonReminder.Height);
            this.listViewBroadcasts.Bounds = new Rectangle(0, this.radioButtonFavorites.Bounds.Bottom, this.Width, this.Height - this.lSearch.Height - this.tbSearch.Height - this.comboBoxElement.Height - this.radioButtonFavorites.Height - this.comboBoxChannel.Height - 8);//this.Height - this.radioButtonFavorites.Bottom);

        }


        protected override void OnResize(EventArgs e)
        {
            this.initVideoMode();
            base.OnResize(e);
        }

        public void fillChannels()
        {
            this.comboBoxChannel.Items.Clear();
            if (this.con.hasDB())
            {
                this.comboBoxChannel.Items.Add(this.con.getLanguageElement("Search.SelectChannel", "-all channels -"));
                for (int i = 0; i < this.con.getChannels().Count; i++)
                {
                    Channel temp = (Channel)this.con.getChannels()[i];
                    this.comboBoxChannel.Items.Add(temp);
                }
            }
            this.comboBoxChannel.SelectedIndex = 0;
        }

        private void listViewBroadcasts_Click(object sender, EventArgs e)
        {
            try
            {
                Broadcast temp = (Broadcast)this.broadcasts[this.listViewBroadcasts.SelectedIndex];
                Cursor.Current = Cursors.WaitCursor;
                Details d = new Details(temp, this.con);
                Cursor.Current = Cursors.Default;
                d.Show();
                d.BringToFront();
            }
            catch
            {
            }
        }

        private void fillDates()
        {
            if (this.con.hasDB())
            {
                this.comboBoxDates.Items.Add(this.con.getLanguageElement("Search.All", "all"));
                this.comboBoxDates.Items.Add(this.con.getLanguageElement("Search.SinceToday", "since today"));
                ArrayList dates = this.con.getDates(this.con.getCurrentChannel().getId());
                for (int i = 0; i < dates.Count; i++)
                {
                    this.comboBoxDates.Items.Add(dates[i]);
                }
            }
        }

        private void fillElements()
        {
            this.searchElements = new ArrayList();
            if (this.con.hasDB())
            {
                ArrayList elements = this.con.getColumnNames();
                for (int i = 0; i < elements.Count; i++)
                {
                    SearchElement se = new SearchElement(elements[i].ToString(), this.con.getLanguageElement("Database.Broadcast." + elements[i].ToString(), elements[i].ToString()));
                    this.comboBoxElement.Items.Add(se);
                }
            }
        }

        private void bSearch_Click(object sender, EventArgs e)
        {
            bool search = true;
            this.listViewBroadcasts.Dispose();
            this.listViewBroadcasts = new CustomTVBrowserList(new System.Drawing.SizeF(this.Width, this.Height));
            this.listViewBroadcasts.ImageList = null;
            this.listViewBroadcasts.Name = "listViewBroadcasts";
            this.listViewBroadcasts.ShowScrollbar = true;
            this.initVideoMode();
            this.listViewBroadcasts.TabIndex = 0;
            this.listViewBroadcasts.TopIndex = 0;
            this.listViewBroadcasts.WrapText = false;
            this.listViewBroadcasts.Click += new System.EventHandler(this.listViewBroadcasts_Click);
            String stmt = "SELECT b.id as id, c.name as channel, b.title as title, strftime('%Y-%m-%d-%H-%M', b.start) as start, strftime('%Y-%m-%d-%H-%M', b.end) as end, b.favorite as favorite, b.reminder as reminder FROM channel c, broadcast b, info i where c.id = b.channel_id AND i.broadcast_id=b.id";
            if (this.radioButtonFavorites.Checked)
            {
                stmt += " AND b.favorite='1'";
            }
            else if (this.radioButtonReminder.Checked)
            {
                stmt += " AND b.reminder='1'";
            }
            else
            {
                if (this.tbSearch.Text.Length > 0)
                {
                    if (this.checkBoxExact.Checked)
                    {
                        if (this.comboBoxElement.SelectedIndex == 0)
                        {
                            stmt += " AND b.title ='" + this.tbSearch.Text.Replace("'", "''") + "'";
                        }
                        else
                        {
                            SearchElement se = (SearchElement)this.comboBoxElement.SelectedItem;
                            stmt += " AND i." + se.getDatabasename() + " ='" + this.encode(this.tbSearch.Text) + "'";
                        }
                    }
                    else
                    {
                        if (this.comboBoxElement.SelectedIndex == 0)
                        {
                            stmt += " AND b.title LIKE '%" + this.tbSearch.Text.Replace("'", "''") + "%'";
                        }
                        else
                        {
                            SearchElement se = (SearchElement)this.comboBoxElement.SelectedItem;
                            stmt += " AND i." + se.getDatabasename() + " LIKE '%" + this.encode(this.tbSearch.Text) + "%'";
                        }
                    }
                }
                else
                {
                    MessageBox.Show(this.con.getLanguageElement("Search.SearchString", "search item is too short (at least one character)"));
                    search = false;
                }
            }

            String date = "";
            if (this.comboBoxDates.SelectedIndex > 1)
            {
                date = ((TVBrowserDate)this.comboBoxDates.SelectedItem).getDateTime().Year.ToString();
                date += "-";
                if (((TVBrowserDate)this.comboBoxDates.SelectedItem).getDateTime().Month < 10)
                    date += "0";
                date += ((TVBrowserDate)this.comboBoxDates.SelectedItem).getDateTime().Month.ToString();
                date += "-";
                if (((TVBrowserDate)this.comboBoxDates.SelectedItem).getDateTime().Day < 10)
                    date += "0";
                date += ((TVBrowserDate)this.comboBoxDates.SelectedItem).getDateTime().Day.ToString();
                this.broadcasts = new ArrayList();
                stmt += " AND date(b.start)=date('" + date + "')";
            }
            else if (this.comboBoxDates.SelectedIndex == 1)
            {
                date = DateTime.Now.ToString("yyyy-MM-dd");
                this.broadcasts = new ArrayList();
                stmt += " AND date(b.start) >= date('" + date + "')";
            }
            if (this.comboBoxChannel.SelectedIndex > 0)
            {
                stmt += " AND c.name ='" + this.comboBoxChannel.SelectedItem.ToString() + "'";
            }
            if (search)
            {
                Cursor.Current = Cursors.WaitCursor;
                broadcasts = this.con.getBroadcastsBySearch(stmt);
                Cursor.Current = Cursors.Default;
                for (int i = 0; i < broadcasts.Count; i++)
                {
                    Broadcast temp = (Broadcast)broadcasts[i];
                    this.listViewBroadcasts.Items.Add(this.con.createColoredListItem(temp, this.getReducedString(temp.getStart().ToShortDateString() + "|" + temp.getChannel() + "|" + temp.ToString(),this.Width)));
                }
                this.Controls.Add(this.listViewBroadcasts);

                if (this.broadcasts.Count < 1)
                    MessageBox.Show(this.con.getLanguageElement("Search.WarningNothingFound", "Sorry - nothing found"));
            }
        }

        private void tbSearch_TextGotFocus(object sender, EventArgs e)
        {
            this.inputPanel.Enabled = true;
        }

        private void tbSearch_TextLostFocus(object sender, EventArgs e)
        {
            this.inputPanel.Enabled = false;
        }

        protected override void OnKeyDown(KeyEventArgs e)
        {
           //TODO
            base.OnKeyDown(e);
        }

        private void comboBoxElement_SelectedIndexChanged(object sender, EventArgs e)
        {
            this.tbSearch.ReadOnly = false;
            this.radioButtonFavorites.Checked = false;
            this.radioButtonReminder.Checked = false;
        }

        private String encode(String text) //for searching in the encrypted database
        {
            String result = "";
            if (comboBoxElement.SelectedIndex > 0)
            {
                /**
                 * Do not modify this part of the software!
                 * Remember this Copyrights of tvbrowser.org
                 * for further information visit our wiki: http://hilfe.tvbrowser.org
                 **/
                for (int i = 0; i < text.Length; i++)
                {
                    int position = (int)text.ToCharArray()[i];
                    position = position + 7;
                    result += (char)position;
                }
                result = result.Replace("'", "@_@");
            }
            else
                result = text;
            return result;
        }

        private void refreshLanguage()
        {
            this.lSearch.Text = this.con.getLanguageElement("Search.AnyBroadcasts", "Search for any broadcasts");
            this.bSearch.Text = this.con.getLanguageElement("Search.ButtonSearch", "search");
            this.radioButtonFavorites.Text = this.con.getLanguageElement("Search.RadiobuttonFavorites", "favorites");
            this.radioButtonReminder.Text = this.con.getLanguageElement("Search.RadiobuttonReminders", "reminders");
            this.checkBoxExact.Text = this.con.getLanguageElement("Search.Exact", "exact?");
            this.Text = this.con.getLanguageElement("Search.Text", "Search");
            this.comboBoxElement.Items[0]=this.con.getLanguageElement("Database.Broadcast.title","title");
        }

        void radioButtonFavorites_Click(object sender, System.EventArgs e)
        {
            if (this.favoriteChecked)
            {
                this.tbSearch.ReadOnly = false;
                this.radioButtonFavorites.Checked = false;
                this.tbSearch.Text = lastSearchWord;
                this.checkBoxExact.Enabled = true;
                this.favoriteChecked = false;
            }
            else
            {
                this.radioButtonFavorites.Checked = true;
                this.tbSearch.ReadOnly = true;
                this.lastSearchWord = this.tbSearch.Text;
                this.tbSearch.Text = "";
                this.checkBoxExact.Enabled = false;
                this.favoriteChecked = true;
                this.reminderChecked = false;
            }
        }

        void radioButtonReminder_Click(object sender, System.EventArgs e)
        {
            if (this.reminderChecked)
            {
                this.tbSearch.ReadOnly = false;
                this.radioButtonReminder.Checked = false;
                this.tbSearch.Text = lastSearchWord;
                this.checkBoxExact.Enabled = true;
                this.reminderChecked = false;
            }
            else
            {
                this.radioButtonReminder.Checked = true;
                this.tbSearch.ReadOnly = true;
                this.lastSearchWord = this.tbSearch.Text;
                this.tbSearch.Text = "";
                this.checkBoxExact.Enabled = false;
                this.reminderChecked = true;
                this.favoriteChecked = false;
            }
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
    }
}