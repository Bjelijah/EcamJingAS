package com.howell.ecamerajing.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.astuetz.PagerSlidingTabStrip;
import com.howell.ecamerajing.adapter.MyPagerAdapter;
import com.howell.ecamjing.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 霍之昊 
 *
 * 类说明：主界面，包含了发现界面和我的界面
 */
public class HomeFragmentActivity extends FragmentActivity {
	
	/**
	 * PagerSlidingTabStrip的实例
	 */
	private PagerSlidingTabStrip tabs;
	/**
	 * 获取当前屏幕的密度
	 */
	private DisplayMetrics dm;
	private ImageButton ibutton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		init();
		setTabsValue();
		ibutton = (ImageButton) findViewById(R.id.title_setting);
		ibutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("123", "title setting on click");
				Intent intent = new Intent(HomeFragmentActivity.this,SettingActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private List<Fragment> getFragmentList(){
		List<Fragment> list = new ArrayList<Fragment>();
		list.add(new FoundFragment());
		list.add(new MediaFragment());
		return list;
	}
	
	private void init(){
		dm = getResources().getDisplayMetrics();
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		String[] titles = { "发现", "我的" };
		pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),getFragmentList(),titles));
		tabs.setViewPager(pager);
	}
	
	/**
	 * 对PagerSlidingTabStrip的各项属性进行赋值。
	 */
	private void setTabsValue() {
		// 设置Tab是自动填充满屏幕的
		tabs.setShouldExpand(true);
		// 设置Tab的分割线是透明的
		tabs.setDividerColor(Color.TRANSPARENT);
		// 设置Tab底部线的高度
		tabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, dm));
		// 设置Tab Indicator的高度
		tabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, dm));
		// 设置Tab标题文字的大小
		tabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 20, dm));
		tabs.setTextColor(getResources().getColor(R.color.white));
		// 设置Tab Indicator的颜色
		tabs.setIndicatorColor(getResources().getColor(R.color.blue));
		// 设置选中Tab文字的颜色 (这是我自定义的一个方法)
		tabs.setSelectedTextColor(getResources().getColor(R.color.blue));
//		tabs.setSelectedTextColor(Color.parseColor("#45c01a"));
		tabs.setTabPaddingLeftRight(0);
		// 取消点击Tab时的背景色
		tabs.setTabBackground(0);
		tabs.setPadding(0, 20, 0, 20);
//		tabs.setShouldExpand(true);
	}

}
