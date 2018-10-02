package ng.neas.com.data

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class MyContentProvider : ContentProvider() {
    companion object {
        const val DATABASE_VERSION = 7
        const val KEY_ID = "_ID"
        const val KEY_TITLE = "title"
        const val KEY_DATE_PUBLISHED = "date_published"
        const val KEY_CONTENT = "content"
        const val KEY_STATE = "state"
        const val KEY_USER = "user"
        const val KEY_FACULTY = "faculty"
        const val KEY_DEPARTMENT = "department"
        const val KEY_REF = "ref"
        const val KEY_MESSAGE_STATUS = "read_status"
        const val KEY_APPROVAL_STATUS = "approval_status"
        const val KEY_TYPE = "type"


        val CONTENT_URI = Uri.parse("content://ng.neas.com/alert")
        val CONTENT_URI_NEWS = Uri.parse("content://ng.neas.com/news")

    }

    private var myOpenHelper: MySQLiteOpenHelper? = null
    private val ALERT: Int = 4
    private val NEWS: Int = 5

    private var uriMatcher: UriMatcher? = null
    init {
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher?.addURI("ng.neas.com", "alert", ALERT)
        uriMatcher?.addURI("ng.neas.com", "news", NEWS)

    }




    override fun delete(uri:Uri, selection:String?, selectionArgs:Array<String>?):Int {
        val db = myOpenHelper?.writableDatabase
        var deleteCount : Int? = -1
        when (uriMatcher?.match(uri)) {
            ALERT ->   deleteCount = db?.delete(MySQLiteOpenHelper.ALERT_TABLE, selection, selectionArgs)
            NEWS ->   deleteCount = db?.delete(MySQLiteOpenHelper.NEWS_TABLE, selection, selectionArgs)
        }
        context!!.contentResolver.notifyChange(uri, null)
        return deleteCount ?: -1
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher?.match(uri)) {
            ALERT -> "vnd.android.cursor.dir/ng.neas.com.alert"
            NEWS -> "vnd.android.cursor.dir/ng.neas.com.news"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = myOpenHelper?.writableDatabase
        val id = when(uriMatcher?.match(uri)){
            ALERT -> db?.insert(MySQLiteOpenHelper.ALERT_TABLE, null,  values)
            NEWS -> db?.insert(MySQLiteOpenHelper.NEWS_TABLE, null,  values)
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
                    NEWS -> db?.insert(MySQLiteOpenHelper.NEWS_TABLE, null, content)
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
            NEWS -> db?.query(MySQLiteOpenHelper.NEWS_TABLE,null,selection, selectionArgs,null,null, null)
            else -> null
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val db = myOpenHelper?.writableDatabase
        val updateCount = when (uriMatcher?.match(uri)) {
            ALERT -> db?.update(MySQLiteOpenHelper.ALERT_TABLE, values, selection, selectionArgs)
            NEWS -> db?.update(MySQLiteOpenHelper.NEWS_TABLE, values, selection, selectionArgs)
            else -> -1
        }
        context!!.contentResolver.notifyChange(uri, null)
        return updateCount!!
    }
    private class MySQLiteOpenHelper(context: Context, name: String,  factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {


        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DATABASE)
            db.execSQL(CREATE_DATABASE_NEWS)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("drop table if exists $ALERT_TABLE")
            db.execSQL("drop table if exists $NEWS_TABLE")
            db.execSQL(CREATE_DATABASE)
            db.execSQL(CREATE_DATABASE_NEWS)

        }

        companion object {

            const val DATABASE_NAME = "NEAS.db"
            const val ALERT_TABLE = "alert_entity"
            const val NEWS_TABLE = "news_entity"

            const val CREATE_DATABASE = ("create table if not exists "
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
                    + KEY_MESSAGE_STATUS + " text, "
                    + KEY_APPROVAL_STATUS + " text, "
                    + " unique( $KEY_REF) );")

            const val CREATE_DATABASE_NEWS = ("create table if not exists "
                    + NEWS_TABLE + " ("
                    + KEY_ID + " integer primary key autoincrement, "
                    + KEY_TITLE + " text  not null, "
                    + KEY_CONTENT + " text  not null, "
                    + KEY_DATE_PUBLISHED + " text  not null, "
                    + KEY_REF + " text, "
                    + KEY_MESSAGE_STATUS + " text, "
                    + KEY_TYPE + " text, "
                    + " unique( $KEY_REF) );")

        }

    }
}
