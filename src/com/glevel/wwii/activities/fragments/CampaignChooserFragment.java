package com.glevel.wwii.activities.fragments;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.glevel.wwii.R;
import com.glevel.wwii.activities.CampaignActivity;
import com.glevel.wwii.activities.GameActivity;
import com.glevel.wwii.activities.adapters.CampaignListAdapter;
import com.glevel.wwii.database.DatabaseHelper;
import com.glevel.wwii.database.dao.BattleDao;
import com.glevel.wwii.game.data.CampaignsData.Campaigns;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.Campaign;
import com.glevel.wwii.game.model.Player;

public class CampaignChooserFragment extends DialogFragment {

    public static final String PARENT_CURSOR_COLUMN_ID = "_id", PARENT_CURSOR_COLUMN_TITLE = "title";
    public static final int NEW_CAMPAIGN_CATEGORY_ID = 0, LOAD_CAMPAIGN_CATEGORY_ID = 1;
    private ExpandableListView mCampaignListView;
    private CampaignListAdapter mAdapter;
    private DatabaseHelper mDbHelper;
    private List<Campaign> mSavedCampaignList;

    /**
     * Callbacks
     */
    private final ExpandableListView.OnChildClickListener mOnItemClickedListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
            if (view.isEnabled()) {
                if (groupPosition == NEW_CAMPAIGN_CATEGORY_ID) {
                    loadCampaign(new Campaign(Campaigns.values()[childPosition]));
                } else {
                    loadCampaign(mSavedCampaignList.get(childPosition));
                }
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0); // remove title from dialog fragment

        mDbHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null)
            return;

        // set the animations to use on showing and hiding the dialog
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSavedCampaignList = mDbHelper.getCampaignDao().get(null, null, null, null);
        mAdapter = new CampaignListAdapter(getActivity(), mSavedCampaignList, new int[] { R.string.new_campaign,
                R.string.load_campaign });
        mCampaignListView.setAdapter(mAdapter);

        // the most dirty hack on earth in order to expand all the groups (buggy
        // when called at the same time (!))
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCampaignListView.expandGroup(0);
            }
        }, 100);

        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCampaignListView.expandGroup(1);
            }
        }, 200);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.campaign_chooser_fragment, container, false);

        // expandable friends list view
        mCampaignListView = (ExpandableListView) layout.findViewById(R.id.campaignList);
        mCampaignListView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        mCampaignListView.setOnChildClickListener(mOnItemClickedListener);
        // disable group collapsing
        mCampaignListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // do nothing
                return true;
            }
        });
        // remove group icons
        mCampaignListView.setGroupIndicator(null);

        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // null out the group cursor. This will cause the group cursor and all
        // of the child cursors to be closed.
        mAdapter = null;
    }

    private void loadCampaign(Campaign campaign) {
        List<Battle> lstSavedBattleForCampaign = mDbHelper.getBattleDao().get(BattleDao.CAMPAIGN_ID + "=?",
                new String[] { "" + campaign.getCampaignId() }, null, null);
        Intent intent;
        Bundle args = new Bundle();
        if (lstSavedBattleForCampaign.size() > 0) {
            // load saved battle
            intent = new Intent(getActivity(), GameActivity.class);
            args.putLong("game_id", lstSavedBattleForCampaign.get(0).getId());
        } else {
            if (campaign.getId() == 0L) {
                // if new campaign
                // init player
                campaign.setPlayer(new Player(campaign.getArmy()));
                // save it to the database
                long id = mDbHelper.getCampaignDao().save(campaign);
                campaign.setId(id);
            }
            // go to campaign screen
            intent = new Intent(getActivity(), CampaignActivity.class);
            args.putLong("campaign_id", campaign.getId());
        }
        intent.putExtras(args);
        startActivity(intent);
        getActivity().finish();
    }

}
