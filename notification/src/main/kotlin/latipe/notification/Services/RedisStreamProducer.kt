package latipe.notification.Services

import latipe.notification.viewmodels.StreamDataEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

@Service
class RedisStreamProducer(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
    @Value("\${spring.application.name}") private val applicationName: String,
) {
    companion object {
        private val atomicInteger = AtomicInteger(0)
        private val logger = LoggerFactory.getLogger(RedisStreamProducer::class.java)
    }

    fun publishEvent(streamTopic: String, data: StreamDataEvent) {
        val record = StreamRecords.newRecord().ofObject(data).withStreamKey(streamTopic)
        reactiveRedisTemplate.opsForStream<String, String>().add(record).subscribe()
    }

}