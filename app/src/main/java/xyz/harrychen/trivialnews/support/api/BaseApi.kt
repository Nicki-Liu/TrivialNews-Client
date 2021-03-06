package xyz.harrychen.trivialnews.support.api

import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.gson.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.RealmObject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import xyz.harrychen.trivialnews.support.API_BASE_URL
import xyz.harrychen.trivialnews.support.utils.ApiException
import xyz.harrychen.trivialnews.support.utils.DateUtils
import xyz.harrychen.trivialnews.support.utils.toISO8601String
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit

interface BaseApi {
    companion object {

        private val newsApiResponseInterceptor by lazy {
            { chain: Interceptor.Chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                val body = response.body()!!.string()

                val json = Parser().parse(StringBuilder(body)) as JsonObject

                val errorCode = json.int("error_code")!!
                if (response.code() == 200 && errorCode == 0) {
                    val result = json["result"] as JsonBase
                    val newBodyString = result.toJsonString()
                    val newResponse = ResponseBody.create(response.body()!!.contentType(), newBodyString)
                    response.newBuilder().body(newResponse).build()

                } else {
                    val errorMessage = json.string("error_message")!!
                    val reason = json.string("reason")!!
                    throw ApiException(errorCode, errorMessage, reason)
                }
            }
        }

        private val fromBuilder =
                { builder: OkHttpClient.Builder ->
                    builder.addInterceptor(newsApiResponseInterceptor)
                            .connectTimeout(3, TimeUnit.SECONDS)
                            .writeTimeout(3, TimeUnit.SECONDS)
                            .readTimeout(3, TimeUnit.SECONDS)
                            .build()
                }

        private var httpClient = fromBuilder(OkHttpClient.Builder())

        fun setToken(token: String) {
            httpClient = fromBuilder(OkHttpClient
                    .Builder()
                    .addInterceptor { chain: Interceptor.Chain ->
                        val request = chain.request()
                        val newRequest = request.newBuilder()
                                .header("Authorization", "Bearer $token").build()
                        chain.proceed(newRequest)
                    })
            RETROFIT = buildRetrofit()
        }

        private var RETROFIT: Retrofit? = null

        val GSON: Gson by lazy {
            GsonBuilder()
                    .setExclusionStrategies(object : ExclusionStrategy {
                        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                            return false
                        }

                        override fun shouldSkipField(f: FieldAttributes?): Boolean {
                            return f?.declaredClass == RealmObject::class.java
                        }
                    })
                    .registerTypeAdapter(Date::class.java,
                            JsonDeserializer<Date> { json: JsonElement?,
                                                     _: Type?, _: JsonDeserializationContext? ->
                                DateUtils.fromISO8601String(json!!.asString)
                            })
                    .registerTypeAdapter(Date::class.java,
                            JsonSerializer<Date> { src: Date?, _: Type?, _: JsonSerializationContext? ->
                                JsonPrimitive(src!!.toISO8601String())
                            })
                    .create()
        }

        private fun buildRetrofit(): Retrofit {
            val builder = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GSON))
                    .baseUrl(API_BASE_URL)
                    .client(httpClient)
            return builder.build()
        }


        fun observeCompletableApi(api: Completable): Completable {
            return api.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }

        fun <T> observeSingleSubscribableApi(api: Single<T>): Single<T> {
            return api.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }


        fun getRetrofit(): Retrofit {
            if (RETROFIT == null) {
                RETROFIT = buildRetrofit()
            }
            return RETROFIT!!
        }

    }
}