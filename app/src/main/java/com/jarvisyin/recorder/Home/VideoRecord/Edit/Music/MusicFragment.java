package com.jarvisyin.recorder.Home.VideoRecord.Edit.Music;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.jarvisyin.recorder.Common.Utils.JYLog;
import com.jarvisyin.recorder.Common.Utils.JYToast;
import com.jarvisyin.recorder.Common.Widget.Dialog.ProgressDialog;
import com.jarvisyin.recorder.Home.VideoRecord.Common.VideoBaseFragment;
import com.jarvisyin.recorder.Home.VideoRecord.Common.Widget.ActionBar;
import com.jarvisyin.recorder.Home.VideoRecord.Edit.EditFragment;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;
import com.jarvisyin.recorder.R;

import java.util.List;

/**
 */
public class MusicFragment extends VideoBaseFragment {
    public static final String TAG = "VShopVideo MusicFragment";

    private List<MusicInfo> musicInfoList = MusicSource.getMusicInfos();
    private ListView listView;
    private ListViewAdapter listViewAdapter;
    private VideoRecordActivity mContext;


    public MusicFragment() {
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        mContext = (VideoRecordActivity) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_record_music, container, false);
        listView = (ListView) view.findViewById(R.id.list_view);

        listViewAdapter = new ListViewAdapter(mContext, musicInfoList);
        listView.setAdapter(listViewAdapter);

        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);

        actionBar.setBtnBackOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.addFragmentWithAnim(new EditFragment());
            }
        });

        actionBar.setBtnRightOnClickListenet(new SaveClick(mContext, listViewAdapter));

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public static class SaveClick implements View.OnClickListener, MusicProcessor.Callback {

        private final VideoRecordActivity mContext;
        private final ListViewAdapter mListViewAdapter;

        private ProgressDialog dialog;


        public SaveClick(VideoRecordActivity context, ListViewAdapter listViewAdapter) {
            mContext = context;
            mListViewAdapter = listViewAdapter;
        }

        @Override
        public void onClick(View v) {
            JYLog.i(TAG, "onClick");
            if (mListViewAdapter.currentMusic.equals(mContext.getBackgroundAudioInfo())) {
                mContext.replaceFragmentWithAnim(new EditFragment());
                return;
            }

            if (dialog == null) dialog = new ProgressDialog(mContext);
            dialog.show();

            MusicInfo musicInfo = mListViewAdapter.currentMusic;
            MusicProcessor processor = new MusicProcessor(
                    mContext,
                    musicInfo);
            processor.setCallback(this);
            processor.start();
        }


        @Override
        public void onSuccess() {
            mHandler.sendEmptyMessage(ON_SUCCESS);
        }

        @Override
        public void onProgress(String message) {
            Message msg = mHandler.obtainMessage(ON_PROGRESS);
            msg.what = ON_PROGRESS;
            msg.obj = message;
            mHandler.sendEmptyMessage(ON_PROGRESS);
        }

        @Override
        public void onFailure(String message) {
            Message msg = mHandler.obtainMessage(ON_FAILURE);
            msg.what = ON_FAILURE;
            msg.obj = message;
            mHandler.sendEmptyMessage(ON_FAILURE);
        }

        private final static int ON_SUCCESS = 1;
        private final static int ON_PROGRESS = 2;
        private final static int ON_FAILURE = 3;

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ON_SUCCESS:
                        mContext.setBackgroundAudioInfo(mListViewAdapter.currentMusic);
                        dialog.dismiss();
                        mContext.replaceFragmentWithAnim(new EditFragment());
                        break;
                    case ON_PROGRESS:
                        JYLog.i(TAG, "ON_PROGRESS = " + msg.obj);
                        break;
                    case ON_FAILURE:
                        JYToast.show("选择失败");
                        dialog.dismiss();
                        break;
                }
            }
        };

    }


    private static class ListViewAdapter extends BaseAdapter implements View.OnClickListener {

        private final VideoRecordActivity mContext;
        private final List<MusicInfo> musicInfoList;
        private MusicInfo currentMusic;

        public ListViewAdapter(VideoRecordActivity context, List<MusicInfo> musicInfoList) {
            this.mContext = context;
            this.musicInfoList = musicInfoList;
            this.currentMusic = context.getBackgroundAudioInfo();
        }

        @Override
        public int getCount() {
            return musicInfoList.size();
        }

        @Override
        public MusicInfo getItem(int position) {
            return musicInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return musicInfoList.get(position).getResourceId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                holder = new Holder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_video_record_music_list_item, null);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
                holder.textView = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(holder);
                convertView.setOnClickListener(this);
            } else {
                holder = (Holder) convertView.getTag();
            }

            MusicInfo musicInfo = getItem(position);
            holder.textView.setText(musicInfo.getName());
            convertView.setTag(R.id.item, musicInfo);
            if (currentMusic.equals(musicInfo)) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }

            return convertView;
        }

        @Override
        public void onClick(View v) {
            currentMusic = (MusicInfo) v.getTag(R.id.item);
            notifyDataSetChanged();
        }
    }

    public static class Holder {
        private CheckBox checkBox;
        private TextView textView;
    }


}
