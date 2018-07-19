package ng.neas.com.data

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class MyContentProvider : ContentProvider() {
    companion object {
        val DATABASE_VERSION = 1
        val KEY_ID = "_ID"
        val KEY_TITLE = "title"
        val KEY_DATE_PUBLISHED = "date_published"
        val KEY_CONTENT = "content"
        val KEY_STATE = "state"
        val KEY_USER = "user"
        val KEY_FACULTY = "faculty"
        val KEY_DEPARTMENT = "department"
        val KEY_REF = "ref"


        val CONTENT_URI = Uri.parse("content://ng.neas.com/alert")

    }

    private var myOpenHelper: MySQLiteOpenHelper? = null
    private val ALERT: Int = 3

    private var uriMatcher: UriMatcher? = null
    init {
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher?.addURI("ng.neas.com", "alert", ALERT)

    }




    override fun delete(uri:Uri, selection:String?, selectionArgs:Array<String>?):Int {
        val db = myOpenHelper?.writableDatabase
        var deleteCount : Int? = -1
        when (uriMatcher?.match(uri)) {
            ALERT ->   deleteCount = db?.delete(MySQLiteOpenHelper.ALERT_TABLE, selection, selectionArgs)

        }
        context!!.contentResolver.notifyChange(uri, null)
        return deleteCount ?: -1
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher?.match(uri)) {
            ALERT -> "vnd.android.cursor.dir/ng.neas.com.alert"

            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = myOpenHelper?.writableDatabase
        val id = when(uriMatcher?.match(uri)){
            ALERT -> db?.insert(MySQLiteOpenHelper.ALERT_TABLE, null,  values)

            else -> -1
        }
        return if (id!! > -1) {
            val insertedId = ContentUris.withAppendedId(CONTENT_URI, id)
            context!!.contentResolver.notifyChange(insertedId, null)
            insertedId
        } else
            null
    }
    override fun bulkInsert(uri: Uri?, values: Array<out ContentValues>?): Int {
        val db = myOpenHelper?.writableDatabase
        var rowsInserted = 0
        db?.beginTransaction()
        try {
            for (content in values!!){
                var inserted = when(uriMatcher?.match(uri)){
                    ALERT -> db?.insert(MySQLiteOpenHelper.ALERT_TABLE, null, content)

                    else -> -1
                }
                if(inserted ?: 0 > 0)
                    rowsInserted++
            }
            db?.setTransactionSuccessful()
        }finally {
            db?.endTransaction()
        }
        if(rowsInserted > 0)
            context!!.contentResolver.notifyChange(uri, null)
        return rowsInserted
    }
    override fun onCreate(): Boolean {
        myOpenHelper = MySQLiteOpenHelper(context, MySQLiteOpenHelper.DATABASE_NAME, null, DATABASE_VERSION)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val db = myOpenHelper?.writableDatabase
        return when (uriMatcher?.match(uri)) {
            ALERT -> db?.query(MySQLiteOpenHelper.ALERT_TABLE,null,selection, selectionArgs,null,null, null)

            else -> null
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val db = myOpenHelper?.writableDatabase
        val updateCount = when (uriMatcher?.match(uri)) {
            ALERT -> db?.update(MySQLiteOpenHelper.ALERT_TABLE, values, selection, selectionArgs)
            else -> -1
        }
        context!!.contentResolver.notifyChange(uri, null)
        return updateCount!!
    }
    private class MySQLiteOpenHelper(context: Context, name: String,  factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {


        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DATABASE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("drop table if exists $ALERT_TABLE")
            db.execSQL(CREATE_DATABASE)

        }

        companion object {

            val DATABASE_NAME = "NEAS.db"
            val ALERT_TABLE = "alert_entity"

            val CREATE_DATABASE = ("create table if not exists "
                    + ALERT_TABLE + " ("
                    + KEY_ID + " integer primary key autoincrement, "
                    + KEY_TITLE + " text  not null, "
                    + KEY_CONTENT + " text  not null, "
                    + KEY_DATE_PUBLISHED + " text  not null, "
                    + KEY_STATE + " text  , "
                    + KEY_USER + " text, "
                    + KEY_FACULTY + " text, "
                    + KEY_DEPARTMENT + " text, "
                    + KEY_REF + " text, "
                    + " UNIQUE ( $KEY_REF ) ON CONFLICT REPLACE);")




        }

    }
}
