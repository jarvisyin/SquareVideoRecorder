package com.jarvisyin.recorder.Home.VideoRecord.Edit;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.jarvisyin.recorder.BuildConfig;
import com.jarvisyin.recorder.Buz.JYImageLoader;
import com.jarvisyin.recorder.Common.Utils.JYToast;
import com.jarvisyin.recorder.Home.VideoRecord.Common.VideoBaseFragment;
import com.jarvisyin.recorder.Home.VideoRecord.Common.Widget.ActionBar;
import com.jarvisyin.recorder.Common.Widget.Dialog.ProgressDialog;
import com.jarvisyin.recorder.Home.VideoRecord.BlockInfo;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;
import com.jarvisyin.recorder.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubtitlesFragment extends VideoBaseFragment {

    private static final boolean VERBOSE = true && BuildConfig.DEBUG;           // lots of logging
    private static final String TAG = "PhotoAlbumActivity";

    private VideoRecordActivity mContext;
    private RecyclerView mRecyclerView;
    private List<String> mExplains;
    private List<BlockInfo> mBlockInfos;
    private SaveAsyncTask mSaveAsyncTask;

    public SubtitlesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        mContext = (VideoRecordActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_record_subtitles, container, false);

        mBlockInfos = mContext.getBlockInfos();
        int length = mBlockInfos.size();
        mExplains = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            BlockInfo blockInfo = mBlockInfos.get(i);
            mExplains.add(blockInfo.getExplain());
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(new RecyclerViewAdapter(getBaseActivity(),mBlockInfos, mExplains));

        ActionBar mActionBar = (ActionBar) view.findViewById(R.id.action_bar);
        mActionBar.setBtnRightOnClickListenet(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveAsyncTask = new SaveAsyncTask(mContext);
                mSaveAsyncTask.execute(mExplains, mBlockInfos);
            }
        });

        mActionBar.setBtnBackOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseActivity().replaceFragmentWithAnim(new EditFragment());
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSaveAsyncTask != null) mSaveAsyncTask.cancel(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    /**
     * 1.循环 比较 原字幕 与 现字幕 是否相同,
     * 2.如果不相同更新BlockInFo的explain string
     * 3.生成字幕图片并保存到外存
     * 4.更新explain file
     * 5.退出
     *
     * 在线程中完成
     */
    private static class SaveAsyncTask extends AsyncTask<Object, Void, String> {

        private final VideoRecordActivity mContext;
        private ProgressDialog dialog;

        public SaveAsyncTask(VideoRecordActivity context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            if (dialog == null) {
                dialog = new ProgressDialog(mContext);
            }
            dialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                List<String> mExplains = (List<String>) params[0];
                List<BlockInfo> mBlockInfos = (List<BlockInfo>) params[1];

                //1.比较 原字幕 与 现字幕 是否相同
                for (int i = 0; i < mBlockInfos.size(); i++) {
                    BlockInfo block = mBlockInfos.get(i);
                    String preExplain = block.getExplain();
                    String curExplain = mExplains.get(i);

                    //2.如果不相同更新BlockInFo的explain string
                    if (!isEquals(preExplain, curExplain)) {
                        if (curExplain != null) curExplain = curExplain.trim();
                        block.setExplain(curExplain);

                        //3.生成字幕图片并保存到外存
                        String explain = block.getExplain();
                        if (TextUtils.isEmpty(explain)) {
                            //4.更新explain file
                            block.setExplainFile(null);
                        } else {
                            Bitmap bitmap = getTextBitmap(explain);
                            //4.更新explain file
                            File explainFile = new File(block.getParentFile(), "explain.png");
                            Utils.saveImage(explainFile.getName(), explainFile.getParent(), bitmap);
                            block.setExplainFile(explainFile);
                            bitmap.recycle();
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            if (TextUtils.isEmpty(s)) {
                //5.退出
                mContext.replaceFragmentWithAnim(new EditFragment());
            } else {
                JYToast.show("编辑字幕失败");
            }
        }

        public Bitmap getTextBitmap(String text) {
            Bitmap bitmap = Bitmap.createBitmap(600, 85, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextSize(69);
            paint.setColor(0xffffffff);
            canvas.drawText(text, 0, 70, paint);
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
            return bitmap;
        }

        private boolean isEquals(String preExplain, String curExplain) {
            if (preExplain == null && curExplain == null) {
                return true;
            } else if (preExplain != null && curExplain != null) {
                if (preExplain.trim().equals(curExplain.trim())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

        private final Context mContext;
        private final List<String> mExplains;
        private final List<BlockInfo> mBlockInfos;

        public RecyclerViewAdapter(Context context, List<BlockInfo> blockInfos, List<String> explains) {
            mExplains = explains;
            mBlockInfos = blockInfos;
            mContext = context;
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(LayoutInflater.from(mContext).inflate(R.layout.fragment_video_record_subtitles_list_item, null));
            recyclerViewHolder.editText.addTextChangedListener(new EditChangedListener(recyclerViewHolder.editText, mExplains));
            return recyclerViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, int position) {
            String explains = mExplains.get(position);
            holder.editText.setText(explains);
            holder.editText.setTag(position);
            JYImageLoader.getInstance().displayImage("file://" + mBlockInfos.get(position).getCoverFile().getPath(),holder.imageView);

        }

        @Override
        public int getItemCount() {
            return mExplains.size();
        }
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final EditText editText;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            editText = (EditText) itemView.findViewById(R.id.edit_text);
        }
    }

    public static class EditChangedListener implements TextWatcher {
        private final List<String> mExplains;
        private final EditText mEditText;

        public EditChangedListener(EditText editText, List<String> explains) {
            this.mEditText = editText;
            this.mExplains = explains;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            Object obj = mEditText.getTag();
            if (obj == null) return;
            int position = Integer.valueOf(mEditText.getTag().toString());
            mExplains.set(position, mEditText.getText().toString());
        }
    }
}
