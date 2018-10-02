package ng.neas.com.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import app.qtax.com.adapter.FeedbackAdapter
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ng.neas.com.App

import ng.neas.com.R
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.FeedBackEntity
import ng.neas.com.utils.MessageStatus

class ManageFeedbackFragment : Fragment(), ValueEventListener, OnCompleteListener<Void> {
    override fun onComplete(p0: Task<Void>) {

    }

    override fun onCancelled(p0: DatabaseError) {
        handler?.sendEmptyMessage(1)
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        handler?.sendEmptyMessage(1)
        userList = ArrayList()
        for ( data in dataSnapshot.children){
            val feed = data.getValue(FeedBackEntity::class.java)
            userList?.add(feed!!)
            if(feed?.status == MessageStatus.UNREAD)
                updateRead(feed)
        }
        adapter?.swapList(userList)
    }

    private fun updateRead(feed: FeedBackEntity) {
        feed.status = MessageStatus.READ
        helper?.submitToDB(getString(R.string.feedback_entity), feed.ref, feed, this)
    }

    private var recyclerView: RecyclerView? = null
    private var adapter: FeedbackAdapter? = null
    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? = null
    private var userList: ArrayList<FeedBackEntity>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = MyHandler(context, true)
        helper = MyFireBaseHelper(context)
        handler?.sendEmptyMessage(0)
        helper?.fetchDB(getString(R.string.feedback_entity), this)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_feedback, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = FeedbackAdapter(context)
        recyclerView?.adapter = adapter
        recyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        adapter?.swapList(App.feedBackList)
        return view
    }
}

