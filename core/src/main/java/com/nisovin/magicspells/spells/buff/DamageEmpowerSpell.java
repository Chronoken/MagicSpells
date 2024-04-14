package com.nisovin.magicspells.spells.buff;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.function.Supplier;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.events.SpellApplyDamageEvent;

public class DamageEmpowerSpell extends BuffSpell {

	private final Map<UUID, Supplier<Float>> entities;

	private SpellFilter filter;

	private final ConfigData<Boolean> constantDamageMultiplier;

	private final ConfigData<Float> damageMultiplier;

	public DamageEmpowerSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		damageMultiplier = getConfigDataFloat("damage-multiplier", 1.5F);
		constantDamageMultiplier = getConfigDataBoolean("constant-damage-multiplier", true);
		filter = getConfigSpellFilter();

		entities = new HashMap<>();
	}

	@Override
	public boolean castBuff(SpellData data) {
		Supplier<Float> supplier;
		if (constantDamageMultiplier.get(data)) {
			float damageMultiplier = this.damageMultiplier.get(data);
			supplier = () -> damageMultiplier;
		} else supplier = () -> damageMultiplier.get(data);

		entities.put(data.target().getUniqueId(), supplier);

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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSpellApplyDamage(SpellApplyDamageEvent event) {
		LivingEntity caster = event.getCaster();
		if (!isActiveBuff(caster)) return;
		if (!filter.check(event.getSpell())) return;

		float damageMultiplier = entities.get(caster.getUniqueId()).get();
		event.applyDamageModifier(damageMultiplier);

		addUseAndChargeCost(caster);
	}

	public Map<UUID, Supplier<Float>> getEntities() {
		return entities;
	}

	public SpellFilter getFilter() {
		return filter;
	}

	public void setFilter(SpellFilter filter) {
		this.filter = filter;
	}

}
