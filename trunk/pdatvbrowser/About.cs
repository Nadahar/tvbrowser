using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;


namespace PocketTVBrowserCF2
{
    public partial class About : Form
    {
        private TVBrowserControll con;
        public About(TVBrowserControll con)
        {
            this.con = con;
            InitializeComponent();
            this.initVideoMode();
            this.refreshLanguage();
        }

        private void initVideoMode()
        {
            this.lVersion.Bounds = new Rectangle(0, 0, this.Width, this.lVersion.Height);
            if (this.pictureBoxIcon.Height > this.Height / 2)
            {
                this.pictureBoxIcon.Height = this.pictureBoxIcon.Height / 2;
                this.pictureBoxIcon.Width = this.pictureBoxIcon.Width / 2;
            }
            this.pictureBoxIcon.Bounds = new Rectangle((this.Width-this.pictureBoxIcon.Width)/2, this.lVersion.Height, this.pictureBoxIcon.Width, this.pictureBoxIcon.Height);
            this.labelThanks.Bounds = new Rectangle(1, this.pictureBoxIcon.Bounds.Bottom, this.Width-1, this.labelThanks.Height);
            this.lCopyright.Bounds = new Rectangle(0, this.Height - this.lCopyright.Height, this.Width, this.lCopyright.Height);
            this.lGNU.Bounds = new Rectangle(0, this.Height - this.lCopyright.Height - this.lGNU.Height, this.Width, this.lGNU.Height);
            this.textBoxThanksTo.Bounds = new Rectangle(1, this.labelThanks.Bounds.Bottom, this.Width-1, this.Height - this.lVersion.Height - this.pictureBoxIcon.Height - this.labelThanks.Height - this.lGNU.Height - this.lCopyright.Height);  
        }

        private void refreshLanguage()
        {
            this.lGNU.Text = this.con.getLanguageElement("About.CreatedBy", "created by \r\nBenedikt Grabenmeier, Thilo Steinert");
            this.labelThanks.Text = this.con.getLanguageElement("About.SpecialThanks", "Special thanks to:");
            this.Text = this.con.getLanguageElement("About.Text", "About");
            this.textBoxThanksTo.Text = this.con.getLanguageElement("About.ThanksTo", "the SQLite team for the database, the testers (especially Thilo Steinert, Mario Siegmann, Bernd Frey, Jan Schütte,  DJ Bone) and Ihmchen for the BussyMix ;-)");
        }

        protected override void OnResize(EventArgs e)
        {
            this.initVideoMode();
            base.OnResize(e);
        }
    }
}