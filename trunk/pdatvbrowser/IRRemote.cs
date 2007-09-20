using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Net.Sockets;
using System.Net;
using System.IO;

namespace PocketTVBrowserCF2
{
    public partial class IRRemote : Form
    {
        public IRRemote()
        {
            InitializeComponent();
            this.DialogResult = DialogResult.OK;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            try
            {
                Cursor.Current = Cursors.WaitCursor;
                IrDAClient client = new IrDAClient();
                IrDADeviceInfo[] infos = client.DiscoverDevices(1);
                int counter = 0;

                while (infos.Length == 0 && counter < 20)
                {
                    infos = client.DiscoverDevices(1);
                    System.Threading.Thread.Sleep(100);
                    counter++;
                }
                Cursor.Current = Cursors.Default;
                if (infos.Length != 0)
                {
                    MessageBox.Show(infos[0].DeviceName + "(" + infos[0].DeviceID + "), " + infos[0].Hints.ToString());
                    IrDAEndPoint endpoint = new IrDAEndPoint(infos[0].DeviceID, "Test");
                    client.Connect(endpoint);
                    
                    Stream stream = client.GetStream();
                    //stream.Write(
                }
                else
                {
                    MessageBox.Show("nix gefunden");
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
            }
        }

        private void buttonVplumeMinus_Click(object sender, EventArgs e)
        {

        }

        private void buttonVolumePlus_Click(object sender, EventArgs e)
        {

        }

        private void buttonChannelMinus_Click(object sender, EventArgs e)
        {

        }

        private void buttonChannelPlus_Click(object sender, EventArgs e)
        {

        }

        private void buttonOnOff_Click(object sender, EventArgs e)
        {

        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            DialogResult = DialogResult.OK;
        }
    }
}