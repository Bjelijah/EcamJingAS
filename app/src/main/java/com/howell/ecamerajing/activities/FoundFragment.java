package com.howell.ecamerajing.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamjing.R;

/**
 * @author 霍之昊 
 *
 * 类说明：发现界面
 */
public class FoundFragment extends Fragment implements OnClickListener,Constable{

	private LinearLayout mPreview,mPlayback,mSetting,mAdd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.found_fragment, null);
		mPreview = (LinearLayout)view.findViewById(R.id.found_fragment_preview);
		mPreview.setOnClickListener(this);
		mPlayback = (LinearLayout)view.findViewById(R.id.found_fragment_playback);
		mPlayback.setOnClickListener(this);
		mSetting = (LinearLayout)view.findViewById(R.id.found_fragment_setting);
		mSetting.setOnClickListener(this);
		mAdd = (LinearLayout)view.findViewById(R.id.found_fragment_add);
		mAdd.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.found_fragment_preview:
			Intent intent = new Intent(getActivity(),PlayActivity.class);
			intent.putExtra("playMode", PLAY_MODE_REVIEW);
			startActivity(intent);
			break;
		case R.id.found_fragment_playback:
			intent = new Intent(getActivity(),PlayBackListActiviy.class);
			startActivity(intent);
			break;
		case R.id.found_fragment_setting:
			intent = new Intent(getActivity(),SettingActivity.class);
			startActivity(intent);
			break;
		case R.id.found_fragment_add:
			intent = new Intent(getActivity(),CourseActivity.class);
			startActivity(intent);
		default:

			break;
		}
	}
}
