#-------------------------------------------------
#
# Project created by QtCreator 2010-02-10T19:51:20
#
#-------------------------------------------------

TARGET = tv-browserm
TEMPLATE = app
INCLUDEPATH += sqlite_source

TRANSLATIONS = tv-browserm_en_EN.ts \
               tv-browserm_de_DE.ts

SOURCES += main.cpp \
    mainwindow.cpp \
    sqlite_source/sqlite3.c \
    runsnowmodel.cpp
HEADERS += mainwindow.h \
    sqlite_source/sqlite3.h \
    runsnowmodel.h

FORMS    += mainwindow.ui
RESOURCES += main.qrc



#CONFIG = qt resources warn_on debug

unix {
    # VARIABLES
    isEmpty(PREFIX):PREFIX = /opt
    BINDIR = /opt/tv-browserm
    DATADIR = /usr/share
    DEFINES += DATADIR=\"$$DATADIR\" \
                PKGDATADIR=\"$$PKGDATADIR\"

    # MAKE INSTALL
    INSTALLS += target \
        desktop \
        pic \
        langDE \
        LangEN /
    target.path = $$BINDIR
    desktop.path = $$DATADIR/applications/hildon
    desktop.files += tv-browserm.desktop
    pic.path = $$DATADIR/pixmaps
    pic.files += res/tvbrowser64.png
    langDE.path = $$BINDIR
    langDE.files += tv-browserm_de_DE.qm
    LangEN.path = $$BINDIR
    LangEN.files += tv-browserm_en_EN.qm
}

