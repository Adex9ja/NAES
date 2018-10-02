package ng.neas.com

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.elkanahtech.widerpay.myutils.MyHandler
import com.elkanahtech.widerpay.myutils.StringUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_feedback.*
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.FeedBackEntity

class FeedbackActivity : AppCompatActivity(), OnCompleteListener<Void> {
    override fun onComplete(task: Task<Void>) {
        if(task.isSuccessful)
            successful()
        else
            handler?.obtainMessage(1,task.exception?.message)?.sendToTarget()
    }

    private fun successful() {
        handler?.obtainMessage(1,"Submitted Successfully")?.sendToTarget()
        txtEmail.setText("")
        txtFullname.setText("")
        txtFeedBack.setText("")
    }

    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        btnContinue.setOnClickListener { attemptSubmit() }
        handler = MyHandler(this, false)
        helper = MyFireBaseHelper(this)
    }

    private fun attemptSubmit() {
        val fullname = txtFullname.text.toString()
        val email = txtEmail.text.toString()
        val feedback = txtFeedBack.text.toString()

        if(fullname.isNullOrEmpty() || feedback.isNullOrEmpty() || email.isNullOrEmpty()){
            Toast.makeText(this, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        val feed = FeedBackEntity().apply { this.email = email; this.feedback = feedback; this.fullname = fullname; this.ref = StringUtils.getTransactionRef() }
        handler?.sendEmptyMessage(0)
        helper?.submitToDB(getString(R.string.feedback_entity), feed.ref, feed, this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
