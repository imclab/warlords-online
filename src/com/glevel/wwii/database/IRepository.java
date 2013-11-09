package com.glevel.wwii.database;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

public interface IRepository<T> {

    public List<T> get(String selection, String[] selectionArgs, String orderBy, String limit);

    public T getById(long id);

    public long save(T entity);

    public void delete(String selection, String[] selectionArgs);

    public List<T> convertCursorToObjectList(Cursor c);

    public T convertCursorToSingleObject(Cursor c);

    public T convertCursorRowToObject(Cursor c);

    public ContentValues getContentValues(T entity);

}
