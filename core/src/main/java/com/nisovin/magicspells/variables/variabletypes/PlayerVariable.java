package com.nisovin.magicspells.variables.variabletypes;

import java.util.Map;
import java.util.HashMap;

import com.nisovin.magicspells.variables.Variable;

public class PlayerVariable extends Variable {

	private final Map<String, Double> map = new HashMap<>();

	@Override
	public void set(String player, double amount) {
		if (amount > maxValue) amount = maxValue;
		else if (amount < minValue) amount = minValue;
		map.put(player, amount);
		if (objective == null) return;
		objective.getScore(player).setScore((int) amount);
	}

	@Override
	public double getValue(String player) {
		return map.getOrDefault(player, defaultValue);
	}

	@Override
	public void reset(String player) {
		map.remove(player);
		if (objective == null) return;
		objective.getScore(player).setScore((int) defaultValue);
	}

}
