package com.shua.likegank.data.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class Content : RealmObject {
    @PrimaryKey
    var url: String = ""
    var content: String = ""

    constructor()

    constructor(content: String, url: String) {
        this.content = content
        this.url = url
    }
}