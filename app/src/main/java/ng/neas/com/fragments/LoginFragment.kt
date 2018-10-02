package ng.neas.com.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.*
import ng.neas.com.MainActivity
import ng.neas.com.R
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.model.UserEntity

class LoginFragment : Fragment(), ValueEventListener {
    override fun onCancelled(p0: DatabaseError) {
       handler?.sendEmptyMessage(1)
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        val phone = txtStudentId?.text.toString()
        if(dataSnapshot.hasChild(phone)){
            val user = dataSnapshot.child(phone).getValue(UserEntity::class.java)
            checkUser(user)
        }
       else
            handler?.obtainMessage(1, "Invalid Login!")?.sendToTarget()


    }

    private fun checkUser(user: UserEntity?) {
        val password = txtPassword?.text.toString()
        if(user?.password  == password || user?.phoneNo == password){
            handler?.sendEmptyMessage(1)
            pref?.loggedInUser = Gson().toJson(user)
            startActivity(Intent(context, MainActivity::class.java))
            activity?.finish()
            clearFields()
        }
        else
            handler?.obtainMessage(1, "Invalid Login!")?.sendToTarget()
    }

    private fun clearFields() {
        txtStudentId?.setText("")
        txtPassword?.setText("")
    }

    private var helper: MyFireBaseHelper? = null
    private var handler: MyHandler? = null
    private var pref: MySharedPreference? = null
    private var txtPassword : EditText? = null
    private var txtStudentId : EditText? = null
    private var btnContinue : Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = MyHandler(context, false)
        pref = MySharedPreference(context!!)
        helper = MyFireBaseHelper(context)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_login, container, false)
        txtPassword = rootView.findViewById(R.id.txtPassword)
        txtStudentId = rootView.findViewById(R.id.txtStudentId)
        btnContinue = rootView.findViewById(R.id.btnContinue)
        btnContinue?.setOnClickListener { attemptLogin() }
        return rootView
    }

    private fun attemptLogin() {
        val password = txtPassword?.text.toString()
        val studId = txtStudentId?.text.toString()

        if(password.isNullOrEmpty() || studId.isNullOrEmpty()){
            Toast.makeText(context, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }
        handler?.sendEmptyMessage(0)
        helper?.queryDB(getString(R.string.user_entity),"studentId", studId, this)
    }
}