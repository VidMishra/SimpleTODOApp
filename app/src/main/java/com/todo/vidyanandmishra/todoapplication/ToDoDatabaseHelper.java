package com.todo.vidyanandmishra.todoapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;

/**
 * Created by vidyanandmishra on 02/11/16.
 */

public class ToDoDatabaseHelper extends SQLiteOpenHelper {

    private static final String NAME = "todo.db";

    private static final int VERSION = 1;

    public static final String TBL_TODO = "ToDO";
    public static final String COL_ID = BaseColumns._ID;
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";

    public static final String CREATE_TBL_TODO = "CREATE TABLE " + TBL_TODO + "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_TITLE + " TEXT," + COL_DESCRIPTION + " TEXT"+")";

    String DROP_TBL_TODO = "DROP TABLE IF EXISTS "+TBL_TODO;

    public ToDoDatabaseHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TBL_TODO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_TBL_TODO);
        onCreate(sqLiteDatabase);
    }

    public long insertToDos(ContentValues values) {

        long id = getWritableDatabase().insert(TBL_TODO, "", values);

        return id;
    }

    public ArrayList<ToDoModel> getToDos() {

        Cursor cursor = getReadableDatabase().query(TBL_TODO, new String[] {COL_ID, COL_TITLE, COL_DESCRIPTION}, null, null, null, null, null);

        ArrayList<ToDoModel> listToDos = new ArrayList<>();

        try {
            if (cursor != null && cursor.moveToFirst()) {

                do {
                    ToDoModel toDoModel = new ToDoModel();

                    toDoModel.id = cursor.getString(cursor.getColumnIndex(COL_ID));
                    toDoModel.title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                    toDoModel.description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));

                    listToDos.add(toDoModel);
                } while (cursor.moveToNext());
            }
        }finally {
            if(cursor != null)
                cursor.close();
        }

        return listToDos;
    }

    public boolean deleteToDo(String id) {

        if(id == null) return false;

        int rowDeleted = getWritableDatabase().delete(TBL_TODO, "_id=?", new String[]{id});

        return rowDeleted > 0;
    }

    public boolean updateToDoDescription(String id, ContentValues values) {

        if(id == null) return false;

        int rowUpdated = getWritableDatabase().update(TBL_TODO, values, "_id=?", new String[]{id});

        return rowUpdated > 0;
    }
}
