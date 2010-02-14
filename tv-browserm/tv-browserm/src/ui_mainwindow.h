/********************************************************************************
** Form generated from reading ui file 'mainwindow.ui'
**
** Created: Sun 14. Feb 01:47:58 2010
**      by: Qt User Interface Compiler version 4.5.3
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
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
    QPushButton *pbNext;
    QPushButton *pbLast;

    void setupUi(QMainWindow *MainWindow)
    {
        if (MainWindow->objectName().isEmpty())
            MainWindow->setObjectName(QString::fromUtf8("MainWindow"));
        MainWindow->resize(800, 420);
        centralWidget = new QWidget(MainWindow);
        centralWidget->setObjectName(QString::fromUtf8("centralWidget"));
        tableView = new QTableView(centralWidget);
        tableView->setObjectName(QString::fromUtf8("tableView"));
        tableView->setGeometry(QRect(0, 170, 800, 251));
        cbTime = new QComboBox(centralWidget);
        cbTime->setObjectName(QString::fromUtf8("cbTime"));
        cbTime->setGeometry(QRect(140, 20, 171, 41));
        dteTime = new QDateTimeEdit(centralWidget);
        dteTime->setObjectName(QString::fromUtf8("dteTime"));
        dteTime->setGeometry(QRect(140, 90, 291, 41));
        rbCB = new QRadioButton(centralWidget);
        rbCB->setObjectName(QString::fromUtf8("rbCB"));
        rbCB->setGeometry(QRect(10, 20, 111, 41));
        rbAm = new QRadioButton(centralWidget);
        rbAm->setObjectName(QString::fromUtf8("rbAm"));
        rbAm->setGeometry(QRect(10, 90, 82, 41));
        pbRefresh = new QPushButton(centralWidget);
        pbRefresh->setObjectName(QString::fromUtf8("pbRefresh"));
        pbRefresh->setGeometry(QRect(460, 70, 80, 71));
        QIcon icon;
        icon.addFile(QString::fromUtf8(":/img/res/refresh.png"), QSize(), QIcon::Normal, QIcon::Off);
        pbRefresh->setIcon(icon);
        pbRefresh->setIconSize(QSize(120, 120));
        pbNext = new QPushButton(centralWidget);
        pbNext->setObjectName(QString::fromUtf8("pbNext"));
        pbNext->setGeometry(QRect(700, 70, 80, 71));
        QIcon icon1;
        icon1.addFile(QString::fromUtf8(":/img/res/forward_alt.png"), QSize(), QIcon::Normal, QIcon::Off);
        pbNext->setIcon(icon1);
        pbNext->setIconSize(QSize(120, 120));
        pbLast = new QPushButton(centralWidget);
        pbLast->setObjectName(QString::fromUtf8("pbLast"));
        pbLast->setGeometry(QRect(600, 70, 80, 71));
        QIcon icon2;
        icon2.addFile(QString::fromUtf8(":/img/res/back_alt.png"), QSize(), QIcon::Normal, QIcon::Off);
        pbLast->setIcon(icon2);
        pbLast->setIconSize(QSize(120, 120));
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
        pbNext->setText(QString());
        pbLast->setText(QString());
        Q_UNUSED(MainWindow);
    } // retranslateUi

};

namespace Ui {
    class MainWindow: public Ui_MainWindow {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MAINWINDOW_H
