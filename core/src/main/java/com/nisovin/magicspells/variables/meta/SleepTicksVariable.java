package com.nisovin.magicspells.variables.meta;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.variables.variabletypes.MetaVariable;

public class SleepTicksVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		Player p = PlayerNameUtils.getPlayerExact(player);
		if (p != null) return p.getSleepTicks();
		return 0;
	}

}
