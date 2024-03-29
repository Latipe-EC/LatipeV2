package latipe.websocket.models

import com.fasterxml.jackson.annotation.JsonProperty

data class StreamDataEvent(
    @JsonProperty("message") val message: String,
    @JsonProperty("topic") val topic: String? = null,
)