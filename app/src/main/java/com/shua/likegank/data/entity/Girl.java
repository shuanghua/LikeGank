package com.shua.likegank.data.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Girl extends RealmObject {
    @PrimaryKey
    public String _id;
    public String dec;
    public String url;

    public Girl() {
    }

    public Girl(String _id, String dec, String url) {
        this._id = _id;
        this.dec = dec;
        this.url = url;
    }
}
