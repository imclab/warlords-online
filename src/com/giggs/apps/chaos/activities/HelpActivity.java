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

import com.giggs.apps.chaos.R;
import com.giggs.apps.chaos.activities.adapters.HelpSimpleAdapter;
import com.giggs.apps.chaos.utils.ApplicationUtils;
import com.giggs.apps.chaos.utils.MyActivity;

public class HelpActivity extends MyActivity {

	private ArrayList<HashMap<String, String>> mHelpCategoryList;
	private Dialog mHelpDetailsDialog;
	private Runnable mStormEffect;
	private ImageView mStormBackground;

	private OnItemClickListener mOnHelpCategoryClicked = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
			if (position == 0) {
				// go to tutorial
				// TODO
				// Intent i = new Intent(HelpActivity.this, Tutorial.class);
				// startActivity(i);
			} else {
				mHelpDetailsDialog = new Dialog(HelpActivity.this, R.style.FullScreenDialog);
				mHelpDetailsDialog.setContentView(R.layout.dialog_help_details);
				ViewGroup content = (ViewGroup) mHelpDetailsDialog.findViewById(R.id.content);
				switch (position) {
				case 1:
					content.addView(getRow(R.drawable.ti_empty_grass, R.string.grass, R.string.grassDescription));
					content.addView(getRow(R.drawable.ti_castle, R.string.castle, R.string.castleDescription));
					content.addView(getRow(R.drawable.ti_windmills, R.string.windmill, R.string.windmillDescription));
					content.addView(getRow(R.drawable.ti_forest, R.string.forest, R.string.forestDescription));
					content.addView(getRow(R.drawable.ti_fort, R.string.fort, R.string.fortDescription));
					content.addView(getRow(R.drawable.ti_mountains, R.string.mountain, R.string.mountainDescription));
					content.addView(getRow(R.drawable.ti_swamp, R.string.hauntedRuins, R.string.hauntedRuinsDescription));
					break;
				case 2:
					content.addView(getRow(R.drawable.ic_android, R.string.battleTitle1, R.string.battle1));
					content.addView(getRow(R.drawable.ic_double_axe, R.string.battleTitle2, R.string.battle2));
					content.addView(getRow(R.drawable.ti_castle, R.string.battleTitle3, R.string.battle3));
					content.addView(getRow(R.drawable.un_bowmen, R.string.battleTitle4, R.string.battle4));
					break;
				case 3:
					content.addView(getRow(R.drawable.ic_double_axe, R.string.human, R.string.humanHelp));
					content.addView(getRow(R.drawable.un_infantry, R.string.infantry, R.string.infantryHelp));
					content.addView(getRow(R.drawable.un_bowmen, R.string.bowmen, R.string.bowmenHelp));
					content.addView(getRow(R.drawable.un_knight, R.string.knights, R.string.knightsHelp));
					break;
				case 4:
					content.addView(getRow(R.drawable.ic_double_axe, R.string.undead, R.string.undeadHelp));
					content.addView(getRow(R.drawable.un_skeleton, R.string.skeleton, R.string.skeletonHelp));
					content.addView(getRow(R.drawable.un_skbowmen, R.string.skeletonBowmen, R.string.skeletonBowmenHelp));
					content.addView(getRow(R.drawable.un_necromancer, R.string.necromancers, R.string.necromancerHelp));
					break;
				case 5:
					content.addView(getRow(R.drawable.ic_double_axe, R.string.chaos, R.string.chaosHelp));
					content.addView(getRow(R.drawable.un_chaos_warriors, R.string.chaosWarriors,
					        R.string.chaosWarriorsHelp));
					content.addView(getRow(R.drawable.un_chaos_wizards, R.string.chaosWizards,
					        R.string.chaosWizardsHelp));
					content.addView(getRow(R.drawable.un_demon, R.string.chaosDemons, R.string.demonsHelp));
					break;
				case 6:
					content.addView(getRow(R.drawable.ic_double_axe, R.string.orcsArmy, R.string.orcsArmyHelp));
					content.addView(getRow(R.drawable.un_goblin, R.string.goblins, R.string.goblinsHelp));
					content.addView(getRow(R.drawable.un_orc, R.string.orcs, R.string.orcsHelp));
					content.addView(getRow(R.drawable.un_troll, R.string.trolls, R.string.trollsHelp));
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
		addHelpCategory(R.string.terrainTiles, R.drawable.ic_map);
		addHelpCategory(R.string.artOfWar, R.drawable.ic_axe);
		addHelpCategory(R.string.human, R.drawable.un_infantry);
		addHelpCategory(R.string.undead, R.drawable.un_skeleton);
		addHelpCategory(R.string.chaos, R.drawable.un_chaos_wizards);
		addHelpCategory(R.string.orcsArmy, R.drawable.un_orc);
		HelpSimpleAdapter mSchedule = new HelpSimpleAdapter(this.getBaseContext(), mHelpCategoryList,
		        R.layout.help_category, new String[] { "title", "image" }, new int[] { R.id.text, R.id.image });
		helpCategoryListView.setAdapter(mSchedule);
		helpCategoryListView.setOnItemClickListener(mOnHelpCategoryClicked);

		// back button
		findViewById(R.id.backButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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

}
