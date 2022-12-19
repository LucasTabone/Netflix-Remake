package co.tiagoaguiar.netflixremake.Util

import android.os.Looper
import android.os.Message
import android.util.Log
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import java.util.logging.Handler
import javax.net.ssl.HttpsURLConnection
import javax.security.auth.callback.Callback
import kotlin.math.log

class CategoryTask(private val callback: Callback) {

    private val handler = android.os.Handler(Looper.getMainLooper())
    val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onPreExecute()
        fun onResult(categories: List<Category>)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()
        // nesse momento, estamos utilizando a UI-THREAD (1)


        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var buffer: BufferedInputStream? = null
            var stream: InputStream? = null

            try {
                // nesse Momento estamos utilizando a NOVA- THREAD (processo Paralelo) (2)
                val requestURL = URL(url) // abrir uma URL
                urlConnection = requestURL.openConnection() as HttpsURLConnection // ABRINDO CONEXÃO
                urlConnection.readTimeout = 2000 // tempo de Leitura (2segundos)
                urlConnection.connectTimeout = 2000 // tempo de conexão (2segundos)

                val statusCode: Int = urlConnection.responseCode //garantias de resposta com o servidor
                if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor")
                }

                stream = urlConnection.inputStream // sequencia de bytes
                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                val categories = toCategories(jsonAsString)

                handler.post {
                    callback.onResult(categories)
                }

            } catch (e: IOException) {
                val message = e.message ?: "erro desconhecido"
                Log.e("Teste", message, e)
                handler.post {
                    callback.onFailure(message)
                }
            } finally {
                urlConnection?.disconnect()
                stream?.close()
                buffer?.close()
            }
        }
    }

    private fun toCategories(jsonAsString: String): List<Category> {
        val categories = mutableListOf<Category>()

        val jsonRoot = JSONObject(jsonAsString)
        val jsonCategories = jsonRoot.getJSONArray("category")
        for (i in 0 until jsonCategories.length()) {
            val jsonCategory = jsonCategories.getJSONObject(i)

            val title = jsonCategory.getString("title")
            val jsonMovies = jsonCategory.getJSONArray("movie")

            val movies = mutableListOf<Movie>()
            for (j in 0 until jsonMovies.length()) {
                val jsonMovie = jsonMovies.getJSONObject(j)
                val id = jsonMovie.getInt("id")
                val coverUrl = jsonMovie.getString("cover_url")

                movies.add(Movie(id, coverUrl))
            }
            categories.add(Category(title, movies))
        }
        return categories
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