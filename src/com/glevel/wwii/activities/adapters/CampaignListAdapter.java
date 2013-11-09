package com.glevel.wwii.activities.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.glevel.wwii.R;
import com.glevel.wwii.activities.fragments.CampaignChooserFragment;
import com.glevel.wwii.game.data.CampaignsData.Campaigns;
import com.glevel.wwii.game.model.Campaign;

public class CampaignListAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater mInflater;
    private List<Campaign> mSavedCampaigns;
    private int[] mHeaderLabels;

    public CampaignListAdapter(FragmentActivity activity, List<Campaign> savedCampaigns, int[] headerLabels) {
        mSavedCampaigns = savedCampaigns;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHeaderLabels = headerLabels;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition == CampaignChooserFragment.NEW_CAMPAIGN_CATEGORY_ID) {
            return new Campaign(Campaigns.values()[childPosition]);
        } else if (groupPosition == CampaignChooserFragment.LOAD_CAMPAIGN_CATEGORY_ID
                && childPosition < mSavedCampaigns.size()) {
            return mSavedCampaigns.get(childPosition);
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.campaign_list_child, null);
        }

        TextView campaignTitle = (TextView) convertView.findViewById(R.id.text);

        Object child = getChild(groupPosition, childPosition);
        if (getChild(groupPosition, childPosition) == null) {
            // if empty view
            convertView.setEnabled(false);
            campaignTitle.setText(R.string.no_saved_campaigns);
            campaignTitle.setBackgroundColor(Color.BLACK);
            campaignTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            convertView.setEnabled(true);
            Campaign campaign = (Campaign) child;
            campaignTitle.setText(campaign.getName());
            campaignTitle.setCompoundDrawablesWithIntrinsicBounds(campaign.getArmy().getFlagImage(), 0, 0, 0);
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition == CampaignChooserFragment.NEW_CAMPAIGN_CATEGORY_ID) {
            return Campaigns.values().length;
        } else {
            // return 1 if empty (used for the empty view)
            return Math.max(1, mSavedCampaigns.size());
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mHeaderLabels[groupPosition];
    }

    @Override
    public int getGroupCount() {
        return mHeaderLabels.length;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.campaign_list_header, null);
        }

        // set header title
        TextView headerTitle = (TextView) convertView.findViewById(R.id.text);
        headerTitle.setText(mHeaderLabels[groupPosition]);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
