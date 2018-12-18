package org.bytekeeper.ctr.publish

import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.bytekeeper.ctr.PreparePublish
import org.bytekeeper.ctr.Publisher
import org.bytekeeper.ctr.Scbw
import org.bytekeeper.ctr.any
import org.bytekeeper.ctr.entity.Bot
import org.bytekeeper.ctr.entity.BotRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant

@RunWith(MockitoJUnitRunner::class)
internal class ReadDirectoryPublisherTest {
    @Mock
    private lateinit var publisher: Publisher

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var scbw: Scbw

    @get: Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataPath: Path

    private lateinit var botReadDir: Path

    private lateinit var sut: ReadDirectoryPublisher

    val botA = Bot(null, true, "botA", null, null, null, false, null, 0, 1000)

    @Before
    fun setup() {
        sut = ReadDirectoryPublisher(botRepository, publisher, scbw)
        dataPath = temporaryFolder.newFolder().toPath()
        botReadDir = temporaryFolder.newFolder().toPath()
        given(scbw.readDirectoryOf(any())).willReturn(botReadDir)
        given(publisher.botDataPath(ArgumentMatchers.anyString())).willReturn(dataPath)
        given(botRepository.findAllByEnabledTrueAndPublishReadTrue())
                .willReturn(listOf(botA))
    }

    @Test
    fun `should create 7z file`() {
        // GIVEN

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(publisher.botDataPath("botA").resolve("read.7z")).exists()
    }

    @Test
    fun `should not create 7z file if folder is too large`() {
        // GIVEN
        Files.write(botReadDir.resolve("test"), ByteArray(60 * 1024 * 1024))
        val subdir = botReadDir.resolve("subdir")
        Files.createDirectories(subdir)
        Files.write(subdir.resolve("test"), ByteArray(60 * 1024 * 1024))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(publisher.botDataPath("botA").resolve("read.7z")).doesNotExist()
    }

    @Test
    fun `should add entries to 7z file`() {
        // GIVEN
        Files.write(botReadDir.resolve("test"), ByteArray(1024 * 1024))
        val subdir = botReadDir.resolve("subdir")
        Files.createDirectories(subdir)
        Files.write(subdir.resolve("test"), ByteArray(3 * 1024 * 1024))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        SevenZFile(publisher.botDataPath("botA").resolve("read.7z").toFile())
                .use { file ->
                    assertThat(file.entries)
                            .extracting("name", "size")
                            .contains(
                                    tuple("test", 1 * 1024 * 1024L),
                                    tuple("subdir/test", 3 * 1024 * 1024L)
                            )
                }
    }

    @Test
    fun `should not update more than once a day`() {
        // GIVEN
        val lastModified = Instant.now().minusSeconds(1000)
        sut.handle(PreparePublish())
        val compressedRead = publisher.botDataPath("botA").resolve("read.7z")
        Files.setLastModifiedTime(compressedRead, FileTime.from(lastModified))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(Files.getLastModifiedTime(compressedRead).toInstant()).isEqualTo(lastModified)
    }

    @Test
    fun `should update after a day`() {
        // GIVEN
        val lastModified = Instant.now().minusSeconds(90000)
        sut.handle(PreparePublish())
        val compressedRead = publisher.botDataPath("botA").resolve("read.7z")
        Files.setLastModifiedTime(compressedRead, FileTime.from(lastModified))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(Files.getLastModifiedTime(compressedRead).toInstant()).isAfter(lastModified)
    }

    @Test
    fun `should encrypt read if requested`() {
        // GIVEN
        botA.authorKeyId = "228B7F33"
        Files.write(botReadDir.resolve("test"), ByteArray(1024 * 1024))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(publisher.botDataPath("botA").resolve("read.7z")).exists()
    }

    @Test
    fun `should overwrite existing file`() {
        // GIVEN
        botA.authorKeyId = "228B7F33"
        val testFile = botReadDir.resolve("test")
        val compressed = publisher.botDataPath("botA").resolve("read.7z")
        Files.write(testFile, ByteArray(1024 * 1024))
        sut.handle(PreparePublish())
        Files.write(testFile, ByteArray(0))
        Files.setLastModifiedTime(compressed, FileTime.from(Instant.MIN))

        // WHEN
        sut.handle(PreparePublish())

        // THEN
        assertThat(Files.size(compressed)).isLessThanOrEqualTo(450)
    }
}