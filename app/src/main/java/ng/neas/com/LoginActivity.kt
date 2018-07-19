package ng.neas.com

import android.content.Intent
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.elkanahtech.widerpay.myutils.MyHandler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.model.UserEntity

class LoginActivity : AppCompatActivity() {



    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var pref: MySharedPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mSectionsPagerAdapter?.addFragment(LoginFragment(), "Login")
        mSectionsPagerAdapter?.addFragment(RegisterFragment(), "Sign Up")
        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        pref = MySharedPreference(this)
        if(pref?.loggedInUser != null)
            startActivity(Intent(this, MainActivity::class.java))
    }






    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private var fragmentList: ArrayList<Fragment> = ArrayList()
        private var titleList: ArrayList<String> = ArrayList()

        fun addFragment(fragment: Fragment, title: String){
            fragmentList.add(fragment)
            titleList.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titleList[position]
        }
    }


    class LoginFragment : Fragment(), ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
           handler?.sendEmptyMessage(1)
        }

        override fun onDataChange(dataSnapshot:   DataSnapshot) {
            val phone = txtPhone?.text.toString()
            if(dataSnapshot.hasChild(phone)){
                val user = dataSnapshot.child(phone).getValue(UserEntity::class.java)
                checkUser(user)
            }
           else
                handler?.obtainMessage(1, "Invalid Login!")?.sendToTarget()


        }

        private fun checkUser(user: UserEntity?) {
            val email = txtEmail?.text.toString()
            if(user?.email  != email)
                handler?.obtainMessage(1, "Invalid Login!")?.sendToTarget()
            else{
                handler?.sendEmptyMessage(1)
                pref?.loggedInUser = Gson().toJson(user)
                startActivity(Intent(context, MainActivity::class.java))
                clearFields()
            }
        }

        private fun clearFields() {
            txtEmail?.setText("")
            txtPhone?.setText("")
        }

        private var helper: MyFireBaseHelper? = null
        private var handler: MyHandler? = null
        private var pref: MySharedPreference? = null
        private var txtPhone : EditText? = null
        private var txtEmail : EditText? = null
        private var btnContinue : Button? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            handler = MyHandler(context, false)
            pref = MySharedPreference(context)
            helper = MyFireBaseHelper(context)
        }
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_login, container, false)
            txtPhone = rootView.findViewById(R.id.txtPhone)
            txtEmail = rootView.findViewById(R.id.txtEmail)
            btnContinue = rootView.findViewById(R.id.btnContinue)
            btnContinue?.setOnClickListener { attemptLogin() }
            return rootView
        }

        private fun attemptLogin() {
            val phone = txtPhone?.text.toString()
            val email = txtEmail?.text.toString()

            if(phone.isNullOrEmpty() || email.isNullOrEmpty()){
                Toast.makeText(context, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
                return
            }
            handler?.sendEmptyMessage(0)
            helper?.queryDB(getString(R.string.user_entity),"phoneNo", phone, this)
        }
    }
    class RegisterFragment : Fragment(), OnCompleteListener<Void>, AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when(position){
                0 -> spDepartment?.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.sict))
                1 -> spDepartment?.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.set))
                2 -> spDepartment?.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.saat))
                3 -> spDepartment?.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.seet))
                4 -> spDepartment?.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.semt))
            }
        }

        override fun onComplete(task: Task<Void>) {
            if(task.isSuccessful)
                registrationSuccessful()
            else
                handler?.obtainMessage(1, task.exception?.message)?.sendToTarget()
        }

        private fun registrationSuccessful() {
            handler?.obtainMessage(3, 0,0, "Registration Successful! \nLogin to Continue" )?.sendToTarget()
            txtEmail?.setText("")
            txtFullName?.setText("")
            txtPhone?.setText("")
        }

        private var helper: MyFireBaseHelper? = null
        private var handler: MyHandler? = null
        private var pref: MySharedPreference? = null
        private var txtFullName : EditText? = null
        private var txtPhone : EditText? = null
        private var txtEmail : EditText? = null
        private var spFaculty : Spinner? = null
        private var spDepartment : Spinner? = null
        private var chAgree : CheckBox? = null
        private var btnContinue : Button? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            handler = MyHandler(context, false)
            pref = MySharedPreference(context)
            helper = MyFireBaseHelper(context)
        }
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_register, container, false)
            txtFullName = rootView.findViewById(R.id.txtFullName)
            txtPhone = rootView.findViewById(R.id.txtPhone)
            txtEmail = rootView.findViewById(R.id.txtEmail)
            spFaculty = rootView.findViewById(R.id.spFaculty)
            spDepartment = rootView.findViewById(R.id.spDepartment)
            chAgree = rootView.findViewById(R.id.chAgree)
            btnContinue = rootView.findViewById(R.id.btnContinue)
            btnContinue?.setOnClickListener { attemptToRegister() }
            spFaculty?.onItemSelectedListener = this
            return rootView
        }

        private fun attemptToRegister() {
            val fullname = txtFullName?.text.toString()
            val phone = txtPhone?.text.toString()
            val email = txtEmail?.text.toString()
            val faculty = spFaculty?.selectedItem.toString()
            val department = spDepartment?.selectedItem.toString()
            val agree = chAgree?.isChecked

            if(fullname.isNullOrEmpty() || phone.isNullOrEmpty() || email.isNullOrEmpty() || agree == false){
                Toast.makeText(context, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
                return
            }

            val user = UserEntity().apply { this.email = email; this.fullName = fullname; this.faculty = faculty; this.phoneNo = phone; this.department = department }
            handler?.sendEmptyMessage(0)
            helper?.submitToDB(getString(R.string.user_entity), user.phoneNo, user, this)

        }



    }

    }

