using System;
using System.Collections.Generic;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace TVBrowserMini
{
    public partial class SearchStartingAt : Form
    {
        private TVBrowserControll con;
        private Mainform main;
        private DateTime selectedDate;
        private int selectedHour;
        private int selectedMinute;
        private ArrayList broadcasts;

        public SearchStartingAt(TVBrowserControll con, Mainform main)
        {
            this.con = con;
            this.main = main;
            InitializeComponent();
            this.refreshLanguage();
            this.ControlBox = true;
            Rectangle screen = Screen.PrimaryScreen.Bounds;
            this.Location = new Point((screen.Width - this.Width) / 2, (screen.Height - this.Height) / 2);
            this.init();
            this.broadcasts = new ArrayList();
        }

        private void init()
        {
            ArrayList dates = this.con.getDates(this.con.getCurrentChannel().getId());
            TVBrowserDate min = (TVBrowserDate)dates[0];
            TVBrowserDate max = (TVBrowserDate)dates[dates.Count - 1];
            this.dateTimePicker.MinDate = min.getDateTime();
            this.dateTimePicker.MaxDate = max.getDateTime();
            this.dateTimePicker.Format = DateTimePickerFormat.Long;
        }

        private void buttonOK_Click(object sender, EventArgs e)
        {
            Cursor.Current = Cursors.WaitCursor;
            this.DialogResult = DialogResult.OK;
            this.selectedDate = this.dateTimePicker.Value;
            this.selectedHour = (int)this.numericUpDownHour.Value;
            this.selectedMinute = (int)this.numericUpDownMinute.Value;

            try
            {
                this.Owner.BringToFront();
                this.Owner.Refresh();
                this.Owner.Focus();
                this.Hide();
            }
            catch
            {
            }
            this.con.getMainform().Show();
            this.broadcasts = this.con.getBroadcastsRunningAt(this.getDateTime());
            Cursor.Current = Cursors.Default;
        }

        public DateTime getDateTime()
        {  
            DateTime result = new DateTime(this.selectedDate.Year, this.selectedDate.Month, this.selectedDate.Day, this.selectedHour, this.selectedMinute, 0);
            return result;
        }

        public ArrayList getBroadcasts()
        {
            return this.broadcasts;
        }

        private void refreshLanguage()
        {
            this.lHeadline.Text = this.con.getLanguageElement("Search.Headline", "Show broadcasts running at");
            this.Text = this.con.getLanguageElement("Search.Text", "Search broadcasts starting at");
        }

        private void buttonCancel_Click(object sender, EventArgs e)
        {
            this.con.getMainform().Show();
            this.DialogResult = DialogResult.Cancel;
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

    }
}