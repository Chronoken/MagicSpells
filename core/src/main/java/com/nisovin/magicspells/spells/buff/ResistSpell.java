package com.nisovin.magicspells.spells.buff;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.events.SpellApplyDamageEvent;

public class ResistSpell extends BuffSpell {

	private final Map<UUID, ResistData> entities;

	private final Set<String> spellDamageTypes;

	private final Set<DamageCause> normalDamageTypes;

	private final ConfigData<Float> multiplier;

	private final ConfigData<Boolean> constantMultiplier;
	private final ConfigData<Boolean> powerAffectsMultiplier;

	public ResistSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		multiplier = getConfigDataFloat("multiplier", 0.5F);

		constantMultiplier = getConfigDataBoolean("constant-multiplier", true);
		powerAffectsMultiplier = getConfigDataBoolean("power-affects-multiplier", true);

		normalDamageTypes = new HashSet<>();
		List<String> causes = getConfigStringList("normal-damage-types", null);
		if (causes != null) {
			for (String cause : causes) {
				try {
					DamageCause damageCause = DamageCause.valueOf(cause.replace(" ", "_").replace("-", "_").toUpperCase());
					normalDamageTypes.add(damageCause);
				} catch (IllegalArgumentException e) {
					MagicSpells.error("ResistSpell '" + internalName + "' has an invalid damage cause defined '" + cause + "'!");
				}
			}
		}

		spellDamageTypes = new HashSet<>();
		causes = getConfigStringList("spell-damage-types", null);
		if (causes != null) spellDamageTypes.addAll(causes);

		entities = new HashMap<>();
	}

	@Override
	public boolean castBuff(SpellData data) {
		boolean constantMultiplier = this.constantMultiplier.get(data);

		float multiplier = 0;
		if (constantMultiplier) {
			multiplier = this.multiplier.get(data);
			if (powerAffectsMultiplier.get(data)) {
				if (multiplier < 1) multiplier /= data.power();
				else if (multiplier > 1) multiplier *= data.power();
			}
		}

		entities.put(data.target().getUniqueId(), new ResistData(data, multiplier, constantMultiplier));
		return true;
	}

	@Override
	public boolean recastBuff(SpellData data) {
		stopEffects(data.target());
		return castBuff(data);
	}

	@Override
	protected boolean isActiveBuff(LivingEntity entity) {
		return entities.containsKey(entity.getUniqueId());
	}

	@Override
	protected void turnOffBuff(LivingEntity entity) {
		entities.remove(entity.getUniqueId());
	}

	@Override
	protected void turnOffBuff() {
		entities.clear();
	}

	@EventHandler
	public void onSpellDamage(SpellApplyDamageEvent event) {
		if (spellDamageTypes.isEmpty()) return;
		if (!isActiveBuff(event.getTarget())) return;

		String spellDamageType = event.getSpellDamageType();
		if (spellDamageType == null) return;
		if (!spellDamageTypes.contains(spellDamageType)) return;

		LivingEntity caster = event.getTarget();
		ResistData data = entities.get(caster.getUniqueId());

		float multiplier = data.multiplier;
		if (!data.constantMultiplier) {
			SpellData subData = data.spellData.target(event.getCaster());

			multiplier = this.multiplier.get(subData);
			if (powerAffectsMultiplier.get(subData)) {
				if (multiplier < 1) multiplier /= subData.power();
				else if (multiplier > 1) multiplier *= subData.power();
			}
		}

		addUseAndChargeCost(caster);
		event.applyDamageModifier(multiplier);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (normalDamageTypes.isEmpty()) return;
		if (!normalDamageTypes.contains(event.getCause())) return;

		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity caster)) return;
		if (!isActiveBuff(caster)) return;

		LivingEntity target = null;
		if (event instanceof EntityDamageByEntityEvent e)
			if (e.getDamager() instanceof LivingEntity damager)
				target = damager;

		ResistData data = entities.get(caster.getUniqueId());

		float multiplier = data.multiplier;
		if (!data.constantMultiplier) {
			SpellData subData = data.spellData.target(target);

			multiplier = this.multiplier.get(subData);
			if (powerAffectsMultiplier.get(subData)) {
				if (multiplier < 1) multiplier /= subData.power();
				else if (multiplier > 1) multiplier *= subData.power();
			}
		}

		addUseAndChargeCost(caster);
		event.setDamage(event.getDamage() * multiplier);
	}

	public Map<UUID, ResistData> getEntities() {
		return entities;
	}

	public Set<String> getSpellDamageTypes() {
		return spellDamageTypes;
	}

	public Set<DamageCause> getNormalDamageTypes() {
		return normalDamageTypes;
	}

	public record ResistData(SpellData spellData, float multiplier, boolean constantMultiplier) {
	}

}
