package ng.neas.com.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import ng.neas.com.R

/**
 * Created by ADEOLU on 12/16/2017.
 */
class MySharedPreference(private val context: Context) {



    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor: SharedPreferences.Editor = pref.edit()

    var loggedInUser: String?
        get() = pref.getString(context.getString(R.string.loggedInUser), null)
        set(username){
            editor.putString(context.getString(R.string.loggedInUser), username)
            editor.commit()
        }
    var isAlertLoaded: Int?
        get() = pref.getInt(context.getString(R.string.isAlertLoaded), -1)
        set(alert){
            editor.putInt(context.getString(R.string.isAlertLoaded), alert ?: -1)
            editor.commit()
        }

}
