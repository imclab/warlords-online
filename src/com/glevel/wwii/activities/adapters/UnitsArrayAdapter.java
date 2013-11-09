package com.glevel.wwii.activities.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.glevel.wwii.R;
import com.glevel.wwii.game.GameUtils;
import com.glevel.wwii.game.model.units.Soldier;
import com.glevel.wwii.game.model.units.Unit;
import com.glevel.wwii.game.model.weapons.Weapon;

public class UnitsArrayAdapter extends ArrayAdapter<Unit> {

    protected Context mContext;
    private boolean mIsMyArmy;
    protected List<Unit> mUnits;

    public UnitsArrayAdapter(Context context, int layout, List<Unit> units, boolean isMyArmy) {
        super(context, layout, units);
        this.mContext = context;
        this.mUnits = units;
        this.mIsMyArmy = isMyArmy;
    }

    @Override
    public int getCount() {
        if (mIsMyArmy) {
            // display a fixed number of unit slots
            return GameUtils.MAX_UNIT_PER_ARMY;
        } else {
            return mUnits.size();
        }
    }

    public static class ViewHolder {
        protected ImageView unitImage;
        protected TextView unitName;
        private TextView unitExperience;
        private TextView unitPriceFrags;
        private ViewGroup unitMainWeapon;
        private TextView unitMainWeaponName;
        private TextView unitMainWeaponAP;
        private TextView unitMainWeaponAT;
        private ViewGroup unitSecondaryWeapon;
        private TextView unitSecondaryWeaponName;
        private TextView unitSecondaryWeaponAP;
        private TextView unitSecondaryWeaponAT;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        // check if the view is null then if so inflate it.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.army_list_item, null);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag(R.string.viewholder);
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.unitImage = (ImageView) view.findViewById(R.id.unitImage);
            viewHolder.unitName = (TextView) view.findViewById(R.id.unitName);
            viewHolder.unitExperience = (TextView) view.findViewById(R.id.unitExperience);
            viewHolder.unitPriceFrags = (TextView) view.findViewById(R.id.unitPrice);
            viewHolder.unitMainWeapon = (ViewGroup) view.findViewById(R.id.unitMainWeapon);
            viewHolder.unitMainWeaponName = (TextView) view.findViewById(R.id.unitMainWeaponName);
            viewHolder.unitMainWeaponAP = (TextView) view.findViewById(R.id.unitMainWeaponAP);
            viewHolder.unitMainWeaponAT = (TextView) view.findViewById(R.id.unitMainWeaponAT);
            viewHolder.unitSecondaryWeapon = (ViewGroup) view.findViewById(R.id.unitSecondaryWeapon);
            viewHolder.unitSecondaryWeaponName = (TextView) view.findViewById(R.id.unitSecondaryWeaponName);
            viewHolder.unitSecondaryWeaponAP = (TextView) view.findViewById(R.id.unitSecondaryWeaponAP);
            viewHolder.unitSecondaryWeaponAT = (TextView) view.findViewById(R.id.unitSecondaryWeaponAT);
            view.setTag(R.string.viewholder, viewHolder);
        }

        if (position < mUnits.size()) {
            // fill view with unit characteristics
            Unit unit = mUnits.get(position);

            // image
            viewHolder.unitImage.setVisibility(View.VISIBLE);
            viewHolder.unitImage.setImageResource(unit.getImage());

            // name
            viewHolder.unitName.setVisibility(View.VISIBLE);
            if (mIsMyArmy && unit instanceof Soldier) {
                // display real name
                viewHolder.unitName.setText(((Soldier) unit).getRealName());
            } else {
                viewHolder.unitName.setText(unit.getName());
            }

            // experience
            viewHolder.unitExperience.setVisibility(View.VISIBLE);
            viewHolder.unitExperience.setText(unit.getExperience().name());
            viewHolder.unitExperience.setTextColor(mContext.getResources().getColor(unit.getExperience().getColor()));

            // weapons
            // main weapon
            viewHolder.unitMainWeapon.setVisibility(View.VISIBLE);
            Weapon mainWeapon = unit.getWeapons().get(0);
            viewHolder.unitMainWeaponName.setText(mainWeapon.getName());
            viewHolder.unitMainWeaponName.setCompoundDrawablesWithIntrinsicBounds(mainWeapon.getImage(), 0, 0, 0);
            viewHolder.unitMainWeaponAP.setBackgroundResource(mainWeapon.getAPColorEfficiency());
            viewHolder.unitMainWeaponAT.setBackgroundResource(mainWeapon.getATColorEfficiency());

            // secondary weapon
            if (unit.getWeapons().size() > 1) {
                viewHolder.unitSecondaryWeapon.setVisibility(View.VISIBLE);
                Weapon secondaryWeapon = unit.getWeapons().get(1);
                viewHolder.unitSecondaryWeaponName.setText(secondaryWeapon.getName());
                viewHolder.unitSecondaryWeaponName.setCompoundDrawablesWithIntrinsicBounds(secondaryWeapon.getImage(),
                        0, 0, 0);
                viewHolder.unitSecondaryWeaponAP.setBackgroundResource(secondaryWeapon.getAPColorEfficiency());
                viewHolder.unitSecondaryWeaponAT.setBackgroundResource(secondaryWeapon.getATColorEfficiency());
            } else {
                viewHolder.unitSecondaryWeapon.setVisibility(View.GONE);
            }

            viewHolder.unitPriceFrags.setVisibility(View.VISIBLE);
            if (mIsMyArmy) {
                // frags
                viewHolder.unitPriceFrags.setText(mContext.getString(R.string.frags_number, unit.getFrags()));
                viewHolder.unitPriceFrags.setTextColor(mContext.getResources().getColor(android.R.color.white));
            } else {
                // price
                viewHolder.unitPriceFrags
                        .setText(mContext.getString(R.string.points, unit.getRealSellPrice(mIsMyArmy)));
                viewHolder.unitPriceFrags.setTextColor(mContext.getResources().getColor(R.color.requisitionPoints));
            }

        } else {
            // hide views to have an empty slot
            viewHolder.unitImage.setVisibility(View.INVISIBLE);
            viewHolder.unitName.setVisibility(View.INVISIBLE);
            viewHolder.unitExperience.setVisibility(View.INVISIBLE);
            viewHolder.unitPriceFrags.setVisibility(View.INVISIBLE);
            viewHolder.unitMainWeapon.setVisibility(View.INVISIBLE);
            viewHolder.unitSecondaryWeapon.setVisibility(View.INVISIBLE);
        }

        return view;
    }

}
