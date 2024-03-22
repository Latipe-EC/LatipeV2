package latipe.websocket.controllers

import latipe.websocket.models.BroadcastEvent
import latipe.websocket.models.MessageRequest
import latipe.websocket.services.RedisBroadcastService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notification")
class NotificationController(private val redisBroadcastService: RedisBroadcastService) {
    @PostMapping
    fun newMessage(@RequestBody request: MessageRequest) {
        val event = BroadcastEvent(request.topic, request.message)
        redisBroadcastService.publish(event)
    }
}