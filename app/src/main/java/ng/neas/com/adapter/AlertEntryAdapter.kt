package app.qtax.com.adapter

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.elkanahtech.widerpay.myutils.UIKits
import ng.neas.com.R
import ng.neas.com.model.AlertEntity

class AlertEntryAdapter(context: Context?) : RecyclerView.Adapter<AlertEntryAdapter.MyPlaceHolder>() {
    private var titleList:ArrayList<AlertEntity>? = null
    private var context: Context? = context
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


    inner class  MyPlaceHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var txtSublist3 = itemView.findViewById<TextView>(R.id.txtSublist3)
        var txtSublist1 = itemView.findViewById<TextView>(R.id.txtSublist1)
        var txtSublist2 = itemView.findViewById<TextView>(R.id.txtSublist2)
        var imageView = itemView.findViewById<TextView>(R.id.imageView)
        var optionMenu = itemView.findViewById<TextView>(R.id.optionMenu)
        init {
            optionMenu.setOnClickListener{
                val menu = PopupMenu(context, it)
                menu.inflate(R.menu.menu_option)
                menu.setOnMenuItemClickListener {
                    val title = titleList?.get(adapterPosition)
                    when(it.itemId){
                        R.id.share -> UIKits.shareText(context!!, title?.detail ?:"")
                        R.id.view -> loadAppropriateCase(title)
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

        private fun loadAppropriateCase(title: AlertEntity?) {
            AlertDialog.Builder(context!!)
                    .setTitle(title?.title)
                    .setMessage(title?.detail)
                    .setPositiveButton("Okay"){dialog, _ -> dialog.dismiss() }
                    .setNegativeButton("Share"){dialog, _ -> dialog.dismiss(); UIKits.shareText(context!!, title?.detail ?:"")  }
                    .show()

        }
    }
}