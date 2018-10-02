package ng.neas.com.model

import ng.neas.com.utils.MessageStatus

class FeedBackEntity {
    var fullname: String? = null
    var email: String? = null
    var feedback: String? = null
    var status: MessageStatus? = MessageStatus.UNREAD
    var ref: String? = null
}