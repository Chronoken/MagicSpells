package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;

public class CreatureTargetSpell extends TargetedSpell implements TargetedEntitySpell, TargetedEntityFromLocationSpell {

	private String targetSpellName;
	private Subspell targetSpell;

	public CreatureTargetSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		targetSpellName = getConfigString("spell", "");
	}

	@Override
	public void initialize() {
		super.initialize();

		targetSpell = new Subspell(targetSpellName);
		if (!targetSpell.process()) {
			targetSpell = null;
			if (!targetSpellName.isEmpty()) MagicSpells.error("CreatureTargetSpell '" + internalName + "' has an invalid spell defined!");
		}
	}

	@Override
	public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			castSpells(caster, power);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
		castSpells(caster, power);
		return true;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		playSpellEffects(EffectPosition.TARGET, target);
		return true;
	}

	@Override
	public boolean castAtEntityFromLocation(LivingEntity caster, Location from, LivingEntity target, float power) {
		castSpells(caster, power);
		return true;
	}

	@Override
	public boolean castAtEntityFromLocation(Location from, LivingEntity target, float power) {
		playSpellEffects(from, target);
		return true;
	}

	private void castSpells(LivingEntity livingEntity, float power) {
		if (!(livingEntity instanceof Creature caster)) return;
		LivingEntity target = caster.getTarget();
		if (target == null || !target.isValid()) return;

		playSpellEffects(caster, target);
		if (targetSpell == null) return;
		if (targetSpell.isTargetedEntityFromLocationSpell()) targetSpell.castAtEntityFromLocation(caster, caster.getLocation(), target, power);
		else if (targetSpell.isTargetedLocationSpell()) targetSpell.castAtLocation(caster, target.getLocation(), power);
		else if (targetSpell.isTargetedEntitySpell()) targetSpell.castAtEntity(caster, target, power);
	}

}
