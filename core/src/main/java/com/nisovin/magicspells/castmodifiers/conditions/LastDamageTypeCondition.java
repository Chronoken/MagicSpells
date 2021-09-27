package com.nisovin.magicspells.castmodifiers.conditions;

import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class LastDamageTypeCondition extends Condition {

	private DamageCause cause;

	@Override
	public boolean initialize(String var) {
		for (DamageCause dc : DamageCause.values()) {
			if (dc.name().equalsIgnoreCase(var)) {
				cause = dc;
				return true;
			}
		}
		DebugHandler.debugBadEnumValue(DamageCause.class, var);
		return false;
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		return check(livingEntity, livingEntity);
	}

	@Override
	public boolean check(LivingEntity livingEntity, LivingEntity target) {
		EntityDamageEvent event = target.getLastDamageCause();
		return event != null && event.getCause() == cause;
	}

	@Override
	public boolean check(LivingEntity livingEntity, Location location) {
		return false;
	}

}
