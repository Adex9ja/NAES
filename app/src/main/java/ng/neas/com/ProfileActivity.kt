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
            0 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.select))
            1 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sict))
            2 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.set))
            3 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.saat))
            4 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.seet))
            5 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.semt))
            6 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sls))
            7 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sps))
            8 -> spDepartment?.adapter = ArrayAdapter(this,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sste))
        }

        when(spFaculty.selectedItemPosition){
            0 -> resources?.getStringArray(R.array.select)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            1 -> resources?.getStringArray(R.array.sict)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            2 -> resources?.getStringArray(R.array.set)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            3 -> resources?.getStringArray(R.array.saat)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            4 -> resources?.getStringArray(R.array.seet)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            5 -> resources?.getStringArray(R.array.semt)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            6 -> resources?.getStringArray(R.array.sls)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            7 -> resources?.getStringArray(R.array.sps)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
            8 -> resources?.getStringArray(R.array.sste)?.indexOf(loggedInUser?.department)?.let { spDepartment.setSelection(it) }
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
        txtStudentId.setText(loggedInUser?.studentId)
        txtMatricNo.setText(loggedInUser?.matricNo)
        txtPhone.setText(loggedInUser?.phoneNo)
        txtPassword.setText(loggedInUser?.password)
        resources?.getStringArray(R.array.faculty)?.indexOf(loggedInUser?.faculty)?.let { spFaculty.setSelection(it) }
    }

    private fun attemptSubmit() {
        val name = txtFullName.text.toString()
        val studId = txtStudentId.text.toString()
        val matric = txtMatricNo.text.toString()
        val password = txtPassword.text.toString()
        val faculty = spFaculty.selectedItem.toString()
        val department = spDepartment.selectedItem.toString()

        if(name.isNullOrEmpty() || matric.isNullOrEmpty() || studId.isNullOrEmpty() || spFaculty?.selectedItemPosition == 0 || password.isNullOrEmpty()){
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        loggedInUser?.fullName = name; loggedInUser?.faculty = faculty; loggedInUser?.studentId = studId; loggedInUser?.department = department;
        loggedInUser?.matricNo = matric; loggedInUser?.password = password
        handler?.sendEmptyMessage(0)
        helper?.submitToDB(getString(R.string.user_entity), loggedInUser?.studentId, loggedInUser, this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
