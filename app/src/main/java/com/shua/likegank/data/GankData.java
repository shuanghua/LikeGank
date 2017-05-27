package com.shua.likegank.data;

import java.util.List;

/**
 * GankData
 * Created by SHUA on 2017/3/27.
 */

public class GankData {

    private boolean error;
    private List<LikeGankEntity> results;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public List<LikeGankEntity> getResults() {
        return results;
    }

    public void setResults(List<LikeGankEntity> results) {
        this.results = results;
    }
}
