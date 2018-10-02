package ng.neas.com.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import app.qtax.com.adapter.AlertEntryAdapter
import ng.neas.com.App

import ng.neas.com.R
import ng.neas.com.data.MyContentProvider
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.ApprovalStatus
import ng.neas.com.utils.MessageStatus
import ng.neas.com.utils.Repository


class AlertListFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var txtSearch: EditText? = null
    private var adapter: AlertEntryAdapter? = null
    private var itemId :Int? = null
    private var user: UserEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getInt(getString(R.string.data))
        user = arguments?.getSerializable(getString(R.string.loggedInUser)) as UserEntity?
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_alert_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        txtSearch = view.findViewById(R.id.txtSearch)
        adapter = AlertEntryAdapter(context, user)
        recyclerView?.adapter = adapter
        recyclerView?.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        loadData()
        //markRead()
        txtSearch?.visibility = View.GONE
        return view
    }

    private fun markRead() {
        when(itemId){
            R.id.faculty ->     {
                Repository.updateAlert(activity?.contentResolver, MyContentProvider.KEY_FACULTY + " = '" + user?.faculty + "' and " + MyContentProvider.KEY_DEPARTMENT + " = '" + getString(R.string.general) + "'")
                App.alertList?.apply { this.forEach { if(it.faculty == user?.faculty && it.department == getString(R.string.general) && it.status == ApprovalStatus.APPROVED) it.messageStatus = MessageStatus.READ } }
            }
            R.id.department ->  {
                Repository.updateAlert(activity?.contentResolver, MyContentProvider.KEY_FACULTY + " <> '" + getString(R.string.general) + "' and " + MyContentProvider.KEY_DEPARTMENT + " = '" + user?.department + "'")
                App.alertList?.apply { this.forEach { if(it.faculty != getString(R.string.general) && it.department == user?.department && it.status == ApprovalStatus.APPROVED) it.messageStatus = MessageStatus.READ } }
            }
            R.id.general ->     {
                Repository.updateAlert(activity?.contentResolver, MyContentProvider.KEY_FACULTY + " = '" + getString(R.string.general) + "'")
                App.alertList?.apply { this.forEach { if(it.faculty == getString(R.string.general) && it.status == ApprovalStatus.APPROVED) it.messageStatus = MessageStatus.READ } }
            }
            R.id.menu_hotlist -> {
                Repository.updateAlert(activity?.contentResolver, MyContentProvider.KEY_MESSAGE_STATUS + " = '" + MessageStatus.UNREAD.toString() + "'")
                App.alertList?.apply { this.forEach { if(it.status == ApprovalStatus.APPROVED && it.messageStatus == MessageStatus.UNREAD) it.messageStatus = MessageStatus.READ } }
            }
        }
    }

    private fun loadData() {
        when(itemId){
            R.id.faculty ->  App.alertList?.filter { it.faculty == user?.faculty && it.department == getString(R.string.general) && it.status == ApprovalStatus.APPROVED }?.let { adapter?.swapList(ArrayList(it)) }
            R.id.department -> App.alertList?.filter { it.faculty != getString(R.string.general) && it.department == user?.department && it.status == ApprovalStatus.APPROVED}?.let { adapter?.swapList(ArrayList(it)) }
            R.id.general -> App.alertList?.filter { it.faculty == getString(R.string.general) && it.status == ApprovalStatus.APPROVED }?.let { adapter?.swapList(ArrayList(it)) }
            R.id.unapproved -> App.alertList?.filter { it.status == ApprovalStatus.PENDING }?.let { adapter?.swapList(ArrayList(it)) }
            else -> App.alertList?.asSequence()?.filter { it.faculty == user?.faculty || it.faculty == getString(R.string.general) || it.department == user?.department || it.department == getString(R.string.general) }?.filter { it.status == ApprovalStatus.APPROVED && it.messageStatus == MessageStatus.UNREAD }?.toList()?.let { adapter?.swapList(ArrayList(it)) }
        }
    }

}
