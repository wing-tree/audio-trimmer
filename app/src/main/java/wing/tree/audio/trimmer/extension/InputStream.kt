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
@Throws(IOException::class)
private fun getByteArrayFromInputStream(inputStream: InputStream): ByteArray? {
    val os = ByteArrayOutputStream()
    val buffer = ByteArray(0xFFFF)
    var len = inputStream.read(buffer)
    while (len != -1) {
        os.write(buffer, 0, len)
        len = inputStream.read(buffer)
    }
    return os.toByteArray()
}

@Synchronized
fun getByteArrayFile(context: Context, audioByteArray: ByteArray?): File? {
    val cache = context.externalCacheDir?.path + File.separator
    val temp = File(cache, Arrays.hashCode(audioByteArray).toString())
    try {
        FileOutputStream(temp).use { outputStream ->
            outputStream.write(audioByteArray)
            return temp
        }
    } catch (ignored: IOException) {
        return null
    }
}
