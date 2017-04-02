package com.jarvisyin.recorder.Home.VideoStore;


import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jarvisyin.recorder.Buz.FileManager;
import com.jarvisyin.recorder.Buz.JYImageLoader;
import com.jarvisyin.recorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.recorder.Common.Utils.DisplayUtils;

import com.jarvisyin.recorder.Common.Widget.ActionBar.ActionBar;
import com.jarvisyin.recorder.Common.Widget.ProgressView;
import com.jarvisyin.recorder.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoStoreFragment extends BaseFragment implements View.OnClickListener {

    public Boolean isChoiceMode = false;

    private ProgressView mProgressView;
    private RecyclerView mRecyclerView;
    private View btnDelete, btnAddVideo;
    private RecyclerViewAdapter mAadapter;
    private final List<VideoItem> videoItems = new ArrayList<>();

    public VideoStoreFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_base_index, container, false);

        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setBtnRightOnClickListenet(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChoiceMode = !isChoiceMode;
                if (isChoiceMode) {
                    btnDelete.setVisibility(View.VISIBLE);
                    btnAddVideo.setVisibility(View.GONE);
                    ((TextView) v).setText("取消");
                } else {
                    btnDelete.setVisibility(View.GONE);
                    btnAddVideo.setVisibility(View.VISIBLE);
                    ((TextView) v).setText("选择");
                }
            }
        });

        btnDelete = view.findViewById(R.id.btn_delete);
        btnAddVideo = view.findViewById(R.id.btn_add_video);
        btnDelete.setOnClickListener(this);
        btnAddVideo.setOnClickListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2));
        mRecyclerView.addItemDecoration(new RecyclerViewItemDecoration());
        mRecyclerView.setAdapter(mAadapter = new RecyclerViewAdapter(videoItems));

        mProgressView = (ProgressView) view.findViewById(R.id.progress_view);

        return view;
    }

    private GetVideoList getVideoList;

    @Override
    public void onResume() {
        super.onResume();
        getVideoList = new GetVideoList();
        getVideoList.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        getVideoList.cancel(true);
    }

    private class GetVideoList extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            videoItems.clear();
            videoItems.addAll(getVideoItems());
            return null;
        }

        /**
         * 运行在ui线程中，在doInBackground()执行完毕后执行
         */
        @Override
        protected void onPostExecute(Integer integer) {
            mAadapter.notifyDataSetChanged();
            if (videoItems.isEmpty())
                mProgressView.setMsg("视频库没有视频");
            else
                mProgressView.hide();
        }
    }


    public List<VideoItem> getVideoItems() {
        File file = FileManager.getVideoStorePath();
        File[] files = file.listFiles();
        List<VideoItem> videoItems = new ArrayList<>();
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        for (File f : files) {
            if (!f.isFile()) continue;

            VideoItem videoItem = new VideoItem();
            String videoPath = f.getPath();
            videoItem.videoPath = videoPath;
            videoItem.imagePath = videoPath.substring(0, videoPath.length() - 4) + ".png";
            String name = f.getName();
            String[] strs = name.split("_");
            if (strs.length != 2) continue;
            if (!name.contains(".mp4")) continue;
            try {
                retr.setDataSource(videoPath);
                int duration = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
                videoItem.time = String.format("%d:%02d", duration / 60, duration % 60);
            } catch (Exception e) {
                e.printStackTrace();
                videoItem.time = "- -";
            }

            videoItems.add(videoItem);
        }
        return videoItems;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_delete:
                Map<String, VideoItem> map = mAadapter.mSelectedMap;
                for (String key : map.keySet()) {
                    new File(key).delete();
                    String str = key.substring(0, key.length() - 4) + ".png";
                    new File(str).delete();
                    videoItems.remove(map.get(key));
                }
                mAadapter.notifyDataSetChanged();
                map.clear();
                if (videoItems.isEmpty()) {
                    mProgressView.setMsg("没有视频");
                }

                break;
            case R.id.btn_add_video:
                //mActivity.startActivity(new Intent(mActivity, VideoImportActivity.class));
                break;
        }
    }

    private class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
        private int offset;

        private RecyclerViewItemDecoration() {
            offset = DisplayUtils.dip2px(mActivity, 10);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
            int itemCount = parent.getAdapter().getItemCount();
            int bottom = 0;

            if (itemCount % 2 == 0) {
                if (itemCount - 1 == position || (itemCount - 2) == position) {
                    bottom = offset;
                }
            } else {
                if (itemCount - 1 == position) {
                    bottom = offset;
                }
            }

            if (position % 2 == 0) {
                outRect.set(offset, offset, offset / 2, bottom);
            } else {
                outRect.set(offset / 2, offset, offset, bottom);
            }
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<Holder> {

        private final List<VideoItem> mVideoItems;
        private final Map<String, VideoItem> mSelectedMap = new HashMap<>();

        public RecyclerViewAdapter(List<VideoItem> videoItems) {
            this.mVideoItems = videoItems;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_video_base_index_list_item, null);
            Holder holder = new Holder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            VideoItem videoItem = videoItems.get(position);
            holder.imageView.setTag(R.id.item_id, videoItem);
            if (isChoiceMode && mSelectedMap.get(videoItem.videoPath) != null) {
                holder.bg.setVisibility(View.VISIBLE);
            } else {
                holder.bg.setVisibility(View.GONE);
            }
            holder.time.setText(videoItem.time);
            JYImageLoader.getInstance().displayImage("file:/" + videoItem.imagePath, holder.imageView);
        }

        @Override
        public int getItemCount() {
            return mVideoItems.size();
        }
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView time;
        private ImageView imageView;
        private View bg;

        public Holder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.time);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            bg = itemView.findViewById(R.id.bg);

            imageView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            VideoItem item = (VideoItem) v.getTag(R.id.item_id);

            if (isChoiceMode) {
                if (mAadapter.mSelectedMap.get(item.videoPath) == null) {
                    mAadapter.mSelectedMap.put(item.videoPath, item);
                    bg.setVisibility(View.VISIBLE);
                } else {
                    mAadapter.mSelectedMap.remove(item.videoPath);
                    bg.setVisibility(View.GONE);
                }
            } else {
                VideoViewFragment videoViewFragment = VideoViewFragment.newInstance(item.videoPath);
                mActivity.addFragmentWithAnim(videoViewFragment);
            }

        }

    }
}
