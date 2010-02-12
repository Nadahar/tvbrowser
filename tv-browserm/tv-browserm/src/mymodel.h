#ifndef MYMODEL_H
#define MYMODEL_H

#include "mymodel.h"
#include <QAbstractTableModel>
#include <QPair>
#include <QList>


template <class T1, class T2, class T3, class T4>
struct QPair4
{
    typedef T1 first_type;
    typedef T2 second_type;
    typedef T3 third_type;
    typedef T4 fourth_type;

    QPair4() : first(T1()), second(T2()), third(T3()), fourth(T4()) {}
    QPair4(const T1 &t1, const T2 &t2, const T3 &t3, const T4 &t4) : first(t1), second(t2), third(t3), fourth(t4) {}

    QPair4<T1, T2, T3, T4> &operator=(const QPair4<T1, T2, T3, T4> &other)
    { first = other.first; second = other.second; third = other.third; fourth = other.fourth; return *this; }

    T1 first;
    T2 second;
    T3 third;
    T4 fourth;
};


//! [0]
class MyModel : public QAbstractTableModel
{
    Q_OBJECT

public:
    MyModel(QObject *parent=0);
    MyModel(QList< QPair4<QString, QString, QString, QString> > listofPairs, QObject *parent=0);

    int rowCount(const QModelIndex &parent) const;
    int columnCount(const QModelIndex &parent) const;
    QVariant data(const QModelIndex &index, int role) const;
    QVariant headerData(int section, Qt::Orientation orientation, int role) const;
    Qt::ItemFlags flags(const QModelIndex &index) const;
    bool setData(const QModelIndex &index, const QVariant &value, int role=Qt::EditRole);
    bool insertRows(int position, int rows, const QModelIndex &index=QModelIndex());
    bool removeRows(int position, int rows, const QModelIndex &index=QModelIndex());
    QList< QPair4<QString, QString, QString, QString> > getList();
    QString getData(int row, int col) const;

private:
    QList< QPair4<QString, QString, QString, QString> > listOfPairs;
};
//! [0]

#endif
