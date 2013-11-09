package com.giggs.apps.chaos.activities.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.giggs.apps.chaos.game.model.units.Unit;
import com.glevel.wwii.R;

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
