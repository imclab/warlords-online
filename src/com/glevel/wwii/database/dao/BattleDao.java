package com.glevel.wwii.database.dao;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.glevel.wwii.database.DatabaseHelper;
import com.glevel.wwii.database.Repository;
import com.glevel.wwii.game.SaveGameHelper;
import com.glevel.wwii.game.data.BattlesData;
import com.glevel.wwii.game.model.Battle;
import com.glevel.wwii.game.model.Battle.Phase;

public class BattleDao extends Repository<Battle> {

    public static final String TABLE_NAME = "battle";
    public static final String ID = "_id";
    public static final String BATTLE_ID = "battle_id";
    public static final String PLAYERS = "players";
    public static final String CAMPAIGN_ID = "campaign_id";
    public static final String PHASE = "phase";

    public BattleDao(DatabaseHelper dataBaseHelper) {
        super(dataBaseHelper);
    }

    @Override
    public Battle getById(long id) {
        this.openDatabase();
        Cursor cursor = mDatabase.query(TABLE_NAME, null, ID + "=" + id, null, null, null, null);
        return convertCursorToSingleObject(cursor);
    }

    @Override
    public List<Battle> get(String selection, String[] selectionArgs, String orderBy, String limit) {
        this.openDatabase();
        Cursor cursor = mDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, orderBy, limit);
        return convertCursorToObjectList(cursor);
    }

    @Override
    public long save(Battle entity) {
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
    public Battle convertCursorRowToObject(Cursor c) {
        Battle entity = new Battle(BattlesData.values()[c.getInt(1)]);
        entity.setId(c.getLong(0));
        entity.setPlayers(SaveGameHelper.getPlayersFromLoadGame(c.getBlob(2)));
        entity.setCampaignId(c.getInt(3));
        entity.setPhase(Phase.values()[c.getInt(4)]);
        return entity;
    }

    @Override
    public ContentValues getContentValues(Battle entity) {
        ContentValues args = new ContentValues();
        args.put(ID, (entity.getId() == 0 ? null : entity.getId()));
        args.put(BATTLE_ID, entity.getBattleId());
        args.put(PLAYERS, SaveGameHelper.toByte(entity.getPlayers()).toByteArray());
        args.put(CAMPAIGN_ID, entity.getCampaignId());
        args.put(PHASE, entity.getPhase().ordinal());
        return args;
    }

}
