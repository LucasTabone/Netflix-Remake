package co.tiagoaguiar.netflixremake.Util

import android.util.Log
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class CategoryTask {

    fun execute(url: String) {
        // nesse momento, estamos utilizando a UI-THREAD (1)
        val executor = Executors.newSingleThreadExecutor()

        executor.execute {
            try {
                // nesse Momento estamos utilizando a NOVA- THREAD (processo Paralelo) (2)
                val requestURL = URL(url) // abrir uma URL
                val urlConnection =
                    requestURL.openConnection() as HttpsURLConnection // ABRINDO CONEXÃO
                urlConnection.readTimeout = 2000 // tempo de Leitura (2segundos)
                urlConnection.connectTimeout = 2000 // tempo de conexão (2segundos)

                val statusCode: Int =
                    urlConnection.responseCode //garantias de resposta com o servidor
                if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor")
                }

                val stream = urlConnection.inputStream // sequencia de bytes
                val buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                Log.i("teste", jsonAsString)

            } catch (e: IOException) {
                Log.e("Teste", e.message ?: "erro desconhecido", e)
            }
        }
    }
    private fun toString(stream: InputStream): String {
        val bytes = ByteArray(1024)
        val baos = ByteArrayOutputStream()
        var read: Int
        while (true) {
            read = stream.read(bytes)
            if (read <= 0) {
                break
            }
            baos.write(bytes, 0, read)
        }
        return String(baos.toByteArray())
    }

}