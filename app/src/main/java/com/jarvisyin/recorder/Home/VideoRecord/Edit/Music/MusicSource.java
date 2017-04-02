package com.jarvisyin.recorder.Home.VideoRecord.Edit.Music;

import com.jarvisyin.recorder.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jarvisyin on 16/12/6.
 */
public class MusicSource {

    public static List<MusicInfo> getMusicInfos() {
        ArrayList<MusicInfo> musicInfoList = new ArrayList<>();
        musicInfoList.add(getEmptyMusicInfo());
        musicInfoList.add(new MusicInfo(R.raw.audio_caihong, "彩虹", "caihong.m4a"));
        musicInfoList.add(new MusicInfo(R.raw.audio_pugongyindeyueding, "蒲公英的约定", "pugongyindeyueding.m4a"));
        return musicInfoList;
    }


    public static MusicInfo getEmptyMusicInfo() {
        return new MusicInfo(-1, "无", null);
    }
}
