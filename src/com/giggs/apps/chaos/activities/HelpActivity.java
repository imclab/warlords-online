package com.giggs.apps.chaos.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.giggs.apps.chaos.MyActivity;
import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.adapters.HelpSimpleAdapter;
import com.giggs.apps.chaos.game.data.ArmiesData;
import com.giggs.apps.chaos.game.data.UnitsData;
import com.giggs.apps.chaos.game.model.units.Unit;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.giggs.apps.chaos.utils.MusicManager;

public class HelpActivity extends MyActivity {

    private ArrayList<HashMap<String, String>> mHelpCategoryList;
    private Dialog mHelpDetailsDialog;
    private Runnable mStormEffect;
    private ImageView mStormBackground;

    /**
     * Callbacks
     */
    private OnItemClickListener mOnHelpCategoryClicked = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
            MusicManager.playSound(getApplicationContext(), R.raw.main_button);
            if (position == 0) {
                // go to tutorial
                Intent i = new Intent(HelpActivity.this, TutorialActivity.class);
                startActivity(i);
            } else {
                mHelpDetailsDialog = new Dialog(HelpActivity.this, R.style.FullScreenDialog);
                mHelpDetailsDialog.setContentView(R.layout.dialog_help_details);
                ViewGroup content = (ViewGroup) mHelpDetailsDialog.findViewById(R.id.content);
                switch (position) {
                case 1:
                    content.addView(getRow(R.drawable.ti_empty_grass, R.string.grass, R.string.grass_description));
                    content.addView(getRow(R.drawable.ti_castle, R.string.castle, R.string.castle_description));
                    content.addView(getRow(R.drawable.ti_windmills, R.string.windmill, R.string.windmill_description));
                    content.addView(getRow(R.drawable.ti_forest, R.string.forest, R.string.forest_description));
                    content.addView(getRow(R.drawable.ti_fort, R.string.fort, R.string.fort_description));
                    content.addView(getRow(R.drawable.ti_mountains, R.string.mountain, R.string.mountain_description));
                    content.addView(getRow(R.drawable.ti_water, R.string.lake, R.string.lake_description));
                    break;
                case 2:
                    content.addView(getRow(R.drawable.ic_double_axe, R.string.battle_title1, R.string.battle1));
                    content.addView(getRow(R.drawable.ic_helmet, R.string.battle_title2, R.string.battle2));
                    content.addView(getRow(R.drawable.ti_castle, R.string.battle_title3, R.string.battle3));
                    content.addView(getRow(R.drawable.human_bowman_image, R.string.battle_title4, R.string.battle4));
                    break;
                case 3:
                    content.addView(getRow(ArmiesData.HUMAN.getImage(), ArmiesData.HUMAN.getName(),
                            R.string.human_army_description));
                    for (Unit unit : UnitsData.getUnits(ArmiesData.HUMAN, 0)) {
                        content.addView(getRow(unit));
                    }
                    break;
                case 4:
                    content.addView(getRow(ArmiesData.ORCS.getImage(), ArmiesData.ORCS.getName(),
                            R.string.orcs_army_description));
                    for (Unit unit : UnitsData.getUnits(ArmiesData.ORCS, 0)) {
                        content.addView(getRow(unit));
                    }
                    break;
                case 5:
                    content.addView(getRow(ArmiesData.UNDEAD.getImage(), ArmiesData.UNDEAD.getName(),
                            R.string.undead_army_description));
                    for (Unit unit : UnitsData.getUnits(ArmiesData.UNDEAD, 0)) {
                        content.addView(getRow(unit));
                    }
                    break;
                case 6:
                    content.addView(getRow(ArmiesData.CHAOS.getImage(), ArmiesData.CHAOS.getName(),
                            R.string.chaos_army_description));
                    for (Unit unit : UnitsData.getUnits(ArmiesData.CHAOS, 0)) {
                        content.addView(getRow(unit));
                    }
                    break;
                case 7:
                    content.addView(getRow(ArmiesData.DWARF.getImage(), ArmiesData.DWARF.getName(),
                            R.string.dwarf_army_description));
                    for (Unit unit : UnitsData.getUnits(ArmiesData.DWARF, 0)) {
                        content.addView(getRow(unit));
                    }
                    break;
                }
                mHelpDetailsDialog.show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setupUI();

        mContinueMusic = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // init storm effect
        mStormBackground = (ImageView) findViewById(R.id.stormBackground);
        mStormEffect = ApplicationUtils.addStormBackgroundAtmosphere(mStormBackground);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStormBackground.removeCallbacks(mStormEffect);
        if (mHelpDetailsDialog != null) {
            mHelpDetailsDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void setupUI() {
        // setup help categories
        ListView helpCategoryListView = (ListView) findViewById(R.id.helpCategoryList);
        mHelpCategoryList = new ArrayList<HashMap<String, String>>();
        addHelpCategory(R.string.tutorial, R.drawable.ic_book);
        addHelpCategory(R.string.terrain_tiles, R.drawable.ic_map);
        addHelpCategory(R.string.art_of_war, R.drawable.ic_axe);
        addHelpCategory(R.string.human_army, ArmiesData.HUMAN.getImage());
        addHelpCategory(R.string.orcs_army, ArmiesData.ORCS.getImage());
        addHelpCategory(R.string.undead_army, ArmiesData.UNDEAD.getImage());
        addHelpCategory(R.string.chaos_army, ArmiesData.CHAOS.getImage());
        addHelpCategory(R.string.dwarf_army, ArmiesData.DWARF.getImage());
        HelpSimpleAdapter mSchedule = new HelpSimpleAdapter(this.getBaseContext(), mHelpCategoryList,
                R.layout.help_category, new String[] { "title", "image" }, new int[] { R.id.text, R.id.image });
        helpCategoryListView.setAdapter(mSchedule);
        helpCategoryListView.setOnItemClickListener(mOnHelpCategoryClicked);

        // back button
        findViewById(R.id.backButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.playSound(getApplicationContext(), R.raw.main_button);
                onBackPressed();
            }
        });
    }

    private void addHelpCategory(int title, int image) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("title", getString(title));
        map.put("image", String.valueOf(image));
        mHelpCategoryList.add(map);
    }

    private View getRow(int image, int title, int description) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.help_details_row, null);
        TextView helpTitle = (TextView) view.findViewById(R.id.title);
        helpTitle.setText(title);
        TextView helpContent = (TextView) view.findViewById(R.id.content);
        helpContent.setText(description);
        ImageView helpImage = (ImageView) view.findViewById(R.id.image);
        helpImage.setImageResource(image);
        return view;
    }

    private View getRow(Unit unit) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.help_unit_row, null);

        TextView unitNameTV = (TextView) view.findViewById(R.id.name);
        unitNameTV.setText(unit.getName());
        ImageView unitImage = (ImageView) view.findViewById(R.id.image);
        unitImage.setImageResource(unit.getImage());
        TextView priceTV = (TextView) view.findViewById(R.id.price);
        priceTV.setText("" + unit.getPrice());
        TextView healthTV = (TextView) view.findViewById(R.id.health);
        healthTV.setText(unit.getHealth() + " hp");
        TextView attackTV = (TextView) view.findViewById(R.id.attack);
        attackTV.setText(unit.getAttack() + " (" + unit.getWeaponType().name() + ")");
        TextView defenseTV = (TextView) view.findViewById(R.id.defense);
        defenseTV.setText(unit.getArmor() + " (" + unit.getArmorType().name() + ")");
        TextView descriptionTV = (TextView) view.findViewById(R.id.description);
        String description = getString(getResources().getIdentifier(
                unit.getSpriteName().replace(".png", "") + "_description", "string", getPackageName()));
        descriptionTV.setText(description);
        if (description.isEmpty()) {
            descriptionTV.setVisibility(View.GONE);
        }
        return view;
    }

}
