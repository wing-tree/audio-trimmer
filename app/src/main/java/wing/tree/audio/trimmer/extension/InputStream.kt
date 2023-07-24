package wing.tree.audio.trimmer.extension

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Arrays

fun InputStream.toFile(context: Context): File? {
    return try {
        getByteArrayFile(context, getByteArrayFromInputStream(this))
    } catch (ignored: IOException) {
        null
    }
}

@Synchronized
private fun getByteArrayFromInputStream(inputStream: InputStream): ByteArray? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val buffer = ByteArray(0xFFFF)

    var len = inputStream.read(buffer)

    while (len > Int.NEGATIVE_ONE) {
        byteArrayOutputStream.write(buffer, Int.ZERO, len)
        len = inputStream.read(buffer)
    }

    return byteArrayOutputStream.toByteArray()
}

@Synchronized
private fun getByteArrayFile(context: Context, audioByteArray: ByteArray?): File? {
    val parent = "${context.externalCacheDir?.path}${File.separator}"
    val file = File(parent, "${Arrays.hashCode(audioByteArray)}")

    try {
        FileOutputStream(file).use { outputStream ->
            outputStream.write(audioByteArray)

            return file
        }
    } catch (ignored: IOException) {
        return null
    }
}
