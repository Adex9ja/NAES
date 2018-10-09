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
import com.elkanahtech.widerpay.myutils.UIKits
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import ng.neas.com.R
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.UserRole

class UserListAdapter(context: Context?) : RecyclerView.Adapter<UserListAdapter.MyPlaceHolder>() {
    private var titleList:ArrayList<UserEntity>? = null
    private var context: Context? = context
    private var helper: MyFireBaseHelper? = null
    private var handler: MyHandler? = null
    init {
        helper = MyFireBaseHelper(context)
        handler = MyHandler(context, false)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaceHolder {
        var itemView = LayoutInflater.from(context).inflate(R.layout.user_entry_list, parent, false)
        return MyPlaceHolder(itemView)
    }

    fun swapList(titleList:ArrayList<UserEntity>?){
        this.titleList = ArrayList(titleList?.reversed())
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
       return titleList?.size ?: 0
    }

    override fun onBindViewHolder(holder: MyPlaceHolder, position: Int) {
        val news = titleList?.get(position)
        holder.txtSublist1.text = news?.fullName
        holder.imageView?.text = news?.fullName?.substring(0,1)
        holder.txtSublist2?.text = news?.department
        holder.txtSublist3?.text = "Matric No:\t" + news?.matricNo
    }


    inner class  MyPlaceHolder(itemView : View) : RecyclerView.ViewHolder(itemView), OnCompleteListener<Void> {
        override fun onComplete(task: Task<Void>) {
            if(task.isSuccessful)
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
                menu.menu.findItem(R.id.approve).isVisible = false
                menu.menu.findItem(R.id.share).isVisible = false
                menu.menu.findItem(R.id.delete).isVisible = true
                menu.menu.findItem(R.id.update).isVisible = true
                menu.setOnMenuItemClickListener {
                    val title = titleList?.get(adapterPosition)
                    when(it.itemId){
                        R.id.delete -> deleteUser(title)
                        R.id.update -> updateUser(title)
                    }
                    return@setOnMenuItemClickListener true
                }
                menu.show()
            }
            itemView.setOnClickListener {
                val title = titleList?.get(adapterPosition)
               loadAppropriateUser(title)
            }
        }
        private fun updateUser(userEntity: UserEntity?){
            userEntity?.userRole = if(userEntity?.userRole == UserRole.USER) UserRole.ADMIN else UserRole.USER
            handler?.sendEmptyMessage(0)
            helper?.submitToDB(context?.getString(R.string.user_entity) ?: "", userEntity?.studentId, userEntity, this)
        }
        private fun deleteUser(title: UserEntity?) {
            handler?.sendEmptyMessage(0)
            helper?.submitToDB(context?.getString(R.string.user_entity) ?: "", title?.studentId, null, this)
        }
        private fun loadAppropriateUser(title: UserEntity?) {
            AlertDialog.Builder(context!!)
                    .setIcon(R.drawable.user)
                    .setTitle("User Detail")
                    .setMessage(getDetail(title))
                    .setPositiveButton("Okay"){dialog, _ -> dialog.dismiss() }
                    .setNegativeButton("Change Privilege"){dialog, _ -> updateUser(title); dialog.dismiss() }
                    .show()

        }
        private fun getDetail(user: UserEntity?): String {
            var sb = StringBuilder()
            sb.append("Full Name :\t" + user?.fullName + "\n\n")
            sb.append("School :\t" + user?.faculty + "\n\n")
            sb.append("Department :\t" + user?.department + "\n\n")
            sb.append("Phone Number :\t" + user?.phoneNo + "\n\n")
            sb.append("Matric No :\t" + user?.matricNo + "\n\n")
            sb.append("Student ID :\t" + user?.studentId + "\n\n")
            sb.append("User Privilege :\t" + user?.userRole + "\n")
            return sb.toString()
        }


    }


}