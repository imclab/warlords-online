package com.giggs.apps.chaos.game.data;

import java.util.ArrayList;
import java.util.List;

import com.giggs.apps.chaos.game.model.units.Unit;
import com.giggs.apps.chaos.game.model.units.human.Bowman;
import com.giggs.apps.chaos.game.model.units.human.Knight;
import com.giggs.apps.chaos.game.model.units.human.Monk;
import com.giggs.apps.chaos.game.model.units.human.Soldier;

public class UnitsData {

    public static List<Unit> getInitialUnits(ArmiesData army, int armyIndex) {
        List<Unit> lstUnits = new ArrayList<Unit>();
        switch (army) {
        case HUMAN:
            lstUnits.add(new Soldier(armyIndex));
            lstUnits.add(new Soldier(armyIndex));
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
            lstUnits.add(new Knight(armyIndex));
            lstUnits.add(new Monk(armyIndex));
            break;
        }
        return lstUnits;
    }

}
