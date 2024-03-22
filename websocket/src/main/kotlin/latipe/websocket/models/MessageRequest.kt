package latipe.websocket.models

data class MessageRequest(
    val topic: String,
    val message: String
)