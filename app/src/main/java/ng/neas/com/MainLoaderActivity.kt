package ng.neas.com

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import ng.neas.com.fragments.AlertListFragment
import ng.neas.com.fragments.ManageFeedbackFragment
import ng.neas.com.fragments.PublishNewsFragment
import ng.neas.com.fragments.UserListFragment
import ng.neas.com.model.UserEntity

class MainLoaderActivity : AppCompatActivity() {

    private var itemId: Int? = null
    private var itemTitle: CharSequence? = null
    private var loggedInUser: UserEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_loader)
        itemId = intent.getIntExtra(getString(R.string.data), -1)
        itemTitle = intent?.getCharSequenceExtra(getString(R.string.title))
        loggedInUser = intent?.getSerializableExtra(getString(R.string.loggedInUser)) as UserEntity?
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = itemTitle
        loadAppropriateFragment()
    }

    private fun loadAppropriateFragment() {
        when(itemId){
            R.id.users -> swapFragment(UserListFragment())
            R.id.manage_feedback -> swapFragment(ManageFeedbackFragment())
            R.id.faculty, R.id.department, R.id.general, R.id.unapproved, R.id.menu_hotlist -> swapFragment(AlertListFragment())
            R.id.news -> swapFragment(PublishNewsFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
    private fun swapFragment(fragment: Fragment){
        val bundle = Bundle().apply {
            putInt(getString(R.string.data), itemId ?: -1)
            putSerializable(getString(R.string.loggedInUser), loggedInUser)
        }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commitAllowingStateLoss()
    }
}
