package ng.neas.com

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import app.qtax.com.adapter.AlertEntryAdapter
import com.elkanahtech.widerpay.myutils.MyHandler
import com.elkanahtech.widerpay.myutils.StringUtils
import com.elkanahtech.widerpay.myutils.UIKits
import com.elkanahtech.widerpay.myutils.listeners.ResponseListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.fragments.AlertListFragment
import ng.neas.com.fragments.UserListFragment
import ng.neas.com.model.AlertEntity
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.Repository
import ng.neas.com.utils.UserRole

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ResponseListener, OnCompleteListener<Void>, ValueEventListener, AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
    }



    override fun onCancelled(p0: DatabaseError) {
        handler?.sendEmptyMessage(1)
    }
    override fun onDataChange(dataSnapshot: DataSnapshot) {

    }
    override fun onComplete(task: Task<Void>) {
        dialog?.dismiss()
        if(task.isSuccessful)
            handler?.sendEmptyMessage(1)
        else
            handler?.obtainMessage(1, task.exception?.message)?.sendToTarget()
    }
    override fun responseReceived(bool: Boolean) {
        if(bool){
            pref?.loggedInUser = null
            finish()
        }
    }
    private var dialog: Dialog? = null
    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? = null
    private var loggedInUser: UserEntity? = null
    private var pref: MySharedPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        val username = nav_view.getHeaderView(0).findViewById<TextView>(R.id.txtFullName)
        nav_view.setNavigationItemSelectedListener(this)
        pref = MySharedPreference(this)
        helper = MyFireBaseHelper(this)
        handler = MyHandler(this, false)
        loggedInUser = Gson().fromJson(pref?.loggedInUser, UserEntity::class.java)
        username.text = loggedInUser?.fullName
        nav_view.menu.findItem(R.id.adminMenu).isVisible = loggedInUser?.userRole == UserRole.ADMIN
        if(intent.hasExtra(getString(R.string.data)))
            showAlert(intent.getStringExtra(getString(R.string.data)), intent.getStringExtra(getString(R.string.full_meaning)))

    }

    private fun showAlert(title: String?, message: String?) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message).setPositiveButton("Okay"){dialog, _ -> dialog.dismiss() }
                .show()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            UIKits.promptLogout(this, this)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        drawer_layout.closeDrawer(GravityCompat.START)
        return when (item.itemId) {
            R.id.sign_out -> {UIKits.promptLogout(this, this);  false}
            R.id.about -> showAbout()
            R.id.new_alert -> addNewAlert()
            R.id.profile -> {startActivity(Intent(this, ProfileActivity::class.java));  false}
            R.id.faculty, R.id.department, R.id.general, R.id.unapproved  -> swapFragment(AlertListFragment(), item.itemId, item.title)
            R.id.users  ->  swapFragment(UserListFragment(), item.itemId, item.title)
            else ->  false
        }
    }
    private fun swapFragment(fragment : Fragment, itemId: Int, title : CharSequence) : Boolean{
        supportActionBar?.title = title
        val bundle = Bundle().apply { putInt(getString(R.string.data), itemId); putSerializable(getString(R.string.loggedInUser), loggedInUser) }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commitAllowingStateLoss()
        return true
    }
    private fun showAbout(): Boolean {
        AlertDialog.Builder(this)
                .setTitle("About App")
                .setMessage(getString(R.string.about))
                .setPositiveButton("Okay"){dialog, _ -> dialog.dismiss()  }
                .show()
        return false
    }
    private var spDepartment: Spinner? = null
    private var spFaculty: Spinner? = null
    private fun addNewAlert(): Boolean {
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.add_alert_layout)
        val txtTitle = dialog?.findViewById<EditText>(R.id.txtTitle)
        val txtContent = dialog?.findViewById<EditText>(R.id.txtContent)
        val btnContinue = dialog?.findViewById<Button>(R.id.btnContinue)
        val layoutLocation = dialog?.findViewById<LinearLayout>(R.id.layoutLocation)
        spFaculty = dialog?.findViewById(R.id.spFaculty)
        spDepartment = dialog?.findViewById(R.id.spDepartment)
        val chGeneral = dialog?.findViewById<CheckBox>(R.id.chGeneral)
        chGeneral?.setOnClickListener { toggleAddress(chGeneral, layoutLocation) }
        spFaculty?.onItemSelectedListener = this
        btnContinue?.setOnClickListener { attemptSubmit(txtTitle, txtContent, chGeneral) }
        dialog?.show()
        return false
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
    private fun toggleAddress(chGeneral: CheckBox?, layoutLocation: LinearLayout?) {
        when(chGeneral?.isChecked){
            false -> layoutLocation?.visibility = View.VISIBLE
            true -> layoutLocation?.visibility = View.GONE
        }
    }
    private fun attemptSubmit(txtTitle: EditText?, txtContent: EditText?, chGeneral: CheckBox?) {
        val titleStr = txtTitle?.text.toString()
        val content = txtContent?.text.toString()
        val faculty = if(chGeneral?.isChecked == true) getString(R.string.general) else spFaculty?.selectedItem.toString()
        val department = spDepartment?.selectedItem.toString()
        if(titleStr.isNullOrEmpty() || content.isNullOrEmpty()){
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        val alert = AlertEntity().apply { this.detail = content; this.publishedDate = Repository.getDate(); this.ref = StringUtils.getTransactionRef(); this.user = loggedInUser
                                            this.title = titleStr; this.user = user; this.faculty = faculty; this.department = department}
        handler?.sendEmptyMessage(0)
        Repository.addAlert(this, alert, helper, this)
    }
}
