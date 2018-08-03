package ng.neas.com.model

import ng.neas.com.utils.ApprovalStatus

class AlertEntity {
    var ref: String? = null
    var title: String? = null
    var detail: String? = null
    var publishedDate: String? = null
    var faculty: String? = null
    var department: String? = null
    var user: UserEntity? = null
    var status: ApprovalStatus = ApprovalStatus.PENDING
}