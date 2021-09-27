package com.nisovin.magicspells;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.util.ValidTargetList;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.Spell.SpellCastState;
import com.nisovin.magicspells.Spell.PostCastAction;
import com.nisovin.magicspells.Spell.SpellCastResult;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.CastUtil.CastMode;
import com.nisovin.magicspells.events.SpellCastedEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;

public class Subspell {

	private static final Random random = ThreadLocalRandom.current();
	
	private Spell spell;
	private String spellName;
	private CastMode mode = CastMode.PARTIAL;

	private int delay = -1;
	private float subPower = 1F;
	private double chance = -1D;
	
	private boolean isTargetedEntity = false;
	private boolean isTargetedLocation = false;
	private boolean isTargetedEntityFromLocation = false;

	// spellName(mode=hard|h|full|f|partial|p|direct|d;power=[subpower];delay=[delay];chance=[chance])
	public Subspell(String data) {
		String[] split = data.split("\\(", 2);
		
		spellName = split[0].trim();
		
		if (split.length > 1) {
			split[1] = split[1].trim();
			if (split[1].endsWith(")")) split[1] = split[1].substring(0, split[1].length() - 1);
			String[] args = Util.splitParams(split[1]);

			for (String arg : args) {
				if (!arg.contains("=")) continue;

				String[] castArguments = arg.split(";");
				for (String castArgument : castArguments) {
					String[] keyValue = castArgument.split("=");
					switch (keyValue[0].toLowerCase()) {
						case "mode" -> mode = Util.getCastMode(keyValue[1]);
						case "power" -> {
							try {
								subPower = Float.parseFloat(keyValue[1]);
							} catch (NumberFormatException e) {
								DebugHandler.debugNumberFormat(e);
							}
						}
						case "delay" -> {
							try {
								delay = Integer.parseInt(keyValue[1]);
							} catch (NumberFormatException e) {
								DebugHandler.debugNumberFormat(e);
							}
						}
						case "chance" -> {
							try {
								chance = Double.parseDouble(keyValue[1]) / 100D;
							} catch (NumberFormatException e) {
								DebugHandler.debugNumberFormat(e);
							}
						}
					}
				}
			}
		}

	}
	
	public boolean process() {
		spell = MagicSpells.getSpellByInternalName(spellName);
		if (spell != null) {
			isTargetedEntity = spell instanceof TargetedEntitySpell;
			isTargetedLocation = spell instanceof TargetedLocationSpell;
			isTargetedEntityFromLocation = spell instanceof TargetedEntityFromLocationSpell;
		}
		return spell != null;
	}
	
	public Spell getSpell() {
		return spell;
	}
	
	public boolean isTargetedEntitySpell() {
		return isTargetedEntity;
	}
	
	public boolean isTargetedLocationSpell() {
		return isTargetedLocation;
	}
	
	public boolean isTargetedEntityFromLocationSpell() {
		return isTargetedEntityFromLocation;
	}
	
	public PostCastAction cast(final LivingEntity caster, final float power) {
		if ((chance > 0 && chance < 1) && random.nextDouble() > chance) return PostCastAction.ALREADY_HANDLED;
		if (delay < 0) return castReal(caster, power);
		MagicSpells.scheduleDelayedTask(() -> castReal(caster, power), delay);
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private PostCastAction castReal(LivingEntity caster, float power) {
		if ((mode == CastMode.HARD || mode == CastMode.FULL) && caster != null) {
			return spell.cast(caster, power * subPower, null).action;
		}
		
		if (mode == CastMode.PARTIAL) {
			SpellCastEvent event = new SpellCastEvent(spell, caster, SpellCastState.NORMAL, power * subPower, null, 0, null, 0);
			EventUtil.call(event);
			if (!event.isCancelled() && event.getSpellCastState() == SpellCastState.NORMAL) {
				PostCastAction act = spell.castSpell(caster, SpellCastState.NORMAL, event.getPower(), null);
				EventUtil.call(new SpellCastedEvent(spell, caster, SpellCastState.NORMAL, event.getPower(), null, 0, null, act));
				return act;
			}
			return PostCastAction.ALREADY_HANDLED;
		}
		
		return spell.castSpell(caster, SpellCastState.NORMAL, power * subPower, null);
	}

	public boolean castAtEntity(final LivingEntity caster, final LivingEntity target, final float power) {
		return castAtEntity(caster, target, power, true);
	}

	public boolean castAtEntity(final LivingEntity caster, final LivingEntity target, final float power, final boolean passTargeting) {
		if (delay < 0) return castAtEntityReal(caster, target, power, passTargeting);
		MagicSpells.scheduleDelayedTask(() -> castAtEntityReal(caster, target, power, passTargeting), delay);
		return true;
	}
	
	private boolean castAtEntityReal(LivingEntity caster, LivingEntity target, float power, boolean passTargeting) {
		if (!isTargetedEntity) {
			if (isTargetedLocation) castAtLocationReal(caster, target.getLocation(), power);
			return false;
		}

		if (mode == CastMode.HARD && caster != null) {
			SpellCastResult result = spell.cast(caster, power, null);
			return result.state == SpellCastState.NORMAL && result.action == PostCastAction.HANDLE_NORMALLY;
		}

		if (mode == CastMode.FULL && caster != null) {
			SpellCastEvent spellCast = spell.preCast(caster, power * subPower, null);
			if (spellCast == null) return false;

			PostCastAction action = PostCastAction.HANDLE_NORMALLY;
			boolean success = false;
			if (spellCast.getSpellCastState() == SpellCastState.NORMAL) {
				SpellTargetEvent spellTarget = new SpellTargetEvent(spell, caster, target, power);
				EventUtil.call(spellTarget);

				if (!spellTarget.isCancelled()) {
					if (passTargeting) success = passTargetingEntity(caster, target, spellCast.getPower());
					else success = ((TargetedEntitySpell) spell).castAtEntity(caster, target, spellCast.getPower());
				}

				if (success) {
					if (spell instanceof TargetedSpell targetedSpell) {
						action = PostCastAction.NO_MESSAGES;
						targetedSpell.sendMessages(caster, target, null);
					}
				} else action = PostCastAction.ALREADY_HANDLED;
			}

			spell.postCast(spellCast, action);

			return success;
		}

		boolean success = false;

		if (mode == CastMode.PARTIAL) {
			SpellCastEvent event = new SpellCastEvent(spell, caster, SpellCastState.NORMAL, power * subPower, null, 0, null, 0);
			SpellTargetEvent spellTarget = new SpellTargetEvent(spell, caster, target, power);
			EventUtil.call(spellTarget);
			EventUtil.call(event);
			if (!spellTarget.isCancelled() && !event.isCancelled() && event.getSpellCastState() == SpellCastState.NORMAL) {
				if (passTargeting) success = passTargetingEntity(caster, target, event.getPower());
				else {
					if (caster != null) success = ((TargetedEntitySpell) spell).castAtEntity(caster, target, event.getPower());
					else success = ((TargetedEntitySpell) spell).castAtEntity(target, event.getPower());
				}
				if (success) EventUtil.call(new SpellCastedEvent(spell, caster, SpellCastState.NORMAL, event.getPower(), null, 0, null, PostCastAction.HANDLE_NORMALLY));
			}
		} else {
			if (passTargeting) success = passTargetingEntity(caster, target, power * subPower);
			else {
				if (caster != null) success = ((TargetedEntitySpell) spell).castAtEntity(caster, target, power * subPower);
				else success = ((TargetedEntitySpell) spell).castAtEntity(target, power * subPower);
			}
		}

		return success;
	}

	public boolean passTargetingEntity(LivingEntity caster, LivingEntity target, float power) {
		ValidTargetList list = spell.getValidTargetList();
		ValidTargetList	originalList = list.clone();
		if (caster.equals(target) && !list.canTargetSelf()) list.setTargetCaster(true);
		if (!list.canTargetEntity(target)) {
			list.addEntityTarget(target);
			spell.setValidTargetList(list);
		}

		boolean success = caster != null ? ((TargetedEntitySpell) spell).castAtEntity(caster, target, power) : ((TargetedEntitySpell) spell).castAtEntity(target, power);
		spell.setValidTargetList(originalList);
		return success;
	}
	
	public boolean castAtLocation(final LivingEntity caster, final Location target, final float power) {
		if (delay < 0) return castAtLocationReal(caster, target, power);
		MagicSpells.scheduleDelayedTask(() -> castAtLocationReal(caster, target, power), delay);
		return true;
	}
	
	private boolean castAtLocationReal(LivingEntity caster, Location target, float power) {
		if (!isTargetedLocation) return false;

		if (mode == CastMode.HARD && caster != null) {
			SpellCastResult result = spell.cast(caster, power, null);
			return result.state == SpellCastState.NORMAL && result.action == PostCastAction.HANDLE_NORMALLY;
		}

		if (mode == CastMode.FULL && caster != null) {
			SpellCastEvent spellCast = spell.preCast(caster, power * subPower, null);
			if (spellCast == null) return false;

			PostCastAction action = PostCastAction.HANDLE_NORMALLY;
			boolean success = false;
			if (spellCast.getSpellCastState() == SpellCastState.NORMAL) {
				SpellTargetLocationEvent spellLocation = new SpellTargetLocationEvent(spell, caster, target, power);
				EventUtil.call(spellLocation);

				if (!spellLocation.isCancelled())
					success = ((TargetedLocationSpell) spell).castAtLocation(caster, target, spellCast.getPower());

				if (!success) action = PostCastAction.ALREADY_HANDLED;
			}

			spell.postCast(spellCast, action);

			return success;
		}

		boolean success = false;

		if (mode == CastMode.PARTIAL) {
			SpellCastEvent event = new SpellCastEvent(spell, caster, SpellCastState.NORMAL, power * subPower, null, 0, null, 0);
			SpellTargetLocationEvent spellLocation = new SpellTargetLocationEvent(spell, caster, target, power);
			EventUtil.call(spellLocation);
			EventUtil.call(event);
			if (!spellLocation.isCancelled() && !event.isCancelled() && event.getSpellCastState() == SpellCastState.NORMAL) {
				if (caster != null) success = ((TargetedLocationSpell) spell).castAtLocation(caster, target, event.getPower());
				else success = ((TargetedLocationSpell) spell).castAtLocation(target, event.getPower());
				if (success) EventUtil.call(new SpellCastedEvent(spell, caster, SpellCastState.NORMAL, event.getPower(), null, 0, null, PostCastAction.HANDLE_NORMALLY));
			}
		} else {
			if (caster != null) success = ((TargetedLocationSpell) spell).castAtLocation(caster, target, power * subPower);
			else success = ((TargetedLocationSpell) spell).castAtLocation(target, power * subPower);
		}

		return success;
	}

	public boolean castAtEntityFromLocation(final LivingEntity caster, final Location from, final LivingEntity target, final float power) {
		return castAtEntityFromLocation(caster, from, target, power, true);
	}

	public boolean castAtEntityFromLocation(final LivingEntity caster, final Location from, final LivingEntity target, final float power, final boolean passTargeting) {
		if (delay < 0) return castAtEntityFromLocationReal(caster, from, target, power, passTargeting);
		MagicSpells.scheduleDelayedTask(() -> castAtEntityFromLocationReal(caster, from, target, power, passTargeting), delay);
		return true;
	}
	
	private boolean castAtEntityFromLocationReal(LivingEntity caster, Location from, LivingEntity target, float power, boolean passTargeting) {
		if (!isTargetedEntityFromLocation) return false;

		if (mode == CastMode.HARD && caster != null) {
			SpellCastResult result = spell.cast(caster, power, MagicSpells.NULL_ARGS);
			return result.state == SpellCastState.NORMAL && result.action == PostCastAction.HANDLE_NORMALLY;
		}

		if (mode == CastMode.FULL && caster != null) {
			SpellCastEvent spellCast = spell.preCast(caster, power * subPower, MagicSpells.NULL_ARGS);
			if (spellCast == null) return false;

			PostCastAction action = PostCastAction.HANDLE_NORMALLY;
			boolean success = false;
			if (spellCast.getSpellCastState() == SpellCastState.NORMAL) {
				SpellTargetEvent spellTarget = new SpellTargetEvent(spell, caster, target, power);
				SpellTargetLocationEvent spellLocation = new SpellTargetLocationEvent(spell, caster, from, power);
				EventUtil.call(spellLocation);
				EventUtil.call(spellTarget);

				if (!spellLocation.isCancelled() && !spellTarget.isCancelled()) {
					if (passTargeting) success = passTargetingEntityFromLocation(caster, from, target, spellCast.getPower());
					else success = ((TargetedEntityFromLocationSpell) spell).castAtEntityFromLocation(caster, from, target, spellCast.getPower());
				}
				if (success) {
					if (spell instanceof TargetedSpell targetedSpell) {
						action = PostCastAction.NO_MESSAGES;
						targetedSpell.sendMessages(caster, target, null);
					}
				} else action = PostCastAction.ALREADY_HANDLED;
			}

			spell.postCast(spellCast, action);

			return success;
		}

		boolean success = false;

		if (mode == CastMode.PARTIAL) {
			SpellCastEvent event = new SpellCastEvent(spell, caster, SpellCastState.NORMAL, power * subPower, null, 0, null, 0);
			SpellTargetEvent spellTarget = new SpellTargetEvent(spell, caster, target, power);
			SpellTargetLocationEvent spellLocation = new SpellTargetLocationEvent(spell, caster, from, power);
			EventUtil.call(spellLocation);
			EventUtil.call(spellTarget);
			EventUtil.call(event);
			if (!spellLocation.isCancelled() && !spellTarget.isCancelled() && !event.isCancelled() && event.getSpellCastState() == SpellCastState.NORMAL) {
				if (passTargeting) success = passTargetingEntityFromLocation(caster, from, target, event.getPower());
				else {
					if (caster != null) success = ((TargetedEntityFromLocationSpell) spell).castAtEntityFromLocation(caster, from, target, event.getPower());
					else success = ((TargetedEntityFromLocationSpell) spell).castAtEntityFromLocation(from, target, event.getPower());
				}
				if (success) EventUtil.call(new SpellCastedEvent(spell, caster, SpellCastState.NORMAL, event.getPower(), null, 0, null, PostCastAction.HANDLE_NORMALLY));
			}
		} else {
			if (passTargeting) success = passTargetingEntityFromLocation(caster, from, target, power * subPower);
			else {
				if (caster != null) success = ((TargetedEntityFromLocationSpell) spell).castAtEntityFromLocation(caster, from, target, power * subPower);
				else success = ((TargetedEntityFromLocationSpell) spell).castAtEntityFromLocation(from, target, power * subPower);
			}
		}

		return success;
	}

	public boolean passTargetingEntityFromLocation(LivingEntity caster, Location from, LivingEntity target, float power) {
		ValidTargetList list = spell.getValidTargetList();
		ValidTargetList	originalList = list.clone();
		if (caster.equals(target) && !list.canTargetSelf()) list.setTargetCaster(true);
		if (!list.canTargetEntity(target)) {
			list.addEntityTarget(target);
			spell.setValidTargetList(list);
		}

		boolean success = caster != null ? ((TargetedEntityFromLocationSpell) spell).castAtEntityFromLocation(caster, from, target, power) : ((TargetedEntityFromLocationSpell) spell).castAtEntityFromLocation(from, target, power);
		spell.setValidTargetList(originalList);
		return success;
	}

}
