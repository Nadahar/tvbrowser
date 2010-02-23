#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "runsnowmodel.h"
#include <QMessageBox>
#include <QAbstractTableModel>
#include <QStringList>
#include <QMovie>
#include <QDialog>
#include <QSettings>
#include <QDir>
#include <QTextCodec>
#include <QWaitCondition>
#include <QThread>
#include "sqlite_source/sqlite3.h"


MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    this->setWindowTitle("TV-Browser");


    model = new runsnowmodel(this);

    ui->tableView->setModel(model);
    ui->tableView->setColumnHidden (2, true);
    ui->tableView->setColumnHidden (3, true);
    ui->tableView->setColumnWidth(0,220);
    ui->tableView->setColumnWidth(1,560);
    //ui->tableView->verticalHeader()->setDefaultSectionSize(50); //setHeight
    ui->tableView->setColumnWidth(2,0);

    ui->tableView->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
    ui->tableView->setEditTriggers(QAbstractItemView::NoEditTriggers);
    ui->tableView->setSelectionMode(QAbstractItemView::SingleSelection);

#ifdef Q_WS_HILDON
       this->setProperty("FingerScrollable", true);
       ui->tableView->setProperty("FingerScrollable", true);
#endif


    ui->cbTime->addItem(tr("Now"),QVariant("0"));
    ui->cbTime->addItem(tr("in 15 minutes"),QVariant("1"));
    ui->cbTime->addItem(tr("in 30 minutes"),QVariant("2"));
    ui->cbTime->addItem(tr("at 06:00"),QVariant("3"));
    ui->cbTime->addItem(tr("at 12:00"),QVariant("4"));
    ui->cbTime->addItem(tr("at 18:00"),QVariant("5"));
    ui->cbTime->addItem(tr("at 20:15"),QVariant("6"));
    ui->cbTime->addItem(tr("at 22:00"),QVariant("7"));

    ui->cbTime->setCurrentIndex(0);
    ui->dteTime->setDateTime(QDateTime::currentDateTime());


    ui->rbAm->setText(tr("On"));
    ui->rbCB->setText(tr("Running"));

    ui->rbCB->setChecked(true);



}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::ClearTable()
{
  int iRows = model->rowCount( QModelIndex());
  for(int i=iRows -1; i >= 0; i = i - 1)
  {
      model->removeRow(i,QModelIndex());
  }

}


void MainWindow::AddToTable(QString sender, QString Sendung, QString VonBis, QString sid)
{
   int iRows = model->rowCount( QModelIndex());

   model->insertRows(iRows, 1, QModelIndex());
   QModelIndex index = model->index(iRows, 0, QModelIndex());
   model->setData(index, sender, Qt::EditRole);
   index = model->index(iRows, 1, QModelIndex());
   model->setData(index, Sendung, Qt::EditRole);
   index = model->index(iRows, 2, QModelIndex());
   model->setData(index, VonBis, Qt::EditRole);
   index = model->index(iRows, 3, QModelIndex());
   model->setData(index, sid, Qt::EditRole);


}

void MainWindow::changeEvent(QEvent *e)
{
    QMainWindow::changeEvent(e);
    switch (e->type()) {
    case QEvent::LanguageChange:
        ui->retranslateUi(this);
        break;
    default:
        break;
    }
}




QString MainWindow::DecryptText(char* sText, bool replace8)
{
    QString sResult = "";
    QString sText2 = "";
    if (sText == NULL)
    {
        return "";
    }
    sText2 = QString::fromUtf8(sText,strlen(sText));
    sText2 = sText2.replace("@_@", "'",Qt::CaseSensitive);

    for (int i = 0; i < sText2.length(); i++)
    {
        QByteArray ba = sText2.toAscii();
        char  cchar = ba.at(i);
        sResult = sResult + QString((char) (int(cchar) - 7));
    }


    if (replace8 == true)
    {
        //problem mit qt auf maemo 5.
        // ü wird zur 8
        // unschöne lösung :(
        sResult = sResult.replace("8", "ü",Qt::CaseSensitive);
    }

    return sResult;
}

void MainWindow::LoadTVData(QDateTime dts)
{

#ifdef Q_WS_HILDON
    QString sAppDir = QDir::homePath() + QLatin1String("/MyDocs/tv-browser");
    QDir dir;
    dir.mkpath(sAppDir);
#else
    QString sAppDir = QApplication::applicationDirPath();
#endif



    QString sID;
    QString lastErrorMessage;
    QString DBd = sAppDir + "/tvexp.tvd";

    QFile f( DBd);
    if( !f.exists() )
    {
      QMessageBox::critical(NULL, tr("No Databasefile"), tr("There is no Databasefile in: \n") + DBd);
      //QApplication::quit();
      return;
    }

    sqlite3 *db;
    int err=0;
    err = sqlite3_open(DBd.toUtf8().data(), &db);
    if ( err ) {
        lastErrorMessage = sqlite3_errmsg(db);
        sqlite3_close(db);
        return;
    }
    sqlite3_stmt *vm;
    const char *tail;
    //QString statement ="SELECT channel.name,broadcast.id,title,start,end FROM broadcast INNER JOIN channel on channel.id = broadcast.channel_id where  datetime('" + dts.toString("yyyy-MM-dd hh:mm:ss") +"') between start and end order by channel.name";


    QString statement ="SELECT channel.name,broadcast.id,title,start,end, info.genre,info.produced, info.location ";
            statement = statement + " FROM broadcast ";
            statement = statement + " INNER JOIN channel on channel.id = broadcast.channel_id ";
            statement = statement + " INNER JOIN info on info.broadcast_id = broadcast.id ";
            statement = statement + " where  (datetime('" + dts.toString("yyyy-MM-dd hh:mm:ss") +"') between start and end) and (end > datetime('" + dts.toString("yyyy-MM-dd hh:mm:ss") + "')) ";
            statement = statement + " order by channel.name ";


    sqlite3_prepare(db,statement.toUtf8().data(),statement.toUtf8().length(),&vm, &tail);
    if (err == SQLITE_OK){
        while ( sqlite3_step(vm) == SQLITE_ROW ){
             //QTime dieTime = QTime::currentTime().addSecs(2);
             //while( QTime::currentTime() < dieTime )
             //QCoreApplication::processEvents(QEventLoop::AllEvents, 100);



             char* cChanName    = (char *) sqlite3_column_text(vm, 0);
             char* cBroadcastID = (char *) sqlite3_column_text(vm, 1);
             char* cTitel       = (char *) sqlite3_column_text(vm, 2);
             char* cStart       = (char *) sqlite3_column_text(vm, 3);
             char* cEnd         = (char *) sqlite3_column_text(vm, 4);

             char* cGenre         = (char *) sqlite3_column_text(vm, 5);
             char* cProduced         = (char *) sqlite3_column_text(vm, 6);
             char* cLocation         = (char *) sqlite3_column_text(vm, 7);



             QString sChanName = QString::fromUtf8(cChanName,strlen(cChanName));

             QString sTitleF ="";
             QString sTitle2 = "";
             QString sVonBisF = QVariant(cStart).toDateTime().toString("hh:mm") + " - " + QVariant(cEnd).toDateTime().toString("hh:mm");


             if (cGenre != NULL)
             {
                 sTitle2  = DecryptText(cGenre,true);
             }
             if (cLocation != NULL)
             {
                 if (sTitle2 == "")
                 {
                    sTitle2  = DecryptText(cLocation,true);
                 }else
                 {
                    sTitle2  = sTitle2  + " - " + DecryptText(cLocation,true);
                 }

             }
             if (cProduced != NULL)
             {
                 if (sTitle2 == "")
                 {
                    sTitle2  = DecryptText(cProduced,false);
                 }else
                 {
                    sTitle2  = sTitle2  + " - " + DecryptText(cProduced,false);
                 }
             }


             sTitleF = sVonBisF + " - " +  QString::fromUtf8(cTitel,strlen(cTitel)) + "\n" + sTitle2;


             AddToTable(sChanName,sTitleF, sVonBisF,QString::fromUtf8(cBroadcastID,strlen(cBroadcastID)));



        }

    }
    sqlite3_finalize(vm);
    sqlite3_close(db);
    ui->tableView->resizeRowsToContents();
    //ui->tableView->resizeColumnToContents(0);
}

void MainWindow::ChangeTVData(int Step)
{

#ifdef Q_WS_HILDON
    QString sAppDir = QDir::homePath() + QLatin1String("/MyDocs/tv-browser");
    QDir dir;
    dir.mkpath(sAppDir);
#else
    QString sAppDir = QApplication::applicationDirPath();
#endif



    QString sID;
    QString lastErrorMessage;
    QString DBd = sAppDir + "/tvexp.tvd";

    QFile f( DBd);
    if( !f.exists() )
    {
      QMessageBox::critical(NULL, tr("No Databasefile"), tr("There is no Databasefile in: \n") + DBd);
      //QApplication::quit();
      return;
    }

    sqlite3 *db;
    int err=0;
    err = sqlite3_open(DBd.toUtf8().data(), &db);
    if ( err ) {
        lastErrorMessage = sqlite3_errmsg(db);
        sqlite3_close(db);
        return;
    }
    sqlite3_stmt *vm;
    const char *tail;

    QString sBroadcastIDs ="";
    int iRows = model->rowCount( QModelIndex());
    for(int i=iRows -1; i >= 0; i = i - 1)
    {
        if (sBroadcastIDs == "")
        {

            sBroadcastIDs ="'" + QVariant( QVariant(QString(model->getData(i,3))).toInt() + Step).toString() + "'";
        }else
        {
            sBroadcastIDs = sBroadcastIDs + ",'" + QVariant( QVariant(QString(model->getData(i,3))).toInt() + Step).toString() + "'";
        }
    }
    if (sBroadcastIDs == "")
    {
        return;
    }
    ClearTable();

    QString statement ="SELECT channel.name,broadcast.id,title,start,end, info.genre,info.produced, info.location ";
            statement = statement + " FROM broadcast ";
            statement = statement + " INNER JOIN channel on channel.id = broadcast.channel_id ";
            statement = statement + " INNER JOIN info on info.broadcast_id = broadcast.id ";
            statement = statement + " where  broadcast.id in(" + sBroadcastIDs + ") ";
            statement = statement + " order by channel.name ";


    sqlite3_prepare(db,statement.toUtf8().data(),statement.toUtf8().length(),&vm, &tail);
    if (err == SQLITE_OK){
        while ( sqlite3_step(vm) == SQLITE_ROW ){
             //QTime dieTime = QTime::currentTime().addSecs(2);
             //while( QTime::currentTime() < dieTime )
             //QCoreApplication::processEvents(QEventLoop::AllEvents, 100);



             char* cChanName    = (char *) sqlite3_column_text(vm, 0);
             char* cBroadcastID = (char *) sqlite3_column_text(vm, 1);
             char* cTitel       = (char *) sqlite3_column_text(vm, 2);
             char* cStart       = (char *) sqlite3_column_text(vm, 3);
             char* cEnd         = (char *) sqlite3_column_text(vm, 4);

             char* cGenre         = (char *) sqlite3_column_text(vm, 5);
             char* cProduced         = (char *) sqlite3_column_text(vm, 6);
             char* cLocation         = (char *) sqlite3_column_text(vm, 7);



             QString sChanName = QString::fromUtf8(cChanName,strlen(cChanName));

             QString sTitleF ="";
             QString sTitle2 = "";
             QString sVonBisF = QVariant(cStart).toDateTime().toString("hh:mm") + " - " + QVariant(cEnd).toDateTime().toString("hh:mm");


             if (cGenre != NULL)
             {
                 sTitle2  = DecryptText(cGenre,true);
             }
             if (cLocation != NULL)
             {
                 if (sTitle2 == "")
                 {
                    sTitle2  = DecryptText(cLocation,true);
                 }else
                 {
                    sTitle2  = sTitle2  + " - " + DecryptText(cLocation,true);
                 }

             }
             if (cProduced != NULL)
             {
                 if (sTitle2 == "")
                 {
                    sTitle2  = DecryptText(cProduced,false);
                 }else
                 {
                    sTitle2  = sTitle2  + " - " + DecryptText(cProduced,false);
                 }
             }


             sTitleF = sVonBisF + " - " +  QString::fromUtf8(cTitel,strlen(cTitel)) + "\n" + sTitle2;


             AddToTable(sChanName,sTitleF, sVonBisF,QString::fromUtf8(cBroadcastID,strlen(cBroadcastID)));



        }

    }
    sqlite3_finalize(vm);
    sqlite3_close(db);
    ui->tableView->resizeRowsToContents();
    //ui->tableView->resizeColumnToContents(0);
}


QString MainWindow::GetKurzinfo(QString sSID)
{
#ifdef Q_WS_HILDON
    QString sAppDir = QDir::homePath() + QLatin1String("/MyDocs/tv-browser");
    QDir dir;
    dir.mkpath(sAppDir);
#else
    QString sAppDir = QApplication::applicationDirPath();
#endif
    QString sID;
    QString lastErrorMessage;
    QString DBd = sAppDir + "/tvexp.tvd";

    QFile f( DBd);
    if( !f.exists() )
    {
      QMessageBox::critical(NULL, tr("No Databasefile"), tr("There is no Databasefile in: \n") + DBd);
      //QApplication::quit();
      return "";
    }

    sqlite3 *db;
    int err=0;
    err = sqlite3_open(DBd.toUtf8().data(), &db);
    if ( err ) {
        lastErrorMessage = sqlite3_errmsg(db);
        sqlite3_close(db);
        return lastErrorMessage;
    }
    sqlite3_stmt *vm;
    QString sInfo ="";
    const char *tail;
    QString statement ="select shortdescription,description  from info where broadcast_id='" + sSID + "'";
    sqlite3_prepare(db,statement.toUtf8().data(),statement.toUtf8().length(),&vm, &tail);
    if (err == SQLITE_OK){
        while ( sqlite3_step(vm) == SQLITE_ROW ){

            char* Text    = (char *) sqlite3_column_text(vm,0);
            if (Text == NULL)
            {
               Text    = (char *) sqlite3_column_text(vm,1);
            }

             sInfo = DecryptText(Text,true);

        }

    }
    sqlite3_finalize(vm);
    sqlite3_close(db);
    return sInfo;
}


void MainWindow::on_pbRefresh_clicked()
{
    QDateTime dts;
    QVariant v;
    ClearTable();

    if (ui->rbCB->isChecked() == true)
    {
        switch(ui->cbTime->currentIndex())
        {
            case 0:
                    dts = QDateTime::currentDateTime();
                    LoadTVData(dts);
                    break;
            case 1:
                    dts = QDateTime::currentDateTime().addSecs(15 * 60);
                    LoadTVData(dts);
                    break;
            case 2:
                    dts = QDateTime::currentDateTime().addSecs(30 * 60);
                    LoadTVData(dts);
                    break;
            case 3:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 06:00:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
            case 4:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 12:00:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
            case 5:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 18:00:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
            case 6:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 20:15:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
            case 7:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 22:00:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
        }

    } else
    {
        dts = ui->dteTime->dateTime();
        LoadTVData(dts);
    }

}

void MainWindow::on_tableView_doubleClicked(QModelIndex index)
{
  QString sTitel = model->getData(index.row(),1);
  QString sSID = model->getData(index.row(),3);
  QString sInfo = GetKurzinfo(sSID);
  QMessageBox::information(NULL, sTitel, sInfo,tr("Done"));
}

void MainWindow::on_rbCB_toggled(bool checked)
{
    if (checked == true)
    {
       ui->dteTime->setEnabled(false);
       ui->cbTime->setEnabled(true);
    }
}

void MainWindow::on_rbAm_toggled(bool checked)
{
    if (checked == true)
    {
       ui->dteTime->setEnabled(true);
       ui->cbTime->setEnabled(false);
    }
}

void MainWindow::on_pbNext_clicked()
{
    ChangeTVData(+1);
}

void MainWindow::on_pbLast_clicked()
{
   ChangeTVData(-1);
}
