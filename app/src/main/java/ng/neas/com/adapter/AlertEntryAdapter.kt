package app.qtax.com.adapter

import android.app.Activity
import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.elkanahtech.widerpay.myutils.MyHandler
import com.elkanahtech.widerpay.myutils.UIKits
import com.elkanahtech.widerpay.myutils.listeners.MyCallBackListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import ng.neas.com.R
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.AlertEntity
import ng.neas.com.model.FirebasePushEntity
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.ApprovalStatus
import ng.neas.com.utils.NetworkUtils
import ng.neas.com.utils.UserRole

class AlertEntryAdapter(context: Context?, user: UserEntity?) : RecyclerView.Adapter<AlertEntryAdapter.MyPlaceHolder>() {
    private var titleList:ArrayList<AlertEntity>? = null
    private var context: Context? = context
    private var user: UserEntity? = user
    private var helper: MyFireBaseHelper? = null
    private var handler: MyHandler? = null
    init {
        helper = MyFireBaseHelper(context)
        handler = MyHandler(context, false)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaceHolder {
        var itemView = LayoutInflater.from(context).inflate(R.layout.alert_entry_list, parent, false)
        return MyPlaceHolder(itemView)
    }

    fun swapList(titleList:ArrayList<AlertEntity>?){
        titleList?.sortedByDescending { it.publishedDate }?.let { this.titleList = ArrayList(it) }
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
       return titleList?.size ?: 0
    }

    override fun onBindViewHolder(holder: MyPlaceHolder, position: Int) {
        val news = titleList?.get(position)
        holder.txtSublist1.text = news?.title
        holder.imageView?.text = news?.title?.substring(0,1)
        holder.txtSublist2?.text = news?.detail
        holder.txtSublist3?.text = "Published Date: " + news?.publishedDate
    }


    inner class  MyPlaceHolder(itemView : View) : RecyclerView.ViewHolder(itemView), OnCompleteListener<Void>, MyCallBackListener {
        override fun onErrorOccur(error: String?, callCode: Int) {

        }

        override fun onProcessComplete(result: String?, callCode: Int) {

        }

        override fun onComplete(task: Task<Void>) {
            if(task?.isSuccessful)
                handler?.obtainMessage(1, "Completed Successfully")?.sendToTarget()
            else
                handler?.obtainMessage(1, task.exception?.message)?.sendToTarget()
        }

        var txtSublist3 = itemView.findViewById<TextView>(R.id.txtSublist3)
        var txtSublist1 = itemView.findViewById<TextView>(R.id.txtSublist1)
        var txtSublist2 = itemView.findViewById<TextView>(R.id.txtSublist2)
        var imageView = itemView.findViewById<TextView>(R.id.imageView)
        var optionMenu = itemView.findViewById<TextView>(R.id.optionMenu)
        init {
            optionMenu.setOnClickListener{
                val menu = PopupMenu(context, it)
                menu.inflate(R.menu.menu_option)
                menu.menu.findItem(R.id.approve).isVisible = user?.userRole ==  UserRole.ADMIN
                menu.menu.findItem(R.id.delete).isVisible = user?.userRole ==  UserRole.ADMIN
                menu.setOnMenuItemClickListener {
                    val title = titleList?.get(adapterPosition)
                    when(it.itemId){
                        R.id.share -> UIKits.shareText(context!!, title?.detail ?:"")
                        R.id.approve -> updateStatus(title)
                        R.id.delete -> deleteAlert(title)
                    }
                    return@setOnMenuItemClickListener true
                }
                menu.show()
            }
            itemView.setOnClickListener {
                val title = titleList?.get(adapterPosition)
               loadAppropriateCase(title)
            }
        }
        private fun updateStatus(title: AlertEntity?) {
            title?.status = if(title?.status == ApprovalStatus.APPROVED) ApprovalStatus.PENDING else ApprovalStatus.APPROVED
            handler?.sendEmptyMessage(0)
            helper?.submitToDB(context?.getString(R.string.alert_entity) ?: "", title?.ref, title, this)
            if(title?.status == ApprovalStatus.APPROVED ){
                val message = FirebasePushEntity().apply { this.message = title?.detail; this.title = title?.title }
                NetworkUtils.postRequest(context as Activity, message , this, 100)
            }

        }
        private fun deleteAlert(title: AlertEntity?) {
            handler?.sendEmptyMessage(0)
            helper?.submitToDB(context?.getString(R.string.alert_entity) ?: "", title?.ref, null, this)
        }
        private fun loadAppropriateCase(title: AlertEntity?) {
            AlertDialog.Builder(context!!)
                    .setTitle(title?.title)
                    .setMessage(getDetail(title))
                    .setPositiveButton("Okay"){dialog, _ -> dialog.dismiss() }
                    .setNegativeButton("Share"){dialog, _ -> dialog.dismiss(); UIKits.shareText(context!!, title?.detail ?:"")  }
                    .show()
        }

        private fun getDetail(alert: AlertEntity?): String {
            var sb = StringBuilder()
            sb.append("Published By:\t" + alert?.user?.fullName + "\n\n")
            sb.append("Department :\t" + alert?.user?.department + "\n\n")
            sb.append("Contact :\t" + alert?.user?.phoneNo + "\n\n")
            sb.append("Published Date :\t" + alert?.publishedDate + "\n\n")
            sb.append("Message :\t" + alert?.detail + "\n")
            return sb.toString()
        }
    }


}