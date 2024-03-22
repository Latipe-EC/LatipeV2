package latipe.notification.configs

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@Configuration
@EnableMongoRepositories(basePackages = ["latipe.notification.repositories"])
@EnableMongoAuditing
class MongoConfig : AbstractMongoClientConfiguration() {

    @Value("\${spring.data.mongodb.uri}")
    private lateinit var mongoUri: String

    private val converters: MutableList<Converter<*, *>> = ArrayList()

    @Bean
    fun transactionManager(mongoDatabaseFactory: MongoDatabaseFactory): MongoTransactionManager {
        return MongoTransactionManager(mongoDatabaseFactory)
    }

    override fun getDatabaseName(): String {
        return "Latipe-Notification-DB"
    }

    override fun mongoClient(): MongoClient {
        val connectionString = ConnectionString(mongoUri)
        return MongoClients.create(
            MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build()
        )
    }

    override fun customConversions(): MongoCustomConversions {
        converters.add(ZonedDateTimeReadConverter())
        converters.add(ZonedDateTimeWriteConverter())
        return MongoCustomConversions(converters)
    }

    class ZonedDateTimeReadConverter : Converter<Date, ZonedDateTime> {
        override fun convert(date: Date): ZonedDateTime {
            return date.toInstant().atZone(ZoneOffset.UTC)
        }
    }

    class ZonedDateTimeWriteConverter : Converter<ZonedDateTime, Date> {
        override fun convert(zonedDateTime: ZonedDateTime): Date {
            return Date.from(zonedDateTime.toInstant())
        }
    }
}