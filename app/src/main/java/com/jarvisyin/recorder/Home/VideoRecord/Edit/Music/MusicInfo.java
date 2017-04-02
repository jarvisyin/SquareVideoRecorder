package com.jarvisyin.recorder.Home.VideoRecord.Edit.Music;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by jarvisyin on 16/7/2.
 */
public class MusicInfo implements Serializable {
    public MusicInfo(int resourceId, String name, String fileName) {
        this.resourceId = resourceId;
        this.name = name;
        this.fileName = fileName;
    }

    private int resourceId;
    private String name;
    private String fileName;

    public int getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (o == this) return true;

        if (!(o instanceof MusicInfo)) {
            return false;
        }
        MusicInfo m = (MusicInfo) o;

        return resourceId == m.resourceId &&
                TextUtils.equals(name, m.name) &&
                TextUtils.equals(fileName, m.fileName);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + resourceId;
        result = 37 * result + (name == null ? 0 : name.hashCode());
        result = 37 * result + (fileName == null ? 0 : fileName.hashCode());
        return result;
    }
}
