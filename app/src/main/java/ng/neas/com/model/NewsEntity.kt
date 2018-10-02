package ng.neas.com.model

import ng.neas.com.utils.MessageStatus
import java.util.*

class NewsEntity {
    var title: String? = null
    var publishedDate: String? = null
    var description: String? = null
    var ref: String? = null
    var type: String? = null
    var status: MessageStatus? = null
}