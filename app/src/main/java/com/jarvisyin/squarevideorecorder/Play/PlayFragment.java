package com.jarvisyin.squarevideorecorder.Play;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jarvisyin.squarevideorecorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.squarevideorecorder.R;

/**
 * Created by Jarvis.
 */
public class PlayFragment extends BaseFragment {
    public static final String TAG = PlayFragment.class.getName();


    private String par1;

    private OnFragmentInteractionListener mListener;

    public PlayFragment() {

    }

    public static PlayFragment newInstance(String par1) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString("par1", par1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            par1 = getArguments().getString("par1");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onButtonPressed(String TAG) {
        if (mListener != null) {
            mListener.onFragmentInteraction(TAG);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String TAG);
    }
}
