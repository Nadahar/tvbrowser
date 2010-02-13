#ifndef MAINWINDOW_H
#define MAINWINDOW_H
#include "runsnowmodel.h"
#include <QMainWindow>
#include <QDateTime>

namespace Ui {
    class MainWindow;
}

class MainWindow : public QMainWindow {
    Q_OBJECT
public:
    MainWindow(QWidget *parent = 0);
    ~MainWindow();
    runsnowmodel *model;


protected:
    void changeEvent(QEvent *e);

private:
    Ui::MainWindow *ui;

private slots:
    void on_rbAm_toggled(bool checked);
    void on_rbCB_toggled(bool checked);
    void on_tableView_doubleClicked(QModelIndex index);
    void on_pbRefresh_clicked();
    void AddToTable(QString sender, QString Sendung, QString VonBis, QString sid);
    void ClearTable();
    QString DecryptText(char* sText, bool replace8);
    void LoadTVData(QDateTime dts);
    QString GetKurzinfo(QString sSID);
};

#endif // MAINWINDOW_H
