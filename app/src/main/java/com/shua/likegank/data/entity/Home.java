package com.shua.likegank.data.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Home extends RealmObject {

    public String title;
    public String createdAt;
    public String type;
    public String url;
    public String who;
    @PrimaryKey
    private String _id;

    public Home() {
    }

    public Home(String _id,
                String title,
                String createdAt,
                String type,
                String url,
                String who) {
        this._id = _id;
        this.title = title;
        this.createdAt = createdAt;
        this.type = type;
        this.url = url;
        this.who = who;
    }
}
