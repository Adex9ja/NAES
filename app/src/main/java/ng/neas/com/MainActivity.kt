package ng.neas.com

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.elkanahtech.widerpay.myutils.MyHandler
import com.elkanahtech.widerpay.myutils.StringUtils
import com.elkanahtech.widerpay.myutils.UIKits
import com.elkanahtech.widerpay.myutils.listeners.ResponseListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.model.AlertEntity
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.ApprovalStatus
import ng.neas.com.utils.MessageStatus
import ng.neas.com.utils.Repository
import ng.neas.com.utils.UserRole

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ResponseListener, OnCompleteListener<Void>, AdapterView.OnItemSelectedListener, View.OnClickListener {
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.logout -> UIKits.promptLogout(this, this)
            R.id.notification -> startMainLoader(R.id.menu_hotlist, "Notification(s)")
            R.id.feedback -> startActivity(Intent(this, FeedbackActivity::class.java))
            R.id.changePass -> startActivity(Intent(this, ProfileActivity::class.java))
            R.id.news -> startActivity(Intent(this, NewsUpcomingActivity::class.java))
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
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
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    private var dialog: Dialog? = null
    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? = null
    private var user: UserEntity? = null
    private var pref: MySharedPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        val username = nav_view.getHeaderView(0).findViewById<TextView>(R.id.txtFullName)

        val notificationMenu = nav_view.menu.findItem(R.id.menu_hotlist)
        val pendingNotificationMenu = nav_view.menu.findItem(R.id.unapproved)
        val feedBackMenu = nav_view.menu.findItem(R.id.manage_feedback)
        val generalMenu = nav_view.menu.findItem(R.id.general)
        val facultyMenu = nav_view.menu.findItem(R.id.faculty)
        val departmentMenu = nav_view.menu.findItem(R.id.department)
        notificationText = notificationMenu?.actionView?.findViewById(R.id.txtCounter)
        pendingNotificationText = pendingNotificationMenu?.actionView?.findViewById(R.id.txtCounter)
        feedBackText = feedBackMenu?.actionView?.findViewById(R.id.txtCounter)
        generalAlert = generalMenu?.actionView?.findViewById(R.id.txtCounter)
        facultyAlert = facultyMenu?.actionView?.findViewById(R.id.txtCounter)
        deptAlert = departmentMenu?.actionView?.findViewById(R.id.txtCounter)

        nav_view.setNavigationItemSelectedListener(this)
        pref = MySharedPreference(this)
        helper = MyFireBaseHelper(this)
        handler = MyHandler(this, false)
        user = Gson().fromJson(pref?.loggedInUser, UserEntity::class.java)
        username.text = user?.fullName
        nav_view.menu.findItem(R.id.adminMenu).isVisible = user?.userRole == UserRole.ADMIN
        nav_view.itemIconTintList = null
        if(intent.hasExtra(getString(R.string.data)))
            showAlert(intent.getStringExtra(getString(R.string.data)), intent.getStringExtra(getString(R.string.full_meaning)))

        notification.setOnClickListener(this)
        feedback.setOnClickListener(this)
        changePass.setOnClickListener(this)
        news.setOnClickListener(this)
        logout.setOnClickListener(this)
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
            super.onBackPressed()
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
            R.id.faculty, R.id.department, R.id.general, R.id.unapproved, R.id.menu_hotlist  -> startMainLoader(item.itemId, item.title)
            R.id.users, R.id.manage_feedback, R.id.news  ->  startMainLoader(item.itemId, item.title)
            else ->  false
        }
    }

    private fun startMainLoader(itemId: Int, title: CharSequence?): Boolean {
        val intent = Intent(this, MainLoaderActivity::class.java)
        intent.putExtra(getString(R.string.data), itemId)
        intent.putExtra(getString(R.string.title), title)
        intent.putExtra(getString(R.string.loggedInUser), user)
        startActivity(intent)
        return false
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
        val deptLayout = dialog?.findViewById<LinearLayout>(R.id.deptLayout)
        val swFaculty = dialog?.findViewById<Switch>(R.id.swFaculty)
        spFaculty = dialog?.findViewById(R.id.spFaculty)
        spDepartment = dialog?.findViewById(R.id.spDepartment)
        val chGeneral = dialog?.findViewById<CheckBox>(R.id.chGeneral)
        chGeneral?.setOnClickListener { toggleAddress(chGeneral, layoutLocation) }
        spFaculty?.onItemSelectedListener = this
        swFaculty?.setOnClickListener { toggleDepartment(swFaculty, deptLayout) }
        btnContinue?.setOnClickListener { attemptSubmit(txtTitle, txtContent, chGeneral, swFaculty) }
        dialog?.show()
        return false
    }
    private fun toggleDepartment(swFaculty: Switch, deptLayout: LinearLayout?) {
        when(swFaculty.isChecked){
            true -> deptLayout?.visibility = View.GONE
            false -> deptLayout?.visibility = View.VISIBLE
        }
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
    }
    private fun toggleAddress(chGeneral: CheckBox?, layoutLocation: LinearLayout?) {
        when(chGeneral?.isChecked){
            false -> layoutLocation?.visibility = View.VISIBLE
            true -> layoutLocation?.visibility = View.GONE
        }
    }
    private fun attemptSubmit(txtTitle: EditText?, txtContent: EditText?, chGeneral: CheckBox?, swFaculty: Switch?) {
        val titleStr = txtTitle?.text.toString()
        val content = txtContent?.text.toString()
        val faculty = if(chGeneral?.isChecked == true) getString(R.string.general) else spFaculty?.selectedItem.toString()
        val department = if(swFaculty?.isChecked == true) getString(R.string.general) else spDepartment?.selectedItem.toString()
        if(titleStr.isNullOrEmpty() || content.isNullOrEmpty() ){
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        if(chGeneral?.isChecked == false &&spFaculty?.selectedItemPosition == 0){
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        val alert = AlertEntity().apply { this.detail = content; this.publishedDate = Repository.getDate(); this.ref = StringUtils.getTransactionRef(); this.user = this@MainActivity.user
                                            this.title = titleStr; this.user = user; this.faculty = faculty; this.department = department; this.messageStatus = MessageStatus.UNREAD}
        handler?.sendEmptyMessage(0)
        Repository.addAlert(this, alert, helper, this)
    }

    override fun onResume() {
        super.onResume()
        App.alertList?.asSequence()?.filter { it.faculty == user?.faculty || it.faculty == getString(R.string.general) || it.department == user?.department || it.department == getString(R.string.general) }?.filter { it.messageStatus == MessageStatus.UNREAD && it.status == ApprovalStatus.APPROVED}?.count()?.let { updateNotification(it) }
        App.alertList?.asSequence()?.filter { it.status == ApprovalStatus.PENDING }?.count()?.let { updatePendingNotification(it) }
        App.alertList?.asSequence()?.filter {it.faculty == getString(R.string.general) && it.status == ApprovalStatus.APPROVED && it.messageStatus == MessageStatus.UNREAD}?.count()?.let { updateGeneralNotification(it) }
        App.alertList?.asSequence()?.filter {it.faculty == user?.faculty && it.department == getString(R.string.general) && it.status == ApprovalStatus.APPROVED && it.messageStatus == MessageStatus.UNREAD}?.count()?.let { updateFacultyNotification(it) }
        App.alertList?.asSequence()?.filter { it.faculty != getString(R.string.general) && it.department == user?.department && it.status == ApprovalStatus.APPROVED && it.messageStatus == MessageStatus.UNREAD}?.count()?.let { updateDepartNotification(it) }
        App.feedBackList?.asSequence()?.filter { it.status == MessageStatus.UNREAD }?.count()?.let { updateFeedBackCount(it) }
        App.newsList?.asSequence()?.filter { it.status == MessageStatus.UNREAD }?.count()?.let { updateNewsCount(it) }
    }

    private fun updateDepartNotification(it: Int) {
        deptAlert?.visibility = if(it > 0) View.VISIBLE else View.GONE
    }

    private fun updateFacultyNotification(it: Int) {
        facultyAlert?.visibility = if(it > 0) View.VISIBLE else View.GONE
    }

    private fun updateGeneralNotification(it: Int) {
        generalAlert?.visibility = if(it > 0) View.VISIBLE else View.GONE
    }


    private var notificationText: TextView? = null
    private var pendingNotificationText: TextView? = null
    private var feedBackText: TextView? = null
    private var generalAlert: ImageView? = null
    private var facultyAlert: ImageView? = null
    private var deptAlert: ImageView? = null
    private fun updateNotification(new_hot_number: Int) {
        notificationText?.text = String.format("%s", new_hot_number)
        notificationCount.text = String.format("%s", new_hot_number)
    }
    private fun updatePendingNotification(new_hot_number: Int) {
        pendingNotificationText?.text = String.format("%s", new_hot_number)
    }
    private fun updateFeedBackCount(new_hot_number: Int) {
        feedBackText?.text = String.format("%s", new_hot_number)
    }
    private fun updateNewsCount(new_hot_number: Int) {
        newsCount.text = String.format("%s", new_hot_number)
    }


}
