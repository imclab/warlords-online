package com.giggs.apps.chaos.activities.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.giggs.apps.chaos.MyApplication;
import com.giggs.apps.chaos.R;

public class HelpSimpleAdapter extends SimpleAdapter {

	private Context mContext;
	private ArrayList<HashMap<String, String>> mHelpCategoryList;
	private int mLayoutId;

	public HelpSimpleAdapter(Context context, ArrayList<HashMap<String, String>> listHelpCategories, int categoryId,
	        String[] strings, int[] is) {
		super(context, listHelpCategories, categoryId, strings, is);
		this.mContext = context;
		this.mHelpCategoryList = listHelpCategories;
		this.mLayoutId = categoryId;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		View mView = v;
		if (mView == null) {
			LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mView = vi.inflate(mLayoutId, null);
		}

		TextView text = (TextView) mView.findViewById(R.id.text);
		text.setTypeface(MyApplication.FONTS.main);
		text.setText(mHelpCategoryList.get(position).get("title"));
		text.setCompoundDrawablesWithIntrinsicBounds(Integer.parseInt(mHelpCategoryList.get(position).get("image")), 0,
		        0, 0);
		return mView;
	}

}
