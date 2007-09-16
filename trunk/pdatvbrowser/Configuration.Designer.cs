namespace PocketTVBrowserCF2
{
    partial class Configuration
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
            this.textBoxPathReminderSound = new System.Windows.Forms.TextBox();
            this.mainMenu1 = new System.Windows.Forms.MainMenu();
            this.tabControlConfiguration = new System.Windows.Forms.TabControl();
            this.tabGeneral = new System.Windows.Forms.TabPage();
            this.lSeconds = new System.Windows.Forms.Label();
            this.lLanguage = new System.Windows.Forms.Label();
            this.comboBoxLanguages = new System.Windows.Forms.ComboBox();
            this.comboBoxReload = new System.Windows.Forms.ComboBox();
            this.labelReload = new System.Windows.Forms.Label();
            this.bBrowseTVData = new System.Windows.Forms.Button();
            this.labelPathDB = new System.Windows.Forms.Label();
            this.textBoxPathDB = new System.Windows.Forms.TextBox();
            this.tabReminders = new System.Windows.Forms.TabPage();
            this.buttonBrowseReminderSound = new System.Windows.Forms.Button();
            this.checkBoxPopupReminders = new System.Windows.Forms.CheckBox();
            this.lRemindersMinutes = new System.Windows.Forms.Label();
            this.comboBoxRemindersMinutes = new System.Windows.Forms.ComboBox();
            this.labelRemindersMinutes = new System.Windows.Forms.Label();
            this.labelReminderWav = new System.Windows.Forms.Label();
            this.openFileDialogTVData = new System.Windows.Forms.OpenFileDialog();
            this.openFileDialogSound = new System.Windows.Forms.OpenFileDialog();
            this.inputPanel = new Microsoft.WindowsCE.Forms.InputPanel();
            this.tabControlConfiguration.SuspendLayout();
            this.tabGeneral.SuspendLayout();
            this.tabReminders.SuspendLayout();
            this.SuspendLayout();
            // 
            // textBoxPathReminderSound
            // 
            this.textBoxPathReminderSound.Location = new System.Drawing.Point(7, 25);
            this.textBoxPathReminderSound.Name = "textBoxPathReminderSound";
            this.textBoxPathReminderSound.Size = new System.Drawing.Size(172, 21);
            this.textBoxPathReminderSound.TabIndex = 4;
            this.textBoxPathReminderSound.Text = "alarm.wav";
            this.textBoxPathReminderSound.LostFocus += new System.EventHandler(this.tbPath_TextLostFocus);
            this.textBoxPathReminderSound.GotFocus += new System.EventHandler(this.tbPath_TextGotFocus);
            // 
            // tabControlConfiguration
            // 
            this.tabControlConfiguration.Controls.Add(this.tabGeneral);
            this.tabControlConfiguration.Controls.Add(this.tabReminders);
            this.tabControlConfiguration.Location = new System.Drawing.Point(0, 0);
            this.tabControlConfiguration.Name = "tabControlConfiguration";
            this.tabControlConfiguration.SelectedIndex = 0;
            this.tabControlConfiguration.Size = new System.Drawing.Size(240, 268);
            this.tabControlConfiguration.TabIndex = 0;
            // 
            // tabGeneral
            // 
            this.tabGeneral.AutoScroll = true;
            this.tabGeneral.Controls.Add(this.lSeconds);
            this.tabGeneral.Controls.Add(this.lLanguage);
            this.tabGeneral.Controls.Add(this.comboBoxLanguages);
            this.tabGeneral.Controls.Add(this.comboBoxReload);
            this.tabGeneral.Controls.Add(this.labelReload);
            this.tabGeneral.Controls.Add(this.bBrowseTVData);
            this.tabGeneral.Controls.Add(this.labelPathDB);
            this.tabGeneral.Controls.Add(this.textBoxPathDB);
            this.tabGeneral.Location = new System.Drawing.Point(0, 0);
            this.tabGeneral.Name = "tabGeneral";
            this.tabGeneral.Size = new System.Drawing.Size(240, 245);
            this.tabGeneral.Text = "general";
            // 
            // lSeconds
            // 
            this.lSeconds.Location = new System.Drawing.Point(161, 61);
            this.lSeconds.Name = "lSeconds";
            this.lSeconds.Size = new System.Drawing.Size(72, 20);
            this.lSeconds.Text = "seconds";
            // 
            // lLanguage
            // 
            this.lLanguage.Location = new System.Drawing.Point(7, 96);
            this.lLanguage.Name = "lLanguage";
            this.lLanguage.Size = new System.Drawing.Size(72, 20);
            this.lLanguage.Text = "language";
            // 
            // comboBoxLanguages
            // 
            this.comboBoxLanguages.Location = new System.Drawing.Point(85, 92);
            this.comboBoxLanguages.Name = "comboBoxLanguages";
            this.comboBoxLanguages.Size = new System.Drawing.Size(148, 22);
            this.comboBoxLanguages.TabIndex = 31;
            // 
            // comboBoxReload
            // 
            this.comboBoxReload.Items.Add("-disabled-");
            this.comboBoxReload.Items.Add("60");
            this.comboBoxReload.Items.Add("120");
            this.comboBoxReload.Items.Add("180");
            this.comboBoxReload.Items.Add("240");
            this.comboBoxReload.Items.Add("300");
            this.comboBoxReload.Items.Add("600");
            this.comboBoxReload.Items.Add("1800");
            this.comboBoxReload.Location = new System.Drawing.Point(85, 58);
            this.comboBoxReload.Name = "comboBoxReload";
            this.comboBoxReload.Size = new System.Drawing.Size(76, 22);
            this.comboBoxReload.TabIndex = 27;
            // 
            // labelReload
            // 
            this.labelReload.Location = new System.Drawing.Point(7, 61);
            this.labelReload.Name = "labelReload";
            this.labelReload.Size = new System.Drawing.Size(79, 20);
            this.labelReload.Text = "refresh every";
            // 
            // bBrowseTVData
            // 
            this.bBrowseTVData.Font = new System.Drawing.Font("Tahoma", 8F, System.Drawing.FontStyle.Bold);
            this.bBrowseTVData.Location = new System.Drawing.Point(186, 26);
            this.bBrowseTVData.Name = "bBrowseTVData";
            this.bBrowseTVData.Size = new System.Drawing.Size(47, 20);
            this.bBrowseTVData.TabIndex = 17;
            this.bBrowseTVData.Text = "browse";
            this.bBrowseTVData.Click += new System.EventHandler(this.bBrowseTVData_Click);
            // 
            // labelPathDB
            // 
            this.labelPathDB.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
            this.labelPathDB.Location = new System.Drawing.Point(7, 3);
            this.labelPathDB.Name = "labelPathDB";
            this.labelPathDB.Size = new System.Drawing.Size(226, 14);
            this.labelPathDB.Text = "Path to your TV-Data File (*.tvd)";
            // 
            // textBoxPathDB
            // 
            this.textBoxPathDB.Location = new System.Drawing.Point(7, 25);
            this.textBoxPathDB.Name = "textBoxPathDB";
            this.textBoxPathDB.Size = new System.Drawing.Size(172, 21);
            this.textBoxPathDB.TabIndex = 2;
            this.textBoxPathDB.Text = "tvdata.tvd";
            this.textBoxPathDB.LostFocus += new System.EventHandler(this.tbPath_TextLostFocus);
            this.textBoxPathDB.GotFocus += new System.EventHandler(this.tbPath_TextGotFocus);
            // 
            // tabReminders
            // 
            this.tabReminders.Controls.Add(this.buttonBrowseReminderSound);
            this.tabReminders.Controls.Add(this.checkBoxPopupReminders);
            this.tabReminders.Controls.Add(this.lRemindersMinutes);
            this.tabReminders.Controls.Add(this.comboBoxRemindersMinutes);
            this.tabReminders.Controls.Add(this.labelRemindersMinutes);
            this.tabReminders.Controls.Add(this.labelReminderWav);
            this.tabReminders.Controls.Add(this.textBoxPathReminderSound);
            this.tabReminders.Location = new System.Drawing.Point(0, 0);
            this.tabReminders.Name = "tabReminders";
            this.tabReminders.Size = new System.Drawing.Size(240, 245);
            this.tabReminders.Text = "reminders";
            // 
            // buttonBrowseReminderSound
            // 
            this.buttonBrowseReminderSound.Font = new System.Drawing.Font("Tahoma", 8F, System.Drawing.FontStyle.Bold);
            this.buttonBrowseReminderSound.Location = new System.Drawing.Point(185, 26);
            this.buttonBrowseReminderSound.Name = "buttonBrowseReminderSound";
            this.buttonBrowseReminderSound.Size = new System.Drawing.Size(48, 20);
            this.buttonBrowseReminderSound.TabIndex = 14;
            this.buttonBrowseReminderSound.Text = "browse";
            this.buttonBrowseReminderSound.Click += new System.EventHandler(this.buttonBrowseReminderSound_Click);
            // 
            // checkBoxPopupReminders
            // 
            this.checkBoxPopupReminders.Location = new System.Drawing.Point(7, 138);
            this.checkBoxPopupReminders.Name = "checkBoxPopupReminders";
            this.checkBoxPopupReminders.Size = new System.Drawing.Size(172, 20);
            this.checkBoxPopupReminders.TabIndex = 10;
            this.checkBoxPopupReminders.Text = "Show Popups";
            // 
            // lRemindersMinutes
            // 
            this.lRemindersMinutes.Location = new System.Drawing.Point(113, 99);
            this.lRemindersMinutes.Name = "lRemindersMinutes";
            this.lRemindersMinutes.Size = new System.Drawing.Size(66, 20);
            this.lRemindersMinutes.Text = "minutes";
            // 
            // comboBoxRemindersMinutes
            // 
            this.comboBoxRemindersMinutes.Items.Add("no timer");
            this.comboBoxRemindersMinutes.Items.Add("0");
            this.comboBoxRemindersMinutes.Items.Add("5");
            this.comboBoxRemindersMinutes.Items.Add("10");
            this.comboBoxRemindersMinutes.Items.Add("15");
            this.comboBoxRemindersMinutes.Items.Add("30");
            this.comboBoxRemindersMinutes.Items.Add("60");
            this.comboBoxRemindersMinutes.Items.Add("120");
            this.comboBoxRemindersMinutes.Items.Add("180");
            this.comboBoxRemindersMinutes.Location = new System.Drawing.Point(7, 97);
            this.comboBoxRemindersMinutes.Name = "comboBoxRemindersMinutes";
            this.comboBoxRemindersMinutes.Size = new System.Drawing.Size(100, 22);
            this.comboBoxRemindersMinutes.TabIndex = 7;
            // 
            // labelRemindersMinutes
            // 
            this.labelRemindersMinutes.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
            this.labelRemindersMinutes.Location = new System.Drawing.Point(7, 59);
            this.labelRemindersMinutes.Name = "labelRemindersMinutes";
            this.labelRemindersMinutes.Size = new System.Drawing.Size(226, 35);
            this.labelRemindersMinutes.Text = "Remind me X minutes before broadcasts beginning";
            // 
            // labelReminderWav
            // 
            this.labelReminderWav.Font = new System.Drawing.Font("Tahoma", 9F, System.Drawing.FontStyle.Bold);
            this.labelReminderWav.Location = new System.Drawing.Point(7, 4);
            this.labelReminderWav.Name = "labelReminderWav";
            this.labelReminderWav.Size = new System.Drawing.Size(226, 14);
            this.labelReminderWav.Text = "Play this Sound (*.wav)";
            // 
            // openFileDialogTVData
            // 
            this.openFileDialogTVData.FileName = "tvdata.tvd";
            // 
            // openFileDialogSound
            // 
            this.openFileDialogSound.FileName = "alarm.wav";
            // 
            // Configuration
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoScroll = true;
            this.ClientSize = new System.Drawing.Size(240, 268);
            this.Controls.Add(this.tabControlConfiguration);
            this.KeyPreview = true;
            this.Menu = this.mainMenu1;
            this.Name = "Configuration";
            this.Text = "Configuration";
            this.Closed += new System.EventHandler(this.bSave_Click);
            this.tabControlConfiguration.ResumeLayout(false);
            this.tabGeneral.ResumeLayout(false);
            this.tabReminders.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.TabControl tabControlConfiguration;
        private System.Windows.Forms.TabPage tabGeneral;
        private System.Windows.Forms.TabPage tabReminders;
        private System.Windows.Forms.Label labelPathDB;
        private System.Windows.Forms.TextBox textBoxPathDB;
        private System.Windows.Forms.OpenFileDialog openFileDialogTVData;
        private System.Windows.Forms.Label lRemindersMinutes;
        private System.Windows.Forms.ComboBox comboBoxRemindersMinutes;
        private System.Windows.Forms.Label labelRemindersMinutes;
        private System.Windows.Forms.Label labelReminderWav;
        private System.Windows.Forms.Button bBrowseTVData;
        private System.Windows.Forms.TextBox textBoxPathReminderSound;
        private System.Windows.Forms.CheckBox checkBoxPopupReminders;
        private System.Windows.Forms.Label labelReload;
        private System.Windows.Forms.ComboBox comboBoxReload;
        private System.Windows.Forms.Button buttonBrowseReminderSound;
        private System.Windows.Forms.OpenFileDialog openFileDialogSound;
        private Microsoft.WindowsCE.Forms.InputPanel inputPanel;
        private System.Windows.Forms.Label lLanguage;
        private System.Windows.Forms.ComboBox comboBoxLanguages;
        private System.Windows.Forms.Label lSeconds;
    }
}