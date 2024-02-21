package com.fm.fmmedia.api.request

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class ProgressRequestBody(
    private val file: File,
    private val progressCallback: (Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? {
        return "multipart/form-data".toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return file.length()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inputStream: InputStream = FileInputStream(file)
        var uploaded: Long = 0

        inputStream.use { input ->
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
                progressCallback((100 * uploaded / fileLength).toInt())
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
