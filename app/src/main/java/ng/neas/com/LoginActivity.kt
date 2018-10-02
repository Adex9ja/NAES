package ng.neas.com

import android.content.Intent
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle

import kotlinx.android.synthetic.main.activity_login.*
import ng.neas.com.data.MySharedPreference
import ng.neas.com.fragments.LoginFragment
import ng.neas.com.fragments.RegisterFragment

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
            gotoMainActivity()
    }

    private fun gotoMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
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


}

