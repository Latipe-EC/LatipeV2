package latipe.notification.Services

import com.google.gson.Gson
import latipe.notification.configs.Const.Companion.WEBSOCKET_CONSUMER
import latipe.notification.models.EEventType
import latipe.notification.viewmodels.CreateNotificationRequest
import latipe.notification.viewmodels.StreamDataEvent
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Service

@Service
class RedisStreamConsumer(
    private val notificationService: NotificationService,
    private val gson: Gson,
    private val redisPublic: RedisStreamProducer
) : StreamListener<String, ObjectRecord<String, StreamDataEvent>> {
    companion object {
        private val logger = LoggerFactory.getLogger(RedisStreamConsumer::class.java)
    }


    override fun onMessage(record: ObjectRecord<String, StreamDataEvent>) {
        logger.info("[NEW] --> received message: ${record.value} from stream: ${record.stream}")

        record.value.type.let { type ->
            when (type) {
                EEventType.NORMAL_NOTIFICATION -> {
                    val data =
                        gson.fromJson(record.value.data, CreateNotificationRequest::class.java)
                    notificationService.save(data);
                    redisPublic.publishEvent(WEBSOCKET_CONSUMER, record.value)
                }

                EEventType.SYSTEM_NOTIFICATION -> {
                    logger.info("received system notification")
                    val data =
                        gson.fromJson(record.value.data, CreateNotificationRequest::class.java)
                    notificationService.save(data)
                    redisPublic.publishEvent(WEBSOCKET_CONSUMER, record.value)
                }

                else -> {}
            }
        }
    }
}