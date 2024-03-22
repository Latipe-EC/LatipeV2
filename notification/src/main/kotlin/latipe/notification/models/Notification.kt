package latipe.notification.models

import org.springframework.data.mongodb.core.mapping.Document

@Document("notifications")
class Notification(
    val title: String,
    val message: String,
    val issuedBy: String,
    val userId: String,
    val url: String?,
    val type: ENotificationType, // Ensure this property exists and matches the type of `input.type`
    val issuedFor: ENotificationIssues,
    val metadata: Map<String, Any>?
) : AbstractAuditEntity() {
    var id: String? = null
    var readAt: Long = 0L
}