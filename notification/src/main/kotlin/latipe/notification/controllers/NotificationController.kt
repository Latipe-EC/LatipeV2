package latipe.notification.controllers;

import latipe.notification.Services.NotificationService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController("/api/v1/notification")
@Validated
class NotificationController(private val notificationService: NotificationService) {

    @PatchMapping("/{id}/read")
    fun readNotification(@PathVariable id: String): CompletableFuture<Void> {
        return notificationService.readAt(id)
    }

}
