package org.bytekeeper.ctr

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import org.bytekeeper.ctr.entity.Race
import org.springframework.core.annotation.Order
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.InputStream
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.Instant
import java.util.zip.ZipError
import kotlin.streams.asSequence

@Service
@Order(2)
class BasilSource(private val config: Config,
                  private val basilBotService: BasilBotService,
                  private val webClient: RedirectingWebClient) : BotSource {
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()
    private val log = LogManager.getLogger()
    private var botCache = emptyMap<String, BotInfo>()
    private val lastDownload = mutableMapOf<String, Path>()
    private val mapper = jacksonObjectMapper()

    override fun allBotInfos(): List<BotInfo> = botCache.values.toList()

    override fun refresh() {
        try {
            botCache = mapper.readValue<List<BotInfo>>(config.basilBotSource.toFile())
                    .map { it.name to it }
                    .toMap()
        } catch (e: java.lang.Exception) {
            log.error("Could not update bot cache (File format ok?)", e)
            botCache = emptyMap()
        }
        val now = Instant.now()
        lastDownload.values.forEach { path ->
            Files.delete(path)
        }
        lastDownload.clear()
        botCache.values.forEach { botInfo ->
            try {
                val lastUpdate = basilBotService.lastUpdateOf(botInfo.name)
                downloadToCache(botInfo, lastUpdate)
                        ?.let { cachedFile ->
                            val md = MessageDigest.getInstance("MD5")
                            Files.newInputStream(cachedFile)
                                    .use {
                                        val buffer = ByteArray(1024 * 1024)
                                        var bytes = it.read(buffer)
                                        while (bytes > 0) {
                                            md.update(buffer, 0, bytes)
                                            bytes = it.read(buffer)
                                        }
                                    }
                            val updated = basilBotService.update(botInfo.name, md.digest().map { byte ->
                                val v = byte.toInt()
                                val a = HEX_CHARS[(v and 0xF0) ushr 4]
                                val b = HEX_CHARS[v and 0x0F]
                                "$a$b"
                            }.joinToString(""), now)
                            botInfo.lastUpdated = updated
                        }
            } catch (e: Exception) {
                log.error("Couldn't download ${botInfo.name}", e)
            }
        }
    }

    override fun downloadBinary(info: org.bytekeeper.ctr.BotInfo): InputStream? {
        if (info !is BotInfo) return null
        val binary = lastDownload[info.name] ?: return null
        return Files.newInputStream(binary)
    }

    override fun downloadBwapiDLL(info: org.bytekeeper.ctr.BotInfo): InputStream? {
        if (info !is BotInfo) return null
        val binary = lastDownload[info.name] ?: return null
        try {
            FileSystems.newFileSystem(binary, null)
                    .use {
                        val bwapi = Files.createTempFile("bwapi", ".dll")
                        val compressedBwapi = it.rootDirectories.asSequence()
                                .flatMap { Files.list(it).asSequence() }
                                .first { it.fileName.toString().equals("bwapi.dll", true) }
                        Files.copy(compressedBwapi, bwapi, StandardCopyOption.REPLACE_EXISTING)
                        return Files.newInputStream(bwapi)
                    }
        } catch (e: ZipError) {
            throw FailedToDownloadBot("Zip file of ${info.name} was defect", e)
        }
    }

    private fun downloadToCache(botInfo: BotInfo, lastUpdate: Instant?): Path? {
        val tempFile = Files.createTempFile("bot-binary", ".zip")
        Files.newOutputStream(tempFile)
                .use { out ->
                    val body = webClient
                            .get(URI.create(botInfo.botBinary))
                            .flatMap {
                                if (lastUpdate != null && !Instant.ofEpochMilli(it.headers().asHttpHeaders().lastModified).isAfter(lastUpdate)) {
                                    Mono.empty<ClientResponse>()
                                }
                                it.toMono()
                            }
                            .block()
                            .bodyToMono(DataBuffer::class.java)
                    DataBufferUtils.write(body, out)
                            .map(DataBufferUtils::release)
                            .then()
                            .block()
                }
        if (Files.size(tempFile) == 0L) {
            Files.delete(tempFile)
            return null
        }
        lastDownload[botInfo.name] = tempFile
        return tempFile
    }

    override fun botInfoOf(name: String): BotInfo? = botCache[name]

    class BotInfo(override val name: String,
                  override val authorKey: String? = null,
                  override var disabled: Boolean = false,
                  val botBinary: String,
                  @JsonProperty("race")
                  val raceValue: String,
                  override val botType: String) : org.bytekeeper.ctr.BotInfo {
        @JsonIgnore
        override var lastUpdated: Instant = Instant.now()
        @JsonIgnore
        override val clearReadDirectory: Boolean = false

        @JsonIgnore
        override val race: Race = parseRace(raceValue)

        override val publishReadDirectory: Boolean
            @JsonIgnore get() = authorKey != null

        private fun parseRace(race: String): Race =
                when (race) {
                    "Terran" -> Race.TERRAN
                    "Zerg" -> Race.ZERG
                    "Protoss" -> Race.PROTOSS
                    "Random" -> Race.RANDOM
                    else -> throw IllegalStateException("Unknown race $race")
                }

    }
}