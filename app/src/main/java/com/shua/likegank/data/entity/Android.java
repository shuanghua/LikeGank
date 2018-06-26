package com.shua.likegank.data.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Android Entity
 * Created by ShuangHua on 2017/5/2.
 */

public class Android extends RealmObject {

    @PrimaryKey
    private String _id;
    public String time;
    public String content;
    public String author;
    public String url;

    public Android() {
    }

    public Android(String time, String content, String author, String _id, String url) {
        this.time = time;
        this.content = content;
        this.author = author;
        this._id = _id;
        this.url = url;
    }
}
