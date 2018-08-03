package ng.neas.com.data

import android.app.Activity
import android.content.Context
import com.elkanahtech.widerpay.myutils.listeners.MyCallBackListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ng.neas.com.R
import ng.neas.com.model.AlertEntity
import ng.neas.com.utils.NetworkUtils


class MyFireBaseHelper(activity: Context?)  {


    private var database = FirebaseDatabase.getInstance().getReference(activity?.getString(R.string.app_name)!!)
    fun queryDB(table_title: String, query: String, value: String?, listener : ValueEventListener) {
        database.child(table_title).orderByChild(query).equalTo(value).addValueEventListener(listener)
    }
    fun submitToDB(table_title: String, ref: String?, T: Any?, listener: OnCompleteListener<Void>){
        database.child(table_title).child(ref ?: "").setValue(T).addOnCompleteListener(listener)
    }
    fun fetchDB(table_title: String, listener: ValueEventListener) {
        database.child(table_title).addValueEventListener(listener)
    }

}