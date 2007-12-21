using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace TVBrowserMini
{
    public partial class Transfer : Form
    {
        TVBrowserControll con;
        public Transfer(TVBrowserControll con)
        {
            this.con = con;
            InitializeComponent();
            Rectangle screen = Screen.PrimaryScreen.Bounds;
            this.initVideoMode();
            this.refreshLanguage();
        }

        private void initVideoMode()
        {
            Rectangle screen = Screen.PrimaryScreen.Bounds;
            this.Location = new Point((screen.Width - this.Width) / 2, (screen.Height - this.Height) / 2);
            this.labelHeadline.Bounds = new Rectangle(1, 2, this.Width-2, this.labelHeadline.Height);
            this.labelPercent.Bounds = new Rectangle(1, this.labelHeadline.Bottom + 1, this.Width - 2, this.labelPercent.Height);
        }

        protected override void OnResize(EventArgs e)
        {
            Rectangle screen = Screen.PrimaryScreen.Bounds;
            this.Location = new Point((screen.Width - this.Width) / 2, (screen.Height - this.Height) / 2);
            base.OnResize(e);
        }
       
        public void setTextLabelPercent(String text)
        {
            this.labelPercent.Text = text;
            this.Refresh();
        }
        private void refreshLanguage()
        {
            this.labelHeadline.Text = this.con.getLanguageElement("Transfer.LabelHeadline", "TV-Data synchronisation");
            this.Text = this.con.getLanguageElement("Transfer.Text", "TV-Data synchronisation");
        }
    }
}