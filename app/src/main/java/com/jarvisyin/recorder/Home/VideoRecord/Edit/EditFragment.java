package com.jarvisyin.recorder.Home.VideoRecord.Edit;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jarvisyin.recorder.Common.Utils.JYToast;
import com.jarvisyin.recorder.Common.Widget.Dialog.ProgressDialog;
import com.jarvisyin.recorder.Home.VideoRecord.BlockInfo;
import com.jarvisyin.recorder.Home.VideoRecord.Common.VideoBaseFragment;
import com.jarvisyin.recorder.Home.VideoRecord.Common.Widget.ActionBar;
import com.jarvisyin.recorder.Home.VideoRecord.Edit.Final.FinalVideoCreator;
import com.jarvisyin.recorder.Home.VideoRecord.Edit.Music.MusicFragment;
import com.jarvisyin.recorder.Home.VideoRecord.Record.RecordFragment;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;
import com.jarvisyin.recorder.R;

import java.util.List;


/**
 * Created by Jarvis.
 */
public class EditFragment extends VideoBaseFragment implements View.OnClickListener {
    public static final String TAG = EditFragment.class.getName();
    private VideoRecordActivity mContext;
    private List<BlockInfo> mBlockInfos;
    private EditHandler mHandler = new EditHandler();
    private MediaPlayer mVideoPlayer;
    private AudioPlayer mAudioPlayer;
    private VideoSurfaceView mVideoSurfaceView;
    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (VideoRecordActivity) getBaseActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_record_edit, container, false);
        view.findViewById(R.id.explain).setOnClickListener(this);
        view.findViewById(R.id.music).setOnClickListener(this);

        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setBtnRightOnClickListenet(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog == null) dialog = new ProgressDialog(mContext);
                dialog.show();

                FinalVideoCreator creator = new FinalVideoCreator(mContext);
                creator.setCallBack(new CreateCallBack());
                creator.start();
            }
        });

        return view;
    }

    private class CreateCallBack implements FinalVideoCreator.CallBack {

        @Override
        public void onSuccess() {
            mHandler.sendEmptyMessage(EditHandler.CREATE_FINAL_VIDEO_SUCCESS);
        }

        @Override
        public void onFailure(String msg) {
            Message message = mHandler.obtainMessage(EditHandler.SHOW_ERROR_MSG);
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onProgress(float p) {
            Message message = mHandler.obtainMessage(EditHandler.SHOW_PROGRESS_MSG);
            message.obj = p;
            mHandler.sendMessage(message);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.explain:
                getBaseActivity().replaceFragmentWithAnim(new SubtitlesFragment());
                break;
            case R.id.music:
                getBaseActivity().replaceFragmentWithAnim(new MusicFragment());
                break;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBlockInfos = mContext.getBlockInfos();
        if (mBlockInfos.isEmpty()) {
            //TODO exit and show error msg
            return;
        }

        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setBtnBackOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseActivity().replaceFragmentWithAnim(new RecordFragment());
            }
        });

        mAudioPlayer = new AudioPlayer(mContext);
        try {
            mAudioPlayer.preprare();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO exit and show error msg
        }

        ViewGroup squareLayout = (ViewGroup) view.findViewById(R.id.square_layout);

        mVideoPlayer = new MediaPlayer();
        try {
            mVideoPlayer.setDataSource(mContext.getProcessingVideoPath());
            mVideoSurfaceView = new VideoSurfaceView(mContext, mVideoPlayer);
            squareLayout.addView(mVideoSurfaceView);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO exit and show error msg
        }

        mAudioPlayer.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoSurfaceView != null) mVideoSurfaceView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        mAudioPlayer.release();
    }

    private class EditHandler extends Handler {

        public static final int SHOW_ERROR_MSG = 1;
        public static final int SHOW_PROGRESS_MSG = 3;
        public static final int CREATE_FINAL_VIDEO_SUCCESS = 2;


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_ERROR_MSG:
                    //TODO show error dialog
                    JYToast.show(msg.obj.toString());
                    dialog.dismiss();
                    break;
                case SHOW_PROGRESS_MSG:
                    String content = String.format("进度:%3.1f%", msg.obj);
                    dialog.setMessage(content);
                    break;
                case CREATE_FINAL_VIDEO_SUCCESS:
                    JYToast.show("制作成功");
                    getBaseActivity().finish();
                    dialog.dismiss();
                    break;
            }
        }
    }
}
