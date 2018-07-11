package ng.neas.com

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
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
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.data.MySharedPreference
import ng.neas.com.model.AlertEntity
import ng.neas.com.model.UserEntity
import ng.neas.com.utils.Repository

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ResponseListener, OnCompleteListener<Void>, ValueEventListener {
    override fun onCancelled(p0: DatabaseError) {
        handler?.sendEmptyMessage(1)
    }
    override fun onDataChange(dataSnapshot: DataSnapshot) {
        rawData = ArrayList()
        for ( data in dataSnapshot.children){
            rawData?.add(data.getValue(AlertEntity::class.java)!!)
        }
        adapter?.swapList(rawData)
        Repository.submitTODB(this, rawData)
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
    private var rawData : ArrayList<AlertEntity>? = ArrayList()
    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? = null
    private var loggedInUser: UserEntity? = null
    private var pref: MySharedPreference? = null
    private var adapter: AlertEntryAdapter? = null
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
        adapter = AlertEntryAdapter(this)
        loggedInUser = Gson().fromJson(pref?.loggedInUser, UserEntity::class.java)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        rawData = Repository.fetchLocal(this)
        helper?.fetchDB(getString(R.string.alert_entity), this)
        username.text = loggedInUser?.fullName
        rawData?.filter { it.location == getString(R.string.general) }?.let { adapter?.swapList(ArrayList(it)) }
    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            UIKits.promptLogout(this, this)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.sign_out -> UIKits.promptLogout(this, this)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        drawer_layout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.sign_out -> UIKits.promptLogout(this, this)
            R.id.new_alert -> addNewAlert()
            R.id.profile -> startActivity(Intent(this, ProfileActivity::class.java))
            R.id.location -> {
                rawData?.filter { it.location == loggedInUser?.location }?.let { adapter?.swapList(ArrayList(it)) }
                return true
            }
            R.id.general -> {
                rawData?.filter { it.location == getString(R.string.general) }?.let { adapter?.swapList(ArrayList(it)) }
                return true
            }
        }
        return false
    }



    private fun addNewAlert() {
        dialog = Dialog(this, R.style.AppTheme_NoActionBar)
        dialog?.setContentView(R.layout.add_alert_layout)
        val txtTitle = dialog?.findViewById<EditText>(R.id.txtTitle)
        val txtContent = dialog?.findViewById<EditText>(R.id.txtContent)
        val btnContinue = dialog?.findViewById<Button>(R.id.btnContinue)
        val layoutLocation = dialog?.findViewById<LinearLayout>(R.id.layoutLocation)
        val spAddress = dialog?.findViewById<Spinner>(R.id.spAddress)
        val chGeneral = dialog?.findViewById<CheckBox>(R.id.chGeneral)
        chGeneral?.setOnClickListener { toggleAddress(chGeneral, layoutLocation) }
        btnContinue?.setOnClickListener { attemptSubmit(txtTitle, txtContent, chGeneral, spAddress) }
        dialog?.show()
    }
    private fun toggleAddress(chGeneral: CheckBox?, layoutLocation: LinearLayout?) {
        when(chGeneral?.isChecked){
            false -> layoutLocation?.visibility = View.VISIBLE
            true -> layoutLocation?.visibility = View.GONE
        }
    }
    private fun attemptSubmit(txtTitle: EditText?, txtContent: EditText?, chGeneral: CheckBox?, spAddress: Spinner?) {
        val titleStr = txtTitle?.text.toString()
        val content = txtContent?.text.toString()

        if(titleStr.isNullOrEmpty() || content.isNullOrEmpty()){
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        val alert = AlertEntity().apply { this.detail = content; this.publishedDate = Repository.getDate(); this.ref = StringUtils.getTransactionRef(); this.user = loggedInUser
                                            this.title = titleStr; this.user = user; this.location = if(chGeneral?.isChecked == true) getString(R.string.general) else spAddress?.selectedItem.toString()}
        handler?.sendEmptyMessage(0)
        Repository.addAlert(this, alert, helper, this)
    }
}