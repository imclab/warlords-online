package com.glevel.wwii.database.dao;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.glevel.wwii.database.DatabaseHelper;
import com.glevel.wwii.database.Repository;
import com.glevel.wwii.game.SaveGameHelper;
import com.glevel.wwii.game.data.CampaignsData.Campaigns;
import com.glevel.wwii.game.model.Campaign;

public class CampaignDao extends Repository<Campaign> {

    public static final String TABLE_NAME = "campaign";
    public static final String ID = "_id";
    public static final String CAMPAIGN_ID = "campaign_id";
    public static final String PLAYER = "player";
    public static final String OPERATIONS = "operations";

    public CampaignDao(DatabaseHelper dataBaseHelper) {
        super(dataBaseHelper);
    }

    @Override
    public Campaign getById(long id) {
        this.openDatabase();
        Cursor cursor = mDatabase.query(TABLE_NAME, null, ID + "=" + id, null, null, null, null);
        return convertCursorToSingleObject(cursor);
    }

    @Override
    public List<Campaign> get(String selection, String[] selectionArgs, String orderBy, String limit) {
        this.openDatabase();
        Cursor cursor = mDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, orderBy, limit);
        return convertCursorToObjectList(cursor);
    }

    @Override
    public long save(Campaign entity) {
        long rowId;
        this.openDatabase();
        if (entity.getId() > 0) {
            rowId = entity.getId();
            mDatabase.update(TABLE_NAME, getContentValues(entity), ID + "=" + entity.getId(), null);
        } else {
            rowId = mDatabase.insert(TABLE_NAME, null, getContentValues(entity));
        }
        this.closeDatabase();
        return rowId;
    }

    @Override
    public void delete(String selection, String[] selectionArgs) {
        this.openDatabase();
        mDatabase.delete(TABLE_NAME, selection, selectionArgs);
        this.closeDatabase();
    }

    @Override
    public Campaign convertCursorRowToObject(Cursor c) {
        DatabaseUtils.dumpCursor(c);
        Campaign entity = new Campaign(Campaigns.values()[c.getInt(1)]);
        entity.setId(c.getLong(0));
        entity.setPlayer(SaveGameHelper.getPlayerFromLoadGame(c.getBlob(2)));
        entity.setOperations(SaveGameHelper.getOperationsListFromLoadGame(c.getBlob(3)));
        return entity;
    }

    @Override
    public ContentValues getContentValues(Campaign entity) {
        ContentValues args = new ContentValues();
        args.put(ID, (entity.getId() == 0 ? null : entity.getId()));
        args.put(CAMPAIGN_ID, entity.getCampaignId());
        args.put(PLAYER, SaveGameHelper.toByte(entity.getPlayer()).toByteArray());
        args.put(OPERATIONS, SaveGameHelper.toByte(entity.getOperations()).toByteArray());
        return args;
    }

}
