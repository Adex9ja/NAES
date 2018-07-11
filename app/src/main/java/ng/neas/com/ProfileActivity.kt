package ng.neas.com

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_profile.*
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.model.UserEntity

class ProfileActivity : AppCompatActivity(), OnCompleteListener<Void> {
    override fun onComplete(task: Task<Void>) {
        handler?.obtainMessage(1, task.exception?.message ?: "Updated Successfully")?.sendToTarget()
        pref?.loggedInUser = Gson().toJson(loggedInUser)
    }

    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? = null
    private var loggedInUser: UserEntity? = null
    private var pref: MySharedPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pref = MySharedPreference(this)
        helper = MyFireBaseHelper(this)
        handler = MyHandler(this, false)
        loggedInUser = Gson().fromJson(pref?.loggedInUser, UserEntity::class.java)
        btnContinue.setOnClickListener { attemptSubmit() }
        fillProfile()
    }

    private fun fillProfile() {
        txtFullName.setText(loggedInUser?.fullName)
        txtEmail.setText(loggedInUser?.email)
        txtPhone.setText(loggedInUser?.phoneNo)
        resources?.getStringArray(R.array.states)?.indexOf(loggedInUser?.location)?.let { spAddress.setSelection(it) }
    }

    private fun attemptSubmit() {
        val name = txtFullName.text.toString()
        val email = txtEmail.text.toString()
        val location = spAddress.selectedItem.toString()

        if(name.isNullOrEmpty() || email.isNullOrEmpty()){
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        loggedInUser?.fullName = name; loggedInUser?.location = location; loggedInUser?.email = email
        handler?.sendEmptyMessage(0)
        helper?.submitToDB(getString(R.string.user_entity), loggedInUser?.phoneNo, loggedInUser, this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
