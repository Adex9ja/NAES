package ng.neas.com.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.elkanahtech.widerpay.myutils.MyHandler
import com.elkanahtech.widerpay.myutils.StringUtils
import com.elkanahtech.widerpay.myutils.UIKits
import com.elkanahtech.widerpay.myutils.fragments.DatePickerFragment
import com.elkanahtech.widerpay.myutils.listeners.MyDatePickerListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

import ng.neas.com.R
import ng.neas.com.data.MyFireBaseHelper
import ng.neas.com.model.NewsEntity
import ng.neas.com.utils.MessageStatus
import java.text.SimpleDateFormat
import java.util.*


class PublishNewsFragment : Fragment(), OnCompleteListener<Void>, AdapterView.OnItemSelectedListener, MyDatePickerListener {
    override fun selectedDate(y: Int, m: Int, d: Int) {
        txtEventDate?.setText("$y-$m-$d")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        txtEventDate?.setText(sdf.format(Date()))
        eventLayout?.visibility = if(position == 0) View.GONE else View.VISIBLE
    }

    override fun onComplete(task: Task<Void>) {
        if(task.isSuccessful)
            successfulPublish()
        else
            handler?.obtainMessage(1, task.exception?.message)?.sendToTarget()
    }

    private fun successfulPublish() {
        handler?.obtainMessage(1, "Submitted Successfully!")?.sendToTarget()
        txtTitle?.setText("")
        txtContent?.setText("")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = MyHandler(context, false)
        helper = MyFireBaseHelper(context)
    }

    private var handler: MyHandler? = null
    private var helper: MyFireBaseHelper? = null
    private var txtTitle: EditText? = null
    private var spType: Spinner? = null
    private var eventLayout: LinearLayout? = null
    private var txtEventDate: EditText? = null
    private var txtContent: EditText? = null
    private var btnContinue: Button? = null
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_publish_news, container, false)
        txtTitle = view.findViewById(R.id.txtTitle)
        spType = view.findViewById(R.id.spType)
        eventLayout = view.findViewById(R.id.eventLayout)
        txtEventDate = view.findViewById(R.id.txtEventDate)
        txtContent = view.findViewById(R.id.txtContent)
        btnContinue = view.findViewById(R.id.btnContinue)
        btnContinue?.setOnClickListener { attemptSubmit() }
        txtEventDate?.setOnClickListener { DatePickerFragment.NewInstance(this).show(fragmentManager!!, null) }
        spType?.onItemSelectedListener = this
        return view
    }

    private fun attemptSubmit() {
        val newTitle = txtTitle?.text.toString()
        val eventDate = txtEventDate?.text.toString()
        val content = txtContent?.text.toString()
        val type = spType?.selectedItem.toString()

        if(newTitle.isNullOrEmpty() || content.isNullOrEmpty()){
            Toast.makeText(context, getString(R.string.required_field), Toast.LENGTH_SHORT).show()
            return
        }

        val news = NewsEntity().apply { this.status = MessageStatus.UNREAD; this.description = content; this.title = newTitle;
            this.ref = StringUtils.getTransactionRef(); this.type = type; this.publishedDate = eventDate;  }
        handler?.sendEmptyMessage(0)
        helper?.submitToDB(getString(R.string.news_entity), news.ref, news, this)
    }


}
