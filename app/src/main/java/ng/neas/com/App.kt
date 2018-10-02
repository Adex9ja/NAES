package ng.neas.com

import android.app.Application
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ng.neas.com.data.MyContentProvider
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.model.AlertEntity
import ng.neas.com.model.FeedBackEntity
import ng.neas.com.model.NewsEntity
import ng.neas.com.utils.MessageStatus
import ng.neas.com.utils.Repository

class App: Application() {


    private var alertListener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            alertList = ArrayList()
            dataSnapshot.children.let { it.forEach {
                val chat = it.getValue(AlertEntity::class.java)
//                if(pref?.isAlertLoaded != MyContentProvider.DATABASE_VERSION)
                chat?.messageStatus = MessageStatus.UNREAD
                alertList?.add(chat!!)
            }}
            pref?.isAlertLoaded = MyContentProvider.DATABASE_VERSION
            Repository.submitTODB(applicationContext.contentResolver,alertList)
            alertList?.forEach { Repository.updateApprovalStatus(contentResolver, it) }
            alertList = Repository.fetchAlerts(contentResolver)
        }

    }
    private var feedBackListener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            feedBackList = ArrayList()
            dataSnapshot.children.let { it.forEach {
                val chat = it.getValue(FeedBackEntity::class.java)
                feedBackList?.add(chat!!)
            }}
        }

    }
    private var newsListener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            newsList = ArrayList()
            dataSnapshot.children.let { it.forEach {
                val chat = it.getValue(NewsEntity::class.java)
                newsList?.add(chat!!)
            }}
            Repository.saveNews(newsList, contentResolver)
            newsList = Repository.loadNews(contentResolver)
        }

    }
    private var helper: MyFireBaseHelper? = null
    private var pref: MySharedPreference? = null
    override fun onCreate() {
        super.onCreate()
        helper = MyFireBaseHelper(this)
        pref = MySharedPreference(this)
        alertList = Repository.fetchAlerts(contentResolver)
        newsList = Repository.loadNews(contentResolver)
        helper?.fetchDB(getString(R.string.alert_entity), alertListener)
        helper?.fetchDB(getString(R.string.feedback_entity),feedBackListener)
        helper?.fetchDB(getString(R.string.news_entity),newsListener)
    }
    companion object {
        var alertList : ArrayList<AlertEntity>? = ArrayList()
        var feedBackList : ArrayList<FeedBackEntity>? = ArrayList()
        var newsList : ArrayList<NewsEntity>? = ArrayList()
    }
}