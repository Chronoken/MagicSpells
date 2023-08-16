package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.compat.CompatBasics;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.events.SpellApplyDamageEvent;
import com.nisovin.magicspells.events.MagicSpellsEntityDamageByEntityEvent;

public class PainSpell extends TargetedSpell implements TargetedEntitySpell {

	private final String spellDamageType;
	private final DamageCause damageType;

	private final ConfigData<Double> damage;

	private final ConfigData<Boolean> ignoreArmor;
	private final ConfigData<Boolean> checkPlugins;
	private final ConfigData<Boolean> powerAffectsDamage;
	private final ConfigData<Boolean> avoidDamageModification;
	private final ConfigData<Boolean> tryAvoidingAntiCheatPlugins;

	public PainSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		spellDamageType = getConfigString("spell-damage-type", "");

		String damageTypeName = getConfigString("damage-type", "ENTITY_ATTACK");
		DamageCause damageType;
		try {
		    damageType = DamageCause.valueOf(damageTypeName.toUpperCase());
		} catch (IllegalArgumentException ignored) {
			DebugHandler.debugBadEnumValue(DamageCause.class, damageTypeName);
			damageType = DamageCause.ENTITY_ATTACK;
		}
		this.damageType = damageType;

		damage = getConfigDataDouble("damage", 4);

		ignoreArmor = getConfigDataBoolean("ignore-armor", false);
		checkPlugins = getConfigDataBoolean("check-plugins", true);
		powerAffectsDamage = getConfigDataBoolean("power-affects-damage", true);
		avoidDamageModification = getConfigDataBoolean("avoid-damage-modification", true);
		tryAvoidingAntiCheatPlugins = getConfigDataBoolean("try-avoiding-anticheat-plugins", false);
	}

	@Override
	public CastResult cast(SpellData data) {
		TargetInfo<LivingEntity> info = getTargetedEntity(data);
		if (info.noTarget()) return noTarget(data);
		data = info.spellData();

		if (data.caster() instanceof Player caster) {
			SpellData finalData = data;
			return CompatBasics.exemptAction(() -> castAtEntity(finalData), caster, CompatBasics.activeExemptionAssistant.getPainExemptions());
		}

		return castAtEntity(data);
	}

	@Override
	public CastResult castAtEntity(SpellData data) {
		if (!data.target().isValid()) return noTarget(data);

		double damage = this.damage.get(data);
		if (powerAffectsDamage.get(data)) damage *= data.power();

		if (checkPlugins.get(data)) {
			MagicSpellsEntityDamageByEntityEvent event = new MagicSpellsEntityDamageByEntityEvent(data.caster(), data.target(), damageType, damage, this);
			if (!event.callEvent()) return noTarget(data);

			if (!avoidDamageModification.get(data)) event.getDamage();
			data.target().setLastDamageCause(event);
		}

		SpellApplyDamageEvent event = new SpellApplyDamageEvent(this, data.caster(), data.target(), damage, damageType, spellDamageType);
		event.callEvent();
		damage = event.getFinalDamage();

		if (ignoreArmor.get(data)) {
			double maxHealth = Util.getMaxHealth(data.target());

			double health = Math.min(data.target().getHealth(), maxHealth);
			health = Math.min(health - damage, maxHealth);

			if (health == 0 && data.caster() instanceof Player player) data.target().setKiller(player);
			data.target().setHealth(health);
			data.target().setLastDamage(damage);

			if (data.hasCaster()) MagicSpells.getVolatileCodeHandler().playHurtAnimation(data.target(), LocationUtil.getRotatedLocation(data.caster().getLocation(), data.target().getLocation()).getYaw());
			else MagicSpells.getVolatileCodeHandler().playHurtAnimation(data.target(), data.target().getLocation().getYaw());

			playSpellEffects(data);
			return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
		}

		if (tryAvoidingAntiCheatPlugins.get(data)) data.target().damage(damage);
		else data.target().damage(damage, data.caster());

		playSpellEffects(data);
		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

}
