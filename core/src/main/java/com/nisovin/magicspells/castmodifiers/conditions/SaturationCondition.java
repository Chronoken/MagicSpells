package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import org.jetbrains.annotations.NotNull;

import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;

public class SaturationCondition extends OperatorCondition {

	private float saturation;
	
	@Override
	public boolean initialize(@NotNull String var) {
		if (var.length() < 2 || !super.initialize(var)) return false;

		try {
			saturation = Float.parseFloat(var.substring(1));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public boolean check(LivingEntity caster) {
		return saturation(caster);
	}

	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return saturation(target);
	}

	@Override
	public boolean check(LivingEntity caster, Location location) {
		return false;
	}

	private boolean saturation(LivingEntity livingEntity) {
		if (!(livingEntity instanceof Player pl)) return false;
		return compare(pl.getSaturation(), saturation);
	}

}
