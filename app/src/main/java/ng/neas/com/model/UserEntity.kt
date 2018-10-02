package ng.neas.com.model

import ng.neas.com.utils.UserRole
import java.io.Serializable

class UserEntity : Serializable {
    var fullName: String? = null
    var studentId: String? = null
    var matricNo: String? = null
    var phoneNo: String? = null
    var faculty: String? = null
    var department: String? = null
    var password: String? = null
    var userRole: UserRole = UserRole.USER
}