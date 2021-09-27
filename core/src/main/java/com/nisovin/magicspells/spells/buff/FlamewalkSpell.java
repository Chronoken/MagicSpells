package com.nisovin.magicspells.spells.buff;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.events.MagicSpellsEntityDamageByEntityEvent;

public class FlamewalkSpell extends BuffSpell {

	private final Map<UUID, Float> entities;

	private int radius;
	private int fireTicks;
	private int tickInterval;

	private boolean checkPlugins;

	private Burner burner;
	
	public FlamewalkSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		radius = getConfigInt("radius", 8);
		fireTicks = getConfigInt("fire-ticks", 80);
		tickInterval = getConfigInt("tick-interval", 100);
		checkPlugins = getConfigBoolean("check-plugins", true);

		if (radius > MagicSpells.getGlobalRadius()) radius = MagicSpells.getGlobalRadius();

		entities = new HashMap<>();
	}

	@Override
	public boolean castBuff(LivingEntity entity, float power, String[] args) {
		entities.put(entity.getUniqueId(), power);
		if (burner == null) burner = new Burner();
		return true;
	}

	@Override
	public boolean isActive(LivingEntity entity) {
		return entities.containsKey(entity.getUniqueId());
	}

	@Override
	public void turnOffBuff(LivingEntity entity) {
		entities.remove(entity.getUniqueId());
		if (!entities.isEmpty()) return;
		if (burner == null) return;
		
		burner.stop();
		burner = null;
	}
	
	@Override
	protected void turnOff() {
		entities.clear();
		if (burner == null) return;
		
		burner.stop();
		burner = null;
	}

	public Map<UUID, Float> getEntities() {
		return entities;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getFireTicks() {
		return fireTicks;
	}

	public void setFireTicks(int fireTicks) {
		this.fireTicks = fireTicks;
	}

	public int getTickInterval() {
		return tickInterval;
	}

	public void setTickInterval(int tickInterval) {
		this.tickInterval = tickInterval;
	}

	public boolean shouldCheckPlugins() {
		return checkPlugins;
	}

	public void setCheckPlugins(boolean checkPlugins) {
		this.checkPlugins = checkPlugins;
	}

	private class Burner implements Runnable {
		
		private final int taskId;

		private Burner() {
			taskId = MagicSpells.scheduleRepeatingTask(this, tickInterval, tickInterval);
		}
		
		public void stop() {
			MagicSpells.cancelTask(taskId);
		}
		
		@Override
		public void run() {
			for (UUID id : entities.keySet()) {
				Entity entity = Bukkit.getEntity(id);
				if (!(entity instanceof LivingEntity)) continue;
				LivingEntity livingEntity = (LivingEntity) entity;

				if (isExpired(livingEntity)) {
					turnOff(livingEntity);
					continue;
				}

				float power = entities.get(livingEntity.getUniqueId());
				playSpellEffects(EffectPosition.DELAYED, livingEntity);

				List<Entity> entities = livingEntity.getNearbyEntities(radius, radius, radius);
				for (Entity target : entities) {
					if (!(target instanceof LivingEntity)) continue;
					if (validTargetList != null && !validTargetList.canTarget(target)) continue;
					if (livingEntity.equals(target)) continue;
					if (checkPlugins) {
						MagicSpellsEntityDamageByEntityEvent event = new MagicSpellsEntityDamageByEntityEvent(livingEntity, target, DamageCause.ENTITY_ATTACK, 1, FlamewalkSpell.this);
						EventUtil.call(event);
						if (event.isCancelled()) continue;
					}

					target.setFireTicks(Math.round(fireTicks * power));
					addUseAndChargeCost(livingEntity);
					playSpellEffects(EffectPosition.TARGET, target);
					playSpellEffectsTrail(livingEntity.getLocation(), target.getLocation());
				}

			}

		}
		
	}

}
