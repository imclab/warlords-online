package com.glevel.wwii.activities.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.glevel.wwii.R;
import com.glevel.wwii.game.model.units.Unit;

public class GameReportUnitsArrayAdapter extends UnitsArrayAdapter {

    public GameReportUnitsArrayAdapter(Context context, int textViewResourceId, List<Unit> units, boolean isMyArmy) {
        super(context, textViewResourceId, units, isMyArmy);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ViewHolder viewHolder = (ViewHolder) view.getTag(R.string.viewholder);
        Unit unit = mUnits.get(position);

        // display units health at the end of the battle
        viewHolder.unitName.setTextColor(mContext.getResources().getColor(unit.getHealth().getColor()));

        // view is not clickable
        view.setEnabled(false);

        return view;
    }

    @Override
    public int getCount() {
        return mUnits.size();
    }

}
