package com.shua.likegank.data.entity;

import io.realm.kotlin.types.RealmObject;
import io.realm.kotlin.types.annotations.PrimaryKey;

/**
 * Home
 * Realm 要求必须有一个无参的构造函数
 */
open class Home : RealmObject {

    @PrimaryKey
    var _id: String = ""
    var title: String = ""
    var createdAt: String = ""
    var type: String = ""
    var url: String = ""
    var who: String = ""

    constructor()

    constructor(
        _id: String,
        title: String,
        createdAt: String,
        type: String,
        url: String,
        who: String
    ) {
        this._id = _id
        this.title = title
        this.createdAt = createdAt
        this.type = type
        this.url = url
        this.who = who
    }
}