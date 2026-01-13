package com.esurakhsh

import com.esurakhsh.utils.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FileUtilsTest {

    @Test
    fun testCallLogToCsv() {
        val logs = listOf(
            CallLogData("123456", 1, 1000L, 60),
            CallLogData("987654", 2, 2000L, 30)
        )
        val csv = FileUtils.convertCallLogsToCsv(logs)
        val expected = "Number,Type,Date,Duration(sec)\n123456,Incoming,1000,60\n987654,Outgoing,2000,30\n"
        assertEquals(expected, csv)
    }

    @Test
    fun testZipCreation() {
        val tempDir = Files.createTempDirectory("test_zip").toFile()
        val file1 = File(tempDir, "test1.txt").apply { writeText("Hello") }
        val file2 = File(tempDir, "test2.txt").apply { writeText("World") }
        val zipFile = File(tempDir, "output.zip")

        FileUtils.zipFiles(listOf(file1, file2), zipFile)

        assertTrue(zipFile.exists())
        assertTrue(zipFile.length() > 0)
        
        tempDir.deleteRecursively()
    }
}
