package com.giggs.apps.chaos.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.giggs.apps.chaos.database.DatabaseHelper;
import com.giggs.apps.chaos.game.model.Battle;
import com.giggs.apps.chaos.game.multiplayer.Message;

public class GameConverterHelper {

    /**
     * Saves a battle.
     * 
     * @param dbHelper
     * @param battle
     * @return
     */
    public static long saveGame(DatabaseHelper dbHelper, Battle battle) {
        return dbHelper.getBattleDao().save(battle);
    }

    /**
     * Deletes the saved games depending on the game mode (campaign or single
     * battle).
     * 
     * @param dbHelper
     */
    public static void deleteSavedBattles(DatabaseHelper dbHelper) {
        dbHelper.getBattleDao().delete(null, null);
    }

    /**
     * Gets a Battle object from a byte array.
     * 
     * @param blob
     * @return
     */
    public static Battle getBattleFromLoadGame(byte[] blob) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream in = new ObjectInputStream(bais);
            Battle battle = (Battle) in.readObject();
            in.close();
            return battle;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a Message object from a byte array.
     * 
     * @param blob
     * @return
     */
    public static Message getMessageFromByte(byte[] blob) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream in = new ObjectInputStream(bais);
            Message msg = (Message) in.readObject();
            in.close();
            return msg;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets an object from a byte array.
     * 
     * @param blob
     * @return
     */
    public static Object getObjectFromByte(byte[] blob) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            ObjectInputStream in = new ObjectInputStream(bais);
            Object o = (Object) in.readObject();
            in.close();
            return o;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert any object to a byte array object.
     * 
     * @param object
     * @return
     */
    public static ByteArrayOutputStream toByte(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(baos);
            out.writeObject(object);
            out.close();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
