package latipe.notification.models

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

open class AbstractAuditEntity {

    @CreatedBy
    @Field("created_by")
    var createdBy: String? = null

    @CreatedDate
    @Field("created_date")
    var createdDate: Date? = null

    @LastModifiedBy
    @Field("last_modified_by")
    var lastModifiedBy: String? = null

    @LastModifiedDate
    @Field("last_modified_date")
    var lastModifiedDate: Date? = null

}