package latipe.notification.viewmodels

import latipe.notification.models.EEventType

data class StreamDataEvent(val type: EEventType, val data: String)