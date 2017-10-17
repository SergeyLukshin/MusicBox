package ru.adoon.mymusic.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;

import ru.adoon.mymusic.Classes.FileItem;
import ru.adoon.mymusic.Classes.MusicItem;

public class DB {

    private static final String DB_NAME = "my_music_db";
    private static final int DB_VERSION = 6;
    private static final String DB_TABLE = "my_music_tab";
    private static final String DB_TABLE_FILES = "my_music_tab_files";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_IMG = "img";
    public static final String COLUMN_RADIOID = "radioid";
    public static final String COLUMN_SUBFOLDER = "sub_folder";
    public static final String COLUMN_RANDOM = "random";

    public static final String COLUMN_FILE_ID = "_id";
    public static final String COLUMN_FILE_OBJECT_ID = "objectid";
    public static final String COLUMN_FILE_NAME = "name";


    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " text, " +
                    COLUMN_URL + " text, " +
                    COLUMN_SUBFOLDER + " integer, " +
                    COLUMN_RANDOM + " integer, " +
                    COLUMN_TYPE + " integer, " +
                    COLUMN_IMG + " integer, " +
                    COLUMN_RADIOID + " integer" +
                    ");";

    private static final String DB_CREATE2 =
            "create table " + DB_TABLE_FILES + "(" +
                    COLUMN_FILE_ID + " integer primary key autoincrement, " +
                    COLUMN_FILE_NAME + " text, " +
                    COLUMN_FILE_OBJECT_ID + " integer" +
                    ");";

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper != null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
        return mDB.query(DB_TABLE, null, null, null, null, null, null);
    }

    public ArrayList<MusicItem> getData() {
        Cursor c = null;
        ArrayList<MusicItem> vec = new ArrayList<MusicItem>();
        c = mDB.query(DB_TABLE, null, null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    int id = c.getInt(c.getColumnIndex(DB.COLUMN_ID));
                    String strName = c.getString(c.getColumnIndex(DB.COLUMN_NAME));
                    String strURL = c.getString(c.getColumnIndex(DB.COLUMN_URL));
                    int iType = c.getInt(c.getColumnIndex(DB.COLUMN_TYPE));
                    int iRadioID= c.getInt(c.getColumnIndex(DB.COLUMN_RADIOID));
                    int iImage = c.getInt(c.getColumnIndex(DB.COLUMN_IMG));

                    int bSubFolder = c.getInt(c.getColumnIndex(DB.COLUMN_SUBFOLDER));
                    int bRandom = c.getInt(c.getColumnIndex(DB.COLUMN_RANDOM));

                    vec.add(new MusicItem(mCtx, id, strName, strURL, bSubFolder == 0 ? false : true, bRandom == 0 ? false : true, iType, iRadioID, iImage));

                    Cursor c2 = null;
                    c2 = mDB.query(DB_TABLE_FILES, null, COLUMN_FILE_OBJECT_ID + " = ?",
                            new String[] { String.valueOf(id) }, null, null, null);
                    if (c2 != null) {

                        if (c2.getCount() > 0) {
                            vec.get(vec.size() - 1).files = new ArrayList<FileItem>();

                            if (c2.moveToFirst()) {
                                do {
                                    String strFileName = c2.getString(c2.getColumnIndex(DB.COLUMN_FILE_NAME));
                                    vec.get(vec.size() - 1).files.add(new FileItem(strFileName));
                                }
                                while (c2.moveToNext());
                            }
                        }
                    }
                    c2.close();
                } while (c.moveToNext());
            }
            c.close();
        }
        return vec;
    }

    // добавить запись в DB_TABLE
    public void addRec(String name, String url, boolean bSubFolder, boolean bRandom, int type, int radioid, int img) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_URL, url);
        cv.put(COLUMN_TYPE, type);
        cv.put(COLUMN_IMG, img);
        cv.put(COLUMN_RADIOID, radioid);
        cv.put(COLUMN_SUBFOLDER, bSubFolder ? 1 : 0);
        cv.put(COLUMN_RANDOM, bRandom ? 1 : 0);
        mDB.insert(DB_TABLE, null, cv);
    }

    public void addRec(MusicItem mi) {

        mDB.beginTransaction();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, mi.name);
        cv.put(COLUMN_URL, mi.url);
        cv.put(COLUMN_TYPE, mi.type);
        cv.put(COLUMN_IMG, mi.image);
        cv.put(COLUMN_RADIOID, mi.radioid);
        cv.put(COLUMN_SUBFOLDER, mi.bSubFolder ? 1 : 0);
        cv.put(COLUMN_RANDOM, mi.bRandom ? 1 : 0);
        long id = mDB.insert(DB_TABLE, null, cv);

        if (mi.files != null) {
            for (int i = 0; i < mi.files.size(); i++) {
                ContentValues cv_file = new ContentValues();
                cv_file.put(COLUMN_FILE_OBJECT_ID, id);
                cv_file.put(COLUMN_FILE_NAME, mi.files.get(i).m_strName);
                mDB.insert(DB_TABLE_FILES, null, cv_file);
            }
        }
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    public void updRec(MusicItem mi) {

        mDB.beginTransaction();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, mi.name);
        cv.put(COLUMN_URL, mi.url);
        cv.put(COLUMN_TYPE, mi.type);
        cv.put(COLUMN_IMG, mi.image);
        cv.put(COLUMN_RADIOID, mi.radioid);
        cv.put(COLUMN_SUBFOLDER, mi.bSubFolder ? 1 : 0);
        cv.put(COLUMN_RANDOM, mi.bRandom ? 1 : 0);
        mDB.update(DB_TABLE, cv, COLUMN_ID + " = ?",
                new String[] { String.valueOf(mi.id) });

        mDB.delete(DB_TABLE_FILES, COLUMN_FILE_OBJECT_ID + " = " + mi.id, null);

        if (mi.files != null) {
            for (int i = 0; i < mi.files.size(); i++) {
                ContentValues cv_file = new ContentValues();
                cv_file.put(COLUMN_FILE_OBJECT_ID, mi.id);
                cv_file.put(COLUMN_FILE_NAME, mi.files.get(i).m_strName);
                mDB.insert(DB_TABLE_FILES, null, cv_file);
            }
        }
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    // удалить запись из DB_TABLE
    public void delRec(long id) {

        mDB.beginTransaction();
        mDB.delete(DB_TABLE, COLUMN_ID + " = " + id, null);
        mDB.delete(DB_TABLE_FILES, COLUMN_FILE_OBJECT_ID + " = " + id, null);
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
            db.execSQL(DB_CREATE2);

            /*ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, "LikeFM");
            cv.put(COLUMN_URL, "http://ic3.101.ru:8000/v12_1");
            cv.put(COLUMN_TYPE, 0);
            cv.put(COLUMN_IMG, 0);
            db.insert(DB_TABLE, null, cv);

            cv.put(COLUMN_NAME, "NRG");
            cv.put(COLUMN_URL, "http://ic3.101.ru:8000/v1_1?setst=-1");
            cv.put(COLUMN_TYPE, 0);
            cv.put(COLUMN_IMG, 0);
            db.insert(DB_TABLE, null, cv);*/
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 6 && newVersion >= 6) {
                db.execSQL(DB_CREATE2);

                return;
            }
        }
    }
}
