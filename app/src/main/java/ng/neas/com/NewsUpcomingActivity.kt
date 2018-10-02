package ng.neas.com

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import app.qtax.com.adapter.NewsListAdater
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_news_upcoming_acitvity.*
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.NewsEntity
import ng.neas.com.utils.Repository

class NewsUpcomingActivity : AppCompatActivity(), ValueEventListener {
    override fun onCancelled(p0: DatabaseError) {
        progress.visibility = View.GONE
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        progress.visibility = View.GONE
        newsList = ArrayList()
        dataSnapshot.children.forEach { newsList?.add(it.getValue(NewsEntity::class.java)!!) }
        Repository.saveNews(newsList, contentResolver)
        newsList = Repository.loadNews(contentResolver)
        adapter?.swapList(newsList)
    }

    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? =  null
    private var adapter: NewsListAdater? = null
    private var newsList: ArrayList<NewsEntity>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_upcoming_acitvity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        adapter = NewsListAdater(this)
        recyclerView.adapter = adapter
        handler = MyHandler(this, false)
        helper = MyFireBaseHelper(this)
        helper?.fetchDB(getString(R.string.news_entity), this)
        newsList = Repository.loadNews(contentResolver)
        adapter?.swapList(newsList)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
