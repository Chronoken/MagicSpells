package com.nisovin.magicspells.variables.meta;

import org.bukkit.Bukkit;

import com.nisovin.magicspells.variables.variabletypes.MetaVariable;

public class CurrentTickVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		return Bukkit.getCurrentTick();
	}

}
