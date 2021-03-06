package com.nisovin.magicspells.spells.passive;

import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityToggleSwimEvent;

import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.spells.passive.util.PassiveListener;

// No trigger variable is currently used
public class StopSwimListener extends PassiveListener {

	@Override
	public void initialize(String var) {

	}

	@OverridePriority
	@EventHandler
	public void onSwim(EntityToggleSwimEvent event) {
		if (!(event.getEntity() instanceof LivingEntity)) return;
		if (!isCancelStateOk(event.isCancelled())) return;
		if (event.isSwimming()) return;

		LivingEntity caster = (LivingEntity) event.getEntity();
		if (!hasSpell(caster) || !canTrigger(caster)) return;

		boolean casted = passiveSpell.activate(caster);
		if (cancelDefaultAction(casted)) event.setCancelled(true);
	}

}
