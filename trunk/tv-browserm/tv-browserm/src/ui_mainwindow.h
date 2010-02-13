/********************************************************************************
** Form generated from reading UI file 'mainwindow.ui'
**
** Created: Sat 13. Feb 22:30:24 2010
**      by: Qt User Interface Compiler version 4.6.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_MAINWINDOW_H
#define UI_MAINWINDOW_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QComboBox>
#include <QtGui/QDateTimeEdit>
#include <QtGui/QHeaderView>
#include <QtGui/QMainWindow>
#include <QtGui/QPushButton>
#include <QtGui/QRadioButton>
#include <QtGui/QTableView>
#include <QtGui/QWidget>

QT_BEGIN_NAMESPACE

class Ui_MainWindow
{
public:
    QWidget *centralWidget;
    QTableView *tableView;
    QComboBox *cbTime;
    QDateTimeEdit *dteTime;
    QRadioButton *rbCB;
    QRadioButton *rbAm;
    QPushButton *pbRefresh;

    void setupUi(QMainWindow *MainWindow)
    {
        if (MainWindow->objectName().isEmpty())
            MainWindow->setObjectName(QString::fromUtf8("MainWindow"));
        MainWindow->resize(800, 600);
        centralWidget = new QWidget(MainWindow);
        centralWidget->setObjectName(QString::fromUtf8("centralWidget"));
        tableView = new QTableView(centralWidget);
        tableView->setObjectName(QString::fromUtf8("tableView"));
        tableView->setGeometry(QRect(0, 150, 801, 451));
        cbTime = new QComboBox(centralWidget);
        cbTime->setObjectName(QString::fromUtf8("cbTime"));
        cbTime->setGeometry(QRect(140, 20, 171, 41));
        dteTime = new QDateTimeEdit(centralWidget);
        dteTime->setObjectName(QString::fromUtf8("dteTime"));
        dteTime->setGeometry(QRect(140, 90, 341, 41));
        rbCB = new QRadioButton(centralWidget);
        rbCB->setObjectName(QString::fromUtf8("rbCB"));
        rbCB->setGeometry(QRect(10, 20, 111, 41));
        rbAm = new QRadioButton(centralWidget);
        rbAm->setObjectName(QString::fromUtf8("rbAm"));
        rbAm->setGeometry(QRect(10, 90, 82, 41));
        pbRefresh = new QPushButton(centralWidget);
        pbRefresh->setObjectName(QString::fromUtf8("pbRefresh"));
        pbRefresh->setGeometry(QRect(640, 30, 80, 71));
        QIcon icon;
        icon.addFile(QString::fromUtf8(":/img/res/Refresh.png"), QSize(), QIcon::Normal, QIcon::Off);
        pbRefresh->setIcon(icon);
        pbRefresh->setIconSize(QSize(120, 120));
        MainWindow->setCentralWidget(centralWidget);

        retranslateUi(MainWindow);

        QMetaObject::connectSlotsByName(MainWindow);
    } // setupUi

    void retranslateUi(QMainWindow *MainWindow)
    {
        MainWindow->setWindowTitle(QApplication::translate("MainWindow", "MainWindow", 0, QApplication::UnicodeUTF8));
        rbCB->setText(QApplication::translate("MainWindow", "Es l\303\244uft", 0, QApplication::UnicodeUTF8));
        rbAm->setText(QApplication::translate("MainWindow", "Am", 0, QApplication::UnicodeUTF8));
        pbRefresh->setText(QString());
    } // retranslateUi

};

namespace Ui {
    class MainWindow: public Ui_MainWindow {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MAINWINDOW_H
