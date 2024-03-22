package latipe.notification.Services

import latipe.notification.models.Notification
import latipe.notification.repositories.NotificationRepository
import latipe.notification.viewmodels.CreateNotificationRequest
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class NotificationService(private val notificationRepository: NotificationRepository) {

    fun save(input: CreateNotificationRequest): CompletableFuture<Notification> {
        return CompletableFuture.supplyAsync {
            notificationRepository.save(
                Notification(
                    title = input.title,
                    message = input.message,
                    issuedBy = input.issuedBy,
                    userId = input.userId,
                    url = input.url,
                    type = input.type,
                    issuedFor = input.issuedFor,
                    metadata = input.metadata
                )
            )
        }
    }

    fun readAt(id: String): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val notification = notificationRepository.findById(id)
                .orElseThrow { throw IllegalArgumentException("Notification not found") }
            notification.readAt = System.currentTimeMillis()
            notificationRepository.save(notification)
        }
    }

}