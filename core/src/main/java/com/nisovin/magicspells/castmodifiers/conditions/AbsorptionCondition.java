package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import org.jetbrains.annotations.NotNull;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;

@Name("absorption")
public class AbsorptionCondition extends OperatorCondition {

	private float health = 0;

	@Override
	public boolean initialize(@NotNull String var) {
		if (var.length() < 2 || !super.initialize(var)) return false;

		try {
			health = Float.parseFloat(var.substring(1));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean check(LivingEntity caster) {
		return absorption(caster);
	}
	
	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return absorption(target);
	}
	
	@Override
	public boolean check(LivingEntity caster, Location location) {
		return false;
	}

	private boolean absorption(LivingEntity target) {
		return compare(target.getAbsorptionAmount(), health);
	}
	
}
