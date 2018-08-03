package ng.neas.com

import android.app.Activity
import android.app.Application
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.AlertEntity
import ng.neas.com.utils.Repository

class App: Application(), ValueEventListener {
    override fun onCancelled(p0: DatabaseError) {

    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        rawData = ArrayList()
        for ( data in dataSnapshot.children){
            rawData?.add(data.getValue(AlertEntity::class.java)!!)
        }
        Repository.submitTODB(contentResolver, rawData)
    }

    private var helper: MyFireBaseHelper? = null

    override fun onCreate() {
        super.onCreate()
        helper = MyFireBaseHelper(this)
        rawData = Repository.fetchLocal(contentResolver)
        helper?.fetchDB(getString(R.string.alert_entity), this)

    }
    companion object {
        var rawData : ArrayList<AlertEntity>? = ArrayList()
    }
}