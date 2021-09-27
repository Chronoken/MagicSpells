package com.nisovin.magicspells.spells;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.ValidTargetList;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;

public class BowSpell extends Spell {

	private static final String METADATA_KEY = "MSBowSpell";
	private static HitListener hitListener;

	private List<String> bowNames;
	private List<String> disallowedBowNames;

	private ValidTargetList triggerList;

	private String bowName;
	private String spellOnShootName;
	private String spellOnHitEntityName;
	private String spellOnHitGroundName;

	private Subspell spellOnShoot;
	private Subspell spellOnHitEntity;
	private Subspell spellOnHitGround;

	private boolean cancelShot;
	private boolean denyOffhand;
	private boolean removeArrow;
	private boolean requireBind;
	private boolean useBowForce;
	private boolean cancelShotOnFail;

	private float minimumForce;
	private float maximumForce;

	public BowSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		List<String> names = getConfigStringList("bow-names", null);
		if (names != null) {
			bowNames = new ArrayList<>();
			names.forEach(str -> bowNames.add(Util.colorize(str)));
		} else bowName = Util.colorize(getConfigString("bow-name", ""));

		List<String> disallowedNames = getConfigStringList("disallowed-bow-names", null);
		if (disallowedNames != null) {
			disallowedBowNames = new ArrayList<>();
			disallowedNames.forEach(str -> disallowedBowNames.add(Util.colorize(str)));
		}

		if (config.isList("spells." + internalName + ".can-trigger")) {
			List<String> targets = getConfigStringList("can-trigger", new ArrayList<>());
			if (targets.isEmpty()) targets.add("players");
			triggerList = new ValidTargetList(this, targets);
		} else triggerList = new ValidTargetList(this, getConfigString("can-trigger", "players"));

		spellOnShootName = getConfigString("spell", "");
		spellOnHitEntityName = getConfigString("spell-on-hit-entity", "");
		spellOnHitGroundName = getConfigString("spell-on-hit-ground", "");

		bindable = getConfigBoolean("bindable", false);
		cancelShot = getConfigBoolean("cancel-shot", true);
		denyOffhand = getConfigBoolean("deny-offhand", false);
		removeArrow = getConfigBoolean("remove-arrow", false);
		requireBind = getConfigBoolean("require-bind", false);
		useBowForce = getConfigBoolean("use-bow-force", true);
		cancelShotOnFail = getConfigBoolean("cancel-shot-on-fail", true);

		minimumForce = getConfigFloat("minimum-force", 0F);
		maximumForce = getConfigFloat("maximum-force", 1F);

		if (minimumForce < 0F) minimumForce = 0F;
		else if (minimumForce > 1F) minimumForce = 1F;
		if (maximumForce < 0F) maximumForce = 0F;
		else if (maximumForce > 1F) maximumForce = 1F;
	}

	@Override
	public void initialize() {
		super.initialize();

		spellOnShoot = initSubspell(spellOnShootName, "BowSpell '" + internalName + "' has an invalid spell defined!");
		spellOnHitEntity = initSubspell(spellOnHitEntityName, "BowSpell '" + internalName + "' has an invalid spell-on-hit-entity defined!");
		spellOnHitGround = initSubspell(spellOnHitGroundName, "BowSpell '" + internalName + "' has an invalid spell-on-hit-ground defined!");

		if (hitListener == null) {
			hitListener = new HitListener();
			registerEvents(hitListener);
		}

		if (!requireBind) registerEvents(new ShootListener());
	}

	@Override
	public void turnOff() {
		super.turnOff();

		hitListener = null;
	}

	@Override
	public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
		return PostCastAction.ALREADY_HANDLED;
	}

	@Override
	public boolean canCastWithItem() {
		return bindable;
	}

	@Override
	public boolean canCastByCommand() {
		return false;
	}

	public boolean isBindRequired() {
		return requireBind;
	}

	public void handleBowCast(EntityShootBowEvent event) {
		if (!cancelShot && event.isCancelled()) return;
		if (!(event.getProjectile() instanceof Arrow)) return;
		if (denyOffhand && event.getHand() == EquipmentSlot.OFF_HAND) return;

		LivingEntity caster = event.getEntity();
		if (!triggerList.canTarget(caster, true)) return;

		if (caster instanceof Player) {
			Spellbook spellbook = MagicSpells.getSpellbook((Player) caster);
			if (!spellbook.hasSpell(this) || !spellbook.canCast(this)) return;
		}

		ItemStack inHand = event.getBow();
		if (inHand == null || (inHand.getType() != Material.BOW && inHand.getType() != Material.CROSSBOW)) return;

		String name = Util.getStringFromComponent(inHand.displayName());
		if (bowNames != null && !bowNames.contains(name)) return;
		if (disallowedBowNames != null && disallowedBowNames.contains(name)) return;
		if (bowName != null && !bowName.isEmpty() && !bowName.equals(name)) return;

		float force = event.getForce();
		if (force < minimumForce || force > maximumForce) return;

		SpellCastEvent castEvent = preCast(caster, useBowForce ? force : 1f, null);
		if (castEvent == null) {
			if (cancelShotOnFail) event.setCancelled(true);
			return;
		}

		if (castEvent.getSpellCastState() == SpellCastState.NORMAL) {
			if (cancelShot) event.setCancelled(true);
			if (!event.isCancelled()) {
				Entity projectile = event.getProjectile();

				ArrowData arrowData = new ArrowData(this, castEvent.getPower());
				List<ArrowData> arrowDataList = null;
				if (projectile.hasMetadata(METADATA_KEY)) {
					List<MetadataValue> metas = projectile.getMetadata(METADATA_KEY);
					for (MetadataValue meta : metas) {
						if (!MagicSpells.plugin.equals(meta.getOwningPlugin())) continue;

						arrowDataList = (List<ArrowData>) meta.value();
						if (arrowDataList != null) arrowDataList.add(arrowData);
						break;
					}
				}

				if (arrowDataList == null) {
					arrowDataList = new ArrayList<>();
					arrowDataList.add(arrowData);

					projectile.setMetadata(METADATA_KEY, new FixedMetadataValue(MagicSpells.plugin, arrowDataList));
				}

				playSpellEffects(EffectPosition.PROJECTILE, projectile);
				playTrackingLinePatterns(EffectPosition.DYNAMIC_CASTER_PROJECTILE_LINE, caster.getLocation(), projectile.getLocation(), caster, projectile);
			}

			if (spellOnShoot != null) spellOnShoot.cast(caster, castEvent.getPower());
		} else if (cancelShotOnFail) event.setCancelled(true);

		postCast(castEvent, PostCastAction.HANDLE_NORMALLY);
	}

	private class ShootListener implements Listener {

		@EventHandler
		public void onArrowLaunch(EntityShootBowEvent event) {
			handleBowCast(event);
		}

	}

	private static class HitListener implements Listener {

		@EventHandler(priority = EventPriority.MONITOR)
		public void onArrowHitGround(ProjectileHitEvent event) {
			if (event.getHitBlock() == null) return;

			Projectile proj = event.getEntity();
			if (!proj.hasMetadata(METADATA_KEY)) return;

			List<MetadataValue> metas = proj.getMetadata(METADATA_KEY);
			boolean remove = false;
			for (MetadataValue meta : metas) {
				if (!MagicSpells.plugin.equals(meta.getOwningPlugin())) continue;

				ProjectileSource shooter = proj.getShooter();
				if (!(shooter instanceof LivingEntity caster)) break;

				List<ArrowData> arrowDataList = (List<ArrowData>) meta.value();
				if (arrowDataList == null || arrowDataList.isEmpty()) break;

				for (ArrowData data : arrowDataList) {
					Subspell groundSpell = data.bowSpell.spellOnHitGround;
					if (groundSpell == null) continue;

					SpellTargetLocationEvent targetLocationEvent = new SpellTargetLocationEvent(data.bowSpell, caster, proj.getLocation(), data.power);
					EventUtil.call(targetLocationEvent);
					if (targetLocationEvent.isCancelled()) continue;

					if (groundSpell.isTargetedLocationSpell())
						groundSpell.castAtLocation(caster, targetLocationEvent.getTargetLocation(), targetLocationEvent.getPower());
					else groundSpell.cast(caster, targetLocationEvent.getPower());

					if (data.bowSpell.removeArrow) remove = true;
				}

				break;
			}

			proj.removeMetadata(METADATA_KEY, MagicSpells.plugin);
			if (remove) proj.remove();
		}

		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onArrowHitEntity(EntityDamageByEntityEvent event) {
			Entity damager = event.getDamager();
			if (!(damager instanceof Arrow)) return;
			if (!damager.hasMetadata(METADATA_KEY)) return;

			List<MetadataValue> metas = damager.getMetadata(METADATA_KEY);
			boolean remove = false;
			for (MetadataValue meta : metas) {
				if (!MagicSpells.plugin.equals(meta.getOwningPlugin())) continue;

				Entity damaged = event.getEntity();
				if (!(damaged instanceof LivingEntity target)) break;

				ProjectileSource shooter = ((Arrow) damager).getShooter();
				if (!(shooter instanceof LivingEntity caster)) break;

				List<ArrowData> arrowDataList = (List<ArrowData>) meta.value();
				if (arrowDataList == null || arrowDataList.isEmpty()) break;

				for (ArrowData data : arrowDataList) {
					Subspell entitySpell = data.bowSpell.spellOnHitEntity;
					if (entitySpell == null) continue;

					SpellTargetEvent targetEvent = new SpellTargetEvent(data.bowSpell, caster, target, data.power);
					EventUtil.call(targetEvent);
					if (targetEvent.isCancelled()) continue;

					target = targetEvent.getTarget();

					if (entitySpell.isTargetedEntityFromLocationSpell())
						entitySpell.castAtEntityFromLocation(caster, caster.getLocation(), target, targetEvent.getPower());
					else if (entitySpell.isTargetedEntitySpell())
						entitySpell.castAtEntity(caster, target, targetEvent.getPower());
					else if (entitySpell.isTargetedLocationSpell())
						entitySpell.castAtLocation(caster, target.getLocation(), targetEvent.getPower());
					else entitySpell.cast(caster, targetEvent.getPower());

					if (data.bowSpell.removeArrow) remove = true;
				}

				break;
			}

			damager.removeMetadata(METADATA_KEY, MagicSpells.plugin);
			if (remove) damager.remove();
		}

	}

	private record ArrowData(BowSpell bowSpell, float power) {

	}

}
