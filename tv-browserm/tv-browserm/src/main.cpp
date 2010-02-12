#include <QtGui/QApplication>
#include <QTranslator>
#include <QLocale>
#include <QDir>
#include "mainwindow.h"

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    QString locale = QLocale::system().name();


    QString sAppDir = QApplication::applicationDirPath();
    
    QTranslator translator;
    QString sTemp = sAppDir + "/tv-browserm_" + locale + ".qm";

    QString sFileName  = "tv-browserm_" + locale + ".qm";

    translator.load(sFileName,sAppDir + "/");
    a.installTranslator(&translator);

    MainWindow w;
    w.show();
    return a.exec();
}
