package com.shua.likegank.data.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class Girl : RealmObject {
    @PrimaryKey
    var _id: String = ""
    var dec: String = ""
    var url: String = ""

    constructor()

    constructor(_id: String, dec: String, url: String) {
        this._id = _id
        this.dec = dec
        this.url = url
    }
}