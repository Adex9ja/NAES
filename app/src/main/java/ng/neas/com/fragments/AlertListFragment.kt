package ng.neas.com.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import app.qtax.com.adapter.AlertEntryAdapter
import ng.neas.com.App

import ng.neas.com.R
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.ApprovalStatus


class AlertListFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var adapter: AlertEntryAdapter? = null
    private var itemId :Int? = null
    private var user: UserEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments.getInt(getString(R.string.data))
        user = arguments.getSerializable(getString(R.string.loggedInUser)) as UserEntity?
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alert_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = AlertEntryAdapter(context, user)
        recyclerView?.adapter = adapter
        recyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        loadData()
        return view
    }

    private fun loadData() {
        when(itemId){
            R.id.faculty ->  App.rawData?.filter { it.faculty == user?.faculty && it.status == ApprovalStatus.APPROVED }?.let { adapter?.swapList(ArrayList(it)) }
            R.id.department -> App.rawData?.filter { it.department == user?.department && it.status == ApprovalStatus.APPROVED}?.let { adapter?.swapList(ArrayList(it)) }
            R.id.general -> App.rawData?.filter { it.faculty == getString(R.string.general) && it.status == ApprovalStatus.APPROVED }?.let { adapter?.swapList(ArrayList(it)) }
            R.id.unapproved -> App.rawData?.filter { it.status == ApprovalStatus.PENDING }?.let { adapter?.swapList(ArrayList(it)) }
        }
    }

}
