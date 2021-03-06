package xyz.harrychen.trivialnews.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*

open class News(
        @PrimaryKey
        @SerializedName("_id")
        var id: Int = 0,
        @SerializedName("channel_id")
        var channelId: Int = 0,
        var title: String = "",
        var summary: String = "",
        @SerializedName("comment_num")
        var commentNum: Int = 0,
        @SerializedName("like_num")
        var likeNum: Int = 0,
        @SerializedName("read_num")
        var readNum: Int = 0,
        var author: String = "",
        var keywords: RealmList<String> = RealmList(),
        @SerializedName("pubdate")
        var publishDate: Date = Date(),
        var link: String = "",
        var picture: String = "",
        @SerializedName("has_read")
        var hasRead: Boolean = false
) : RealmObject()