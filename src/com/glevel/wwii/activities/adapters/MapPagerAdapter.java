package com.glevel.wwii.activities.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.glevel.wwii.R;
import com.glevel.wwii.game.data.BattlesData;

public class MapPagerAdapter extends PagerAdapter {

    private OnClickListener mMapClickedListener;

    public MapPagerAdapter(OnClickListener onMapSelectedListener) {
        this.mMapClickedListener = onMapSelectedListener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final BattlesData map = BattlesData.values()[position];

        LayoutInflater inflater = (LayoutInflater) container.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.map_chooser_item, null);

        TextView mapName = (TextView) layout.findViewById(R.id.mapName);
        mapName.setText(map.getName());

        ImageView mapImage = (ImageView) layout.findViewById(R.id.mapImage);
        mapImage.setImageResource(map.getImage());

        layout.setOnClickListener(mMapClickedListener);

        // remove default sound effect on click
        layout.setSoundEffectsEnabled(false);

        ((ViewPager) container).addView(layout);
        return layout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return BattlesData.values().length;
    }

    @Override
    public float getPageWidth(int position) {
        return 0.5f;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
        ((ViewPager) collection).removeView((View) view);
    }

}
