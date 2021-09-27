package com.nisovin.magicspells.castmodifiers;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.*;
import com.nisovin.magicspells.MagicSpells;

public class ModifierSet {

	public static CastListener castListener = null;
	public static TargetListener targetListener = null;
	public static ManaListener manaListener = null;

	public static void initializeModifierListeners() {
		boolean modifiers = false;
		boolean targetModifiers = false;
		for (Spell spell : MagicSpells.spells()) {
			if (spell.getModifiers() != null) modifiers = true;
			if (spell.getTargetModifiers() != null) targetModifiers = true;
			if (modifiers && targetModifiers) break;
		}

		if (modifiers) {
			castListener = new CastListener();
			MagicSpells.registerEvents(castListener);
		}
		if (targetModifiers) {
			targetListener = new TargetListener();
			MagicSpells.registerEvents(targetListener);
		}
		if (MagicSpells.getManaHandler() != null && MagicSpells.getManaHandler().getModifiers() != null) {
			manaListener = new ManaListener();
			MagicSpells.registerEvents(manaListener);
		}
	}

	public static void unload() {
		if (castListener != null) {
			castListener.unload();
			castListener = null;
		}

		if (targetListener != null) {
			targetListener.unload();
			targetListener = null;
		}

		if (manaListener != null) {
			manaListener.unload();
			manaListener = null;
		}
	}

	private final List<Modifier> modifiers;

	public ModifierSet(List<String> data) {
		this(data, null, false);
	}

	public ModifierSet(List<String> data, Spell spell) {
		this(data, spell, false);
	}

	public ModifierSet(List<String> data, boolean isFromManaSystem) {
		this(data, null, isFromManaSystem);
	}

	private ModifierSet(List<String> data, Spell spell, boolean isFromManaSystem) {
		modifiers = new ArrayList<>();
		for (String s : data) {
			Modifier m = new Modifier();
			m.process(s);

			if (!m.isInitialized()) {
				String extra = "";
				if (m.getCustomActionData() != null) extra = ": " + m.getCustomActionData().getInvalidText();

				if (isFromManaSystem) MagicSpells.error("Mana system has a problem with modifier '" + s + "'" + extra);
				else if (spell != null) MagicSpells.error("Spell '" + spell.getInternalName() + "' has a problem with modifier '" + s + "'" + extra);
				else MagicSpells.error("Problem with modifier: " + s + "'" + extra);
				continue;
			}

			modifiers.add(m);
			if (isFromManaSystem) MagicSpells.debug(3, "    Modifier added for mana system: " + s);
			else if (spell != null) MagicSpells.debug(3, "    Modifier added for spell '" + spell.getInternalName() + "': " + s);
			else MagicSpells.debug(3, "    Modifier added: " + s);
		}
	}

	public void apply(SpellCastEvent event) {
		for (Modifier modifier : modifiers) {
			boolean cont = modifier.apply(event);
			if (!cont) {
				String msg = modifier.getStrModifierFailed() != null ? modifier.getStrModifierFailed() : event.getSpell().getStrModifierFailed();
				MagicSpells.sendMessage(msg, event.getCaster(), event.getSpellArgs());
				break;
			}
		}
	}

	public void apply(ManaChangeEvent event) {
		for (Modifier modifier : modifiers) {
			boolean cont = modifier.apply(event);
			if (!cont) {
				if (modifier.getStrModifierFailed() != null) MagicSpells.sendMessage(modifier.getStrModifierFailed(), event.getPlayer(), MagicSpells.NULL_ARGS);
				break;
			}
		}
	}

	public void apply(SpellTargetEvent event) {
		for (Modifier modifier : modifiers) {
			boolean cont = modifier.apply(event);
			if (!cont) {
				if (modifier.getStrModifierFailed() != null) MagicSpells.sendMessage(modifier.getStrModifierFailed(), event.getCaster(), MagicSpells.NULL_ARGS);
				break;
			}
		}
	}

	public void apply(MagicSpellsGenericPlayerEvent event) {
		for (Modifier modifier : modifiers) {
			boolean cont = modifier.apply(event);
			if (!cont) {
				if (modifier.getStrModifierFailed() != null) MagicSpells.sendMessage(modifier.getStrModifierFailed(), event.getPlayer(), MagicSpells.NULL_ARGS);
				break;
			}
		}
	}

	public void apply(SpellTargetLocationEvent event) {
		for (Modifier modifier : modifiers) {
			boolean cont = modifier.apply(event);
			if (!cont) {
				if (modifier.getStrModifierFailed() != null) MagicSpells.sendMessage(modifier.getStrModifierFailed(), event.getCaster(), MagicSpells.NULL_ARGS);
				break;
			}
		}
	}

	public boolean check(LivingEntity livingEntity) {
		for (Modifier modifier : modifiers) {
			boolean pass = modifier.check(livingEntity);
			if (!pass) return false;
		}
		return true;
	}

	public boolean check(LivingEntity livingEntity, LivingEntity entity) {
		for (Modifier modifier : modifiers) {
			boolean pass = modifier.check(livingEntity, entity);
			if (!pass) return false;
		}
		return true;
	}

	public boolean check(LivingEntity livingEntity, Location location) {
		for (Modifier modifier : modifiers) {
			boolean pass = modifier.check(livingEntity, location);
			if (!pass) return false;
		}
		return true;
	}

}
