package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import org.jetbrains.annotations.NotNull;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.Condition;

@Name("facing")
public class FacingCondition extends Condition {

	private String direction;

	@Override
	public boolean initialize(@NotNull String var) {
		direction = var;
		return true;
	}

	@Override
	public boolean check(LivingEntity caster) {
		return getDirection(caster.getLocation()).equals(direction);
	}

	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return getDirection(target.getLocation()).equals(direction);
	}

	@Override
	public boolean check(LivingEntity caster, Location location) {
		return getDirection(location).equals(direction);
	}

	private String getDirection(Location loc) {
		float y = loc.getYaw();
		if (y < 0) y += 360;
		y %= 360;
		if (y <= 45 || y >= 315) return "south";
		if (y >= 45 && y <= 135) return "west";
		if (y >= 135 && y <= 225) return "north";
		return "east";
	}

}
