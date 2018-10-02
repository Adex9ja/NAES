package app.qtax.com.adapter

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import ng.neas.com.R
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.FeedBackEntity
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.UserRole

class FeedbackAdapter(context: Context?) : RecyclerView.Adapter<FeedbackAdapter.MyPlaceHolder>() {
    private var feedbackList:ArrayList<FeedBackEntity>? = null
    private var context: Context? = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaceHolder {
        var itemView = LayoutInflater.from(context).inflate(R.layout.user_entry_list, parent, false)
        return MyPlaceHolder(itemView)
    }

    fun swapList(feedbackList:ArrayList<FeedBackEntity>?){
        this.feedbackList = ArrayList(feedbackList?.reversed())
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
       return feedbackList?.size ?: 0
    }

    override fun onBindViewHolder(holder: MyPlaceHolder, position: Int) {
        val news = feedbackList?.get(position)
        holder.txtSublist1.text = news?.feedback
        holder.imageView?.text = news?.fullname?.substring(0,1)
        holder.txtSublist2?.text = news?.fullname
        holder.txtSublist3?.text = news?.email
    }


    inner class  MyPlaceHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {


        var txtSublist3 = itemView.findViewById<TextView>(R.id.txtSublist3)
        var txtSublist1 = itemView.findViewById<TextView>(R.id.txtSublist1)
        var txtSublist2 = itemView.findViewById<TextView>(R.id.txtSublist2)
        var imageView = itemView.findViewById<TextView>(R.id.imageView)
        var optionMenu = itemView.findViewById<TextView>(R.id.optionMenu)
        init {
            optionMenu.visibility = View.GONE
            itemView.setOnClickListener {
                val title = feedbackList?.get(adapterPosition)
               loadAppropriateUser(title)
            }
        }

        private fun loadAppropriateUser(title: FeedBackEntity?) {
            AlertDialog.Builder(context!!)
                    .setIcon(R.drawable.feed)
                    .setTitle("FeedBack Detail")
                    .setMessage(getDetail(title))
                    .setPositiveButton("Okay"){dialog, _ -> dialog.dismiss() }
                    .show()

        }
        private fun getDetail(user: FeedBackEntity?): String {
            var sb = StringBuilder()
            sb.append("Full Name :\t" + user?.fullname + "\n\n")
            sb.append("Email :\t" + user?.email + "\n\n")
            sb.append("FeedBack :\t" + user?.feedback + "\n\n")
            return sb.toString()
        }


    }


}