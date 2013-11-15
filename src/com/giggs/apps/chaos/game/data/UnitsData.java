package com.giggs.apps.chaos.game.data;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.model.units.Unit;
import com.giggs.apps.chaos.game.model.units.chaos.DamnedSoul;
import com.giggs.apps.chaos.game.model.units.chaos.Demon;
import com.giggs.apps.chaos.game.model.units.chaos.Warrior;
import com.giggs.apps.chaos.game.model.units.chaos.Wizard;
import com.giggs.apps.chaos.game.model.units.human.Bowman;
import com.giggs.apps.chaos.game.model.units.human.Knight;
import com.giggs.apps.chaos.game.model.units.human.Monk;
import com.giggs.apps.chaos.game.model.units.human.Soldier;
import com.giggs.apps.chaos.game.model.units.orc.Goblin;
import com.giggs.apps.chaos.game.model.units.orc.Ogre;
import com.giggs.apps.chaos.game.model.units.orc.Orc;
import com.giggs.apps.chaos.game.model.units.orc.Troll;
import com.giggs.apps.chaos.game.model.units.undead.Necromancer;
import com.giggs.apps.chaos.game.model.units.undead.Skeleton;
import com.giggs.apps.chaos.game.model.units.undead.Zombie;

public class UnitsData {

    public static List<Unit> getInitialUnits(ArmiesData army, int armyIndex) {
        List<Unit> lstUnits = new ArrayList<Unit>();
        switch (army) {
        case HUMAN:
            lstUnits.add(new Soldier(armyIndex));
            lstUnits.add(new Soldier(armyIndex));
            break;
        case UNDEAD:
            lstUnits.add(new Skeleton(armyIndex));
            lstUnits.add(new Skeleton(armyIndex));
            break;
        case ORCS:
            lstUnits.add(new Goblin(armyIndex));
            lstUnits.add(new Goblin(armyIndex));
            break;
        case CHAOS:
            lstUnits.add(new Warrior(armyIndex));
            lstUnits.add(new Warrior(armyIndex));
            break;
        }
        return lstUnits;
    }

    public static List<Unit> getUnits(ArmiesData army, int armyIndex) {
        List<Unit> lstUnits = new ArrayList<Unit>();
        switch (army) {
        case HUMAN:
            lstUnits.add(new Soldier(armyIndex));
            lstUnits.add(new Bowman(armyIndex));
            lstUnits.add(new Monk(armyIndex));
            lstUnits.add(new Knight(armyIndex));
            break;
        case UNDEAD:
            lstUnits.add(new Skeleton(armyIndex));
            lstUnits.add(new Bowman(armyIndex));
            lstUnits.add(new Zombie(armyIndex));
            lstUnits.add(new Necromancer(armyIndex));
            break;
        case ORCS:
            lstUnits.add(new Goblin(armyIndex));
            lstUnits.add(new Orc(armyIndex));
            lstUnits.add(new Troll(armyIndex));
            lstUnits.add(new Ogre(armyIndex));
            break;
        case CHAOS:
            lstUnits.add(new Warrior(armyIndex));
            lstUnits.add(new Wizard(armyIndex));
            lstUnits.add(new Demon(armyIndex));
            lstUnits.add(new DamnedSoul(armyIndex));
            break;
        }
        return lstUnits;
    }

}
