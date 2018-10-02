package ng.neas.com.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import ng.neas.com.R
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.model.UserEntity

class RegisterFragment : Fragment(), OnCompleteListener<Void>, AdapterView.OnItemSelectedListener, ValueEventListener {
    override fun onCancelled(p0: DatabaseError) {

    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        val studId = txtStudentId?.text.toString()
        if (dataSnapshot.hasChild(studId)) {
            handler?.obtainMessage(1, "Student ID already exist!")?.sendToTarget()
        } else
            attemptToRegister()
    }

    private fun attemptToRegister() {
        val fullname = txtFullName?.text.toString()
        val phone = txtPhone?.text.toString()
        val studId = txtStudentId?.text.toString()
        val matric = txtMatricNo?.text.toString()
        val password = txtPassword?.text.toString()
        val faculty = spFaculty?.selectedItem.toString()
        val department = spDepartment?.selectedItem.toString()
        val agree = chAgree?.isChecked

        if (fullname.isNullOrEmpty() || phone.isNullOrEmpty() || studId.isNullOrEmpty() || agree == false || matric.isNullOrEmpty() || password.isNullOrEmpty() || spFaculty?.selectedItemPosition == 0) {
            Toast.makeText(context, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        val user = UserEntity().apply { this.studentId = studId; this.fullName = fullname; this.faculty = faculty; this.phoneNo = phone; this.department = department; this.matricNo = matric; this.password = password }
        helper?.submitToDB(getString(R.string.user_entity), user.studentId, user, this)

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            0 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.select))
            1 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sict))
            2 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.set))
            3 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.saat))
            4 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.seet))
            5 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.semt))
            6 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sls))
            7 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sps))
            8 -> spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sste))
        }
    }

    override fun onComplete(task: Task<Void>) {
        if (task.isSuccessful)
            registrationSuccessful()
        else
            handler?.obtainMessage(1, task.exception?.message)?.sendToTarget()
    }

    private fun registrationSuccessful() {
        handler?.obtainMessage(3, 0, 0, "Registration Successful! \nLogin to Continue")?.sendToTarget()
        txtStudentId?.setText("")
        txtMatricNo?.setText("")
        txtFullName?.setText("")
        txtPhone?.setText("")
        txtPassword?.setText("")
        spFaculty?.setSelection(0)
        spDepartment?.adapter = ArrayAdapter(context,  android.R.layout.simple_list_item_1, resources.getStringArray(R.array.select))
    }

    private var helper: MyFireBaseHelper? = null
    private var handler: MyHandler? = null
    private var pref: MySharedPreference? = null
    private var txtFullName: EditText? = null
    private var txtStudentId: EditText? = null
    private var txtMatricNo: EditText? = null
    private var txtPhone: EditText? = null
    private var txtPassword: EditText? = null
    private var spFaculty: Spinner? = null
    private var spDepartment: Spinner? = null
    private var chAgree: CheckBox? = null
    private var btnContinue: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = MyHandler(context, false)
        pref = MySharedPreference(context!!)
        helper = MyFireBaseHelper(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_register, container, false)
        txtFullName = rootView.findViewById(R.id.txtFullName)
        txtPhone = rootView.findViewById(R.id.txtPhone)
        txtStudentId = rootView.findViewById(R.id.txtStudentId)
        txtMatricNo = rootView.findViewById(R.id.txtMatricNo)
        spFaculty = rootView.findViewById(R.id.spFaculty)
        spDepartment = rootView.findViewById(R.id.spDepartment)
        chAgree = rootView.findViewById(R.id.chAgree)
        txtPassword = rootView.findViewById(R.id.txtPassword)
        btnContinue = rootView.findViewById(R.id.btnContinue)
        btnContinue?.setOnClickListener { checkIfUserExist() }
        spFaculty?.onItemSelectedListener = this
        return rootView
    }

    private fun checkIfUserExist() {
        val studId = txtStudentId?.text.toString()
        handler?.sendEmptyMessage(0)
        helper?.queryDB(getString(R.string.user_entity), "studentId", studId, this)
    }


}