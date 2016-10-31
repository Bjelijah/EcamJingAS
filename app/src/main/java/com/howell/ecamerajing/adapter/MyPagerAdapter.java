package com.howell.ecamerajing.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class MyPagerAdapter extends FragmentPagerAdapter {
	
	private List<Fragment> fragmentList;
	private String[] titles;

	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}
	
	public MyPagerAdapter(FragmentManager fm,List<Fragment> fragmentList,String[] titles) {
		super(fm);
		// TODO Auto-generated constructor stub
		this.fragmentList = fragmentList;
		this.titles = titles;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		return fragmentList.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return titles.length;
	}

}
