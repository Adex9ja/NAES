package app.qtax.com.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ng.neas.com.App
import ng.neas.com.R
import ng.neas.com.model.NewsEntity
import ng.neas.com.utils.MessageStatus
import ng.neas.com.utils.Repository

class NewsListAdater(context: Context?) : RecyclerView.Adapter<NewsListAdater.MyPlaceHolder>() {
    private var newsList:ArrayList<NewsEntity>? = null
    private var context: Context? = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaceHolder {
        var itemView = LayoutInflater.from(context).inflate(R.layout.user_entry_list, parent, false)
        return MyPlaceHolder(itemView)
    }

    fun swapList(feedbackList:ArrayList<NewsEntity>?){
        this.newsList = feedbackList
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
       return newsList?.size ?: 0
    }

    override fun onBindViewHolder(holder: MyPlaceHolder, position: Int) {
        val news = newsList?.get(position)
        holder.txtSublist1.text = news?.title
        holder.imageView?.text = news?.title?.substring(0,1)
        holder.txtSublist2?.text = news?.description
        holder.txtSublist3?.text = "Published Date: " + news?.publishedDate.toString()
        var typeface = if(news?.status == MessageStatus.UNREAD) Typeface.BOLD else Typeface.NORMAL
        holder.txtSublist1.setTypeface(null, typeface)
        holder.txtSublist2.setTypeface(null, typeface)
        holder.txtSublist3.setTypeface(null, typeface)
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
                val title = newsList?.get(adapterPosition)
               loadAppropriateUser(title)
            }
        }

        private fun loadAppropriateUser(title: NewsEntity?) {
            Repository.updateNews(context?.contentResolver, title?.ref)
            newsList = Repository.loadNews(context?.contentResolver!!)
            App.newsList = newsList
            notifyDataSetChanged()
            AlertDialog.Builder(context!!)
                    .setIcon(R.drawable.newspaper)
                    .setTitle("Detail")
                    .setMessage(getDetail(title))
                    .setPositiveButton("Okay"){dialog, _ -> dialog.dismiss(); }
                    .show()

        }
        private fun getDetail(user: NewsEntity?): String {
            var sb = StringBuilder()
            sb.append("Title :\t" + user?.title + "\n\n")
            sb.append((if(user?.type == context?.resources?.getStringArray(R.array.content_type)?.get(0)) "Published Date :\t" else "Event Date :\t") + user?.publishedDate + "\n\n")
            sb.append("News Detail :\t" + user?.description + "\n\n")
            return sb.toString()
        }


    }


}