package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import org.jetbrains.annotations.NotNull;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.util.MobUtil;
import com.nisovin.magicspells.castmodifiers.Condition;

@Name("riding")
public class RidingCondition extends Condition {

	private EntityType entityType;
	
	@Override
	public boolean initialize(@NotNull String var) {
		if (var.isEmpty()) return true;
		entityType = MobUtil.getEntityType(var);
		return entityType != null;
	}

	@Override
	public boolean check(LivingEntity caster) {
		return isRiding(caster);
	}

	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return isRiding(target);
	}
	
	@Override
	public boolean check(LivingEntity caster, Location location) {
		return false;
	}

	private boolean isRiding(LivingEntity target) {
		Entity vehicle = target.getVehicle();
		if (vehicle == null) return false;
		return entityType == null || vehicle.getType() == entityType;
	}

}
