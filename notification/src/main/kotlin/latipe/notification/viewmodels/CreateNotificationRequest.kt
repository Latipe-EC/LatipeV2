package latipe.notification.viewmodels

import latipe.notification.models.ENotificationIssues
import latipe.notification.models.ENotificationType

data class CreateNotificationRequest(
    val title: String,
    val message: String,
    val issuedBy: String,
    val userId: String,
    val url: String?,
    val type: ENotificationType,
    val issuedFor: ENotificationIssues,
    val metadata: Map<String, Any>?
)