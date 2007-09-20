namespace PocketTVBrowserCF2
{
    partial class IRRemote
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.MainMenu mainMenu1;

        /// <summary>
        /// Verwendete Ressourcen bereinigen.
        /// </summary>
        /// <param name="disposing">True, wenn verwaltete Ressourcen gelöscht werden sollen; andernfalls False.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Vom Windows Form-Designer generierter Code

        /// <summary>
        /// Erforderliche Methode für die Designerunterstützung.
        /// Der Inhalt der Methode darf nicht mit dem Code-Editor geändert werden.
        /// </summary>
        private void InitializeComponent()
        {
            this.mainMenu1 = new System.Windows.Forms.MainMenu();
            this.button = new System.Windows.Forms.Button();
            this.buttonOnOff = new System.Windows.Forms.Button();
            this.buttonChannelPlus = new System.Windows.Forms.Button();
            this.buttonChannelMinus = new System.Windows.Forms.Button();
            this.buttonVolumePlus = new System.Windows.Forms.Button();
            this.buttonVplumeMinus = new System.Windows.Forms.Button();
            this.buttonClose = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // button
            // 
            this.button.BackColor = System.Drawing.Color.White;
            this.button.Location = new System.Drawing.Point(8, 42);
            this.button.Name = "button";
            this.button.Size = new System.Drawing.Size(224, 18);
            this.button.TabIndex = 0;
            this.button.Text = "Gerät zuweisen";
            this.button.Click += new System.EventHandler(this.button1_Click);
            // 
            // buttonOnOff
            // 
            this.buttonOnOff.BackColor = System.Drawing.Color.Red;
            this.buttonOnOff.Font = new System.Drawing.Font("Tahoma", 7F, System.Drawing.FontStyle.Bold);
            this.buttonOnOff.Location = new System.Drawing.Point(8, 8);
            this.buttonOnOff.Name = "buttonOnOff";
            this.buttonOnOff.Size = new System.Drawing.Size(28, 28);
            this.buttonOnOff.TabIndex = 1;
            this.buttonOnOff.Text = "On";
            this.buttonOnOff.Click += new System.EventHandler(this.buttonOnOff_Click);
            // 
            // buttonChannelPlus
            // 
            this.buttonChannelPlus.BackColor = System.Drawing.Color.Black;
            this.buttonChannelPlus.ForeColor = System.Drawing.SystemColors.ActiveCaptionText;
            this.buttonChannelPlus.Location = new System.Drawing.Point(44, 8);
            this.buttonChannelPlus.Name = "buttonChannelPlus";
            this.buttonChannelPlus.Size = new System.Drawing.Size(28, 28);
            this.buttonChannelPlus.TabIndex = 2;
            this.buttonChannelPlus.Text = "C+";
            this.buttonChannelPlus.Click += new System.EventHandler(this.buttonChannelPlus_Click);
            // 
            // buttonChannelMinus
            // 
            this.buttonChannelMinus.BackColor = System.Drawing.Color.Black;
            this.buttonChannelMinus.ForeColor = System.Drawing.Color.White;
            this.buttonChannelMinus.Location = new System.Drawing.Point(78, 8);
            this.buttonChannelMinus.Name = "buttonChannelMinus";
            this.buttonChannelMinus.Size = new System.Drawing.Size(28, 28);
            this.buttonChannelMinus.TabIndex = 3;
            this.buttonChannelMinus.Text = "C-";
            this.buttonChannelMinus.Click += new System.EventHandler(this.buttonChannelMinus_Click);
            // 
            // buttonVolumePlus
            // 
            this.buttonVolumePlus.BackColor = System.Drawing.Color.Black;
            this.buttonVolumePlus.ForeColor = System.Drawing.Color.White;
            this.buttonVolumePlus.Location = new System.Drawing.Point(112, 8);
            this.buttonVolumePlus.Name = "buttonVolumePlus";
            this.buttonVolumePlus.Size = new System.Drawing.Size(28, 28);
            this.buttonVolumePlus.TabIndex = 4;
            this.buttonVolumePlus.Text = "V+";
            this.buttonVolumePlus.Click += new System.EventHandler(this.buttonVolumePlus_Click);
            // 
            // buttonVplumeMinus
            // 
            this.buttonVplumeMinus.BackColor = System.Drawing.Color.Black;
            this.buttonVplumeMinus.ForeColor = System.Drawing.Color.White;
            this.buttonVplumeMinus.Location = new System.Drawing.Point(146, 8);
            this.buttonVplumeMinus.Name = "buttonVplumeMinus";
            this.buttonVplumeMinus.Size = new System.Drawing.Size(28, 28);
            this.buttonVplumeMinus.TabIndex = 5;
            this.buttonVplumeMinus.Text = "V-";
            this.buttonVplumeMinus.Click += new System.EventHandler(this.buttonVplumeMinus_Click);
            // 
            // buttonClose
            // 
            this.buttonClose.BackColor = System.Drawing.Color.Transparent;
            this.buttonClose.Font = new System.Drawing.Font("Tahoma", 12F, System.Drawing.FontStyle.Bold);
            this.buttonClose.ForeColor = System.Drawing.Color.Black;
            this.buttonClose.Location = new System.Drawing.Point(180, 8);
            this.buttonClose.Name = "buttonClose";
            this.buttonClose.Size = new System.Drawing.Size(52, 28);
            this.buttonClose.TabIndex = 6;
            this.buttonClose.Text = "X";
            this.buttonClose.Click += new System.EventHandler(this.buttonClose_Click);
            // 
            // IRRemote
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(224)))), ((int)(((byte)(224)))), ((int)(((byte)(224)))));
            this.ClientSize = new System.Drawing.Size(240, 65);
            this.Controls.Add(this.buttonClose);
            this.Controls.Add(this.buttonVplumeMinus);
            this.Controls.Add(this.buttonVolumePlus);
            this.Controls.Add(this.buttonChannelMinus);
            this.Controls.Add(this.buttonChannelPlus);
            this.Controls.Add(this.buttonOnOff);
            this.Controls.Add(this.button);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.None;
            this.MinimizeBox = false;
            this.Name = "IRRemote";
            this.Text = "IRRemote";
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button button;
        private System.Windows.Forms.Button buttonOnOff;
        private System.Windows.Forms.Button buttonChannelPlus;
        private System.Windows.Forms.Button buttonChannelMinus;
        private System.Windows.Forms.Button buttonVolumePlus;
        private System.Windows.Forms.Button buttonVplumeMinus;
        private System.Windows.Forms.Button buttonClose;
    }
}