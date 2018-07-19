package ng.neas.com

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_profile.*
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.model.UserEntity

class ProfileActivity : AppCompatActivity(), OnCompleteListener<Void>, AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(position){
            0 -> spDepartment?.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sict))
            1 -> spDepartment?.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.set))
            2 -> spDepartment?.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.saat))
            3 -> spDepartment?.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.seet))
            4 -> spDepartment?.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.semt))
        }
    }

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
        spFaculty.onItemSelectedListener = this
        fillProfile()
    }

    private fun fillProfile() {
        txtFullName.setText(loggedInUser?.fullName)
        txtEmail.setText(loggedInUser?.email)
        txtPhone.setText(loggedInUser?.phoneNo)
        resources?.getStringArray(R.array.faculty)?.indexOf(loggedInUser?.faculty)?.let { spFaculty.setSelection(it) }
        when(spFaculty.selectedItemPosition){
            0 -> resources?.getStringArray(R.array.sict)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            1 -> resources?.getStringArray(R.array.set)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            2 -> resources?.getStringArray(R.array.saat)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            3 -> resources?.getStringArray(R.array.seet)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            4 -> resources?.getStringArray(R.array.semt)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
        }
    }

    private fun attemptSubmit() {
        val name = txtFullName.text.toString()
        val email = txtEmail.text.toString()
        val faculty = spFaculty.selectedItem.toString()
        val department = spDepartment.selectedItem.toString()

        if(name.isNullOrEmpty() || email.isNullOrEmpty()){
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        loggedInUser?.fullName = name; loggedInUser?.faculty = faculty; loggedInUser?.email = email; loggedInUser?.department = department
        handler?.sendEmptyMessage(0)
        helper?.submitToDB(getString(R.string.user_entity), loggedInUser?.phoneNo, loggedInUser, this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
