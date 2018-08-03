package ng.neas.com.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import com.google.android.gms.tasks.OnCompleteListener
import com.google.gson.Gson
import ng.neas.com.R
import ng.neas.com.data.MyContentProvider
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.AlertEntity
import ng.neas.com.model.UserEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Repository {
    companion object {
        private val sdf = SimpleDateFormat("dd - MMM, yyyy hh:mm:s a")
        fun addAlert(activity: Activity, alert:AlertEntity?, helper: MyFireBaseHelper?, listener: OnCompleteListener<Void>){
            val values = ContentValues().apply {
                put(MyContentProvider.KEY_CONTENT, alert?.detail)
                put(MyContentProvider.KEY_DATE_PUBLISHED, alert?.publishedDate)
                put(MyContentProvider.KEY_REF, alert?.ref)
                put(MyContentProvider.KEY_TITLE, alert?.title)
                put(MyContentProvider.KEY_FACULTY, alert?.faculty)
                put(MyContentProvider.KEY_DEPARTMENT, alert?.department)
                put(MyContentProvider.KEY_USER, Gson().toJson(alert?.user))
            }
            activity.contentResolver.insert(MyContentProvider.CONTENT_URI, values)
            helper?.submitToDB(activity.getString(R.string.alert_entity), alert?.ref, alert, listener)
        }
        fun getDate(): String? {
            return sdf.format(Date())
        }
        fun submitTODB(contentResolver: ContentResolver, rawData: ArrayList<AlertEntity>?) {
            try {
                contentResolver.delete(MyContentProvider.CONTENT_URI, null, null)
                val values = rawData?.map { it -> ContentValues().apply {
                    put(MyContentProvider.KEY_CONTENT, it.detail)
                    put(MyContentProvider.KEY_DATE_PUBLISHED, it.publishedDate)
                    put(MyContentProvider.KEY_REF, it.ref)
                    put(MyContentProvider.KEY_TITLE, it.title)
                    put(MyContentProvider.KEY_FACULTY, it.faculty)
                    put(MyContentProvider.KEY_DEPARTMENT, it.department)
                    put(MyContentProvider.KEY_USER, Gson().toJson(it.user))
                } }
                contentResolver.bulkInsert(MyContentProvider.CONTENT_URI,values?.toTypedArray())
            }catch (_: Exception){}
        }
        fun fetchLocal(contentResolver: ContentResolver): ArrayList<AlertEntity>? {
            var data = ArrayList<AlertEntity>()
            val cursor = contentResolver.query(MyContentProvider.CONTENT_URI, null, null, null, null)
            while (cursor?.moveToNext() == true){
                val alert = AlertEntity().apply {
                    this.title = cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_TITLE))
                    this.faculty = cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_FACULTY))
                    this.department = cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_DEPARTMENT))
                    this.detail = cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_CONTENT))
                    this.publishedDate = cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_DATE_PUBLISHED))
                    this.ref = cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_REF))
                    this.user = Gson().fromJson(cursor.getString(cursor.getColumnIndexOrThrow(MyContentProvider.KEY_USER)), UserEntity::class.java)
                }
                data.add(alert)
            }
            return data
        }
    }
}