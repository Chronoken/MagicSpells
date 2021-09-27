package com.nisovin.magicspells.spelleffects.effecttypes;

import org.bukkit.entity.Entity;

public class EffectLibEntityEffect extends EffectLibEffect {
	
	@Override
	protected Runnable playEffectEntity(final Entity e) {
		if (!initialize()) return null;
		return manager.start(className, effectLibSection, e);
	}

}
