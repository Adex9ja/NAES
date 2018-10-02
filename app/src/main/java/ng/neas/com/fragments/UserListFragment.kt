package ng.neas.com.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import app.qtax.com.adapter.AlertEntryAdapter
import app.qtax.com.adapter.UserListAdapter
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ng.neas.com.App

import ng.neas.com.R
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.AlertEntity
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.ApprovalStatus


class UserListFragment : Fragment(), ValueEventListener, TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        userList?.filter {
                    it.fullName?.contains(s!!, true) == true ||
                    it.studentId?.contains(s!!, true) == true ||
                    it.matricNo?.contains(s!!, true) == true ||
                    it.phoneNo?.contains(s!!, true) == true }?.let { adapter?.swapList(ArrayList(it)) }
    }

    override fun onCancelled(p0: DatabaseError) {
        handler?.sendEmptyMessage(1)
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        handler?.sendEmptyMessage(1)
        userList = ArrayList()
        dataSnapshot.children.forEach {  userList?.add(it.getValue(UserEntity::class.java)!!)  }
        adapter?.swapList(userList)
    }

    private var recyclerView: RecyclerView? = null
    private var txtSearch: EditText? = null
    private var adapter: UserListAdapter? = null
    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? = null
    private var userList: ArrayList<UserEntity>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = MyHandler(context, true)
        helper = MyFireBaseHelper(context)
        handler?.sendEmptyMessage(0)
        helper?.fetchDB(getString(R.string.user_entity), this)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alert_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        txtSearch = view.findViewById(R.id.txtSearch)
        adapter = UserListAdapter(context)
        recyclerView?.adapter = adapter
        recyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        txtSearch?.addTextChangedListener(this)
        return view
    }



}
