package com.glevel.wwii.database;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class Repository<T> implements IRepository<T> {

    protected SQLiteDatabase mDatabase;

    protected DatabaseHelper dataBaseHelper;

    public Repository(DatabaseHelper dataBaseHelper) {
        this.dataBaseHelper = dataBaseHelper;
    }

    public void openDatabase() {
        mDatabase = dataBaseHelper.getWritableDatabase();
    }

    public void closeDatabase() {
        mDatabase.close();
    }

    public T convertCursorToSingleObject(Cursor c) {
        T entity = null;
        if (c.moveToFirst()) {
            entity = convertCursorRowToObject(c);
        }
        c.close();
        closeDatabase();
        return entity;
    }

    public List<T> convertCursorToObjectList(Cursor c) {
        List<T> list = new ArrayList<T>();
        if (c.getCount() == 0) {
            return list;
        }
        while (c.moveToNext()) {
            T entity = convertCursorRowToObject(c);
            list.add(entity);
        }
        c.close();
        closeDatabase();
        return list;
    }

}
