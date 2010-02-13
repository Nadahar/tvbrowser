#include "runsnowmodel.h"


runsnowmodel::runsnowmodel(QObject *parent)
    : QAbstractTableModel(parent)
{
}

runsnowmodel::runsnowmodel(QList< QPair4<QString, QString, QString, QString> > pairs, QObject *parent)
    : QAbstractTableModel(parent)
{    
    listOfPairs=pairs;
}

int runsnowmodel::rowCount(const QModelIndex &parent) const
{
    Q_UNUSED(parent);
    return listOfPairs.size();
}

int runsnowmodel::columnCount(const QModelIndex &parent) const
{
    Q_UNUSED(parent);
    return 4;
}

QVariant runsnowmodel::data(const QModelIndex &index, int role) const
{
    if (!index.isValid())
        return QVariant();

    if (index.row() >= listOfPairs.size() || index.row() < 0)
        return QVariant();

    if (role == Qt::DisplayRole) {
        QPair4<QString, QString, QString, QString> pair = listOfPairs.at(index.row());

        if (index.column() == 0)
            return pair.first;
        else if (index.column() == 1)
            return pair.second;
        else if (index.column() == 2)
            return pair.third;
        else if (index.column() == 3)
            return pair.fourth;
    }
    return QVariant();
}

QString runsnowmodel::getData(int row, int col) const
{
    if (row >= listOfPairs.size() || row < 0)
        return "";


        QPair4<QString, QString, QString, QString> pair = listOfPairs.at(row);

        if (col == 0)
            return pair.first;
        else if (col == 1)
            return pair.second;
        else if (col == 2)
            return pair.third;
        else if (col == 3)
            return pair.fourth;
        else return "";
}

QVariant runsnowmodel::headerData(int section, Qt::Orientation orientation, int role) const
{
    if (role != Qt::DisplayRole)
        return QVariant();

    if (orientation == Qt::Horizontal) {
        switch (section) {
            case 0:
                return tr("Channel");
            case 1:
                return tr("broadcast");
            case 2:
                return tr("begin - end");
            case 3:
                return tr("");
            default:
                return QVariant();
        }
    }
    return QVariant();
}

bool runsnowmodel::insertRows(int position, int rows, const QModelIndex &index)
{
    Q_UNUSED(index);
    beginInsertRows(QModelIndex(), position, position+rows-1);

    for (int row=0; row < rows; row++) {
        QPair4<QString, QString, QString, QString> pair(" ", " ", " ", " ");
        listOfPairs.insert(position, pair);
    }

    endInsertRows();
    return true;
}

bool runsnowmodel::removeRows(int position, int rows, const QModelIndex &index)
{
    Q_UNUSED(index);
    beginRemoveRows(QModelIndex(), position, position+rows-1);

    for (int row=0; row < rows; ++row) {
        listOfPairs.removeAt(position);
    }

    endRemoveRows();
    return true;
}

bool runsnowmodel::setData(const QModelIndex &index, const QVariant &value, int role)
{
        if (index.isValid() && role == Qt::EditRole) {
                int row = index.row();

                QPair4<QString, QString, QString, QString> p = listOfPairs.value(row);

                if (index.column() == 0)
                        p.first = value.toString();
                else if (index.column() == 1)
                        p.second = value.toString();
                else if (index.column() == 2)
                        p.third = value.toString();
                else if (index.column() == 3)
                        p.fourth = value.toString();
        else
            return false;

        listOfPairs.replace(row, p);
                emit(dataChanged(index, index));

        return true;
        }

        return false;
}

Qt::ItemFlags runsnowmodel::flags(const QModelIndex &index) const
{
    if (!index.isValid())
        return Qt::ItemIsEnabled;

    return QAbstractTableModel::flags(index) | Qt::ItemIsEditable;
}

QList< QPair4<QString, QString, QString, QString> > runsnowmodel::getList()
{
    return listOfPairs;
}
