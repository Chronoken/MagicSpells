package com.nisovin.magicspells.spells.buff;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.config.ConfigData;

public class GillsSpell extends BuffSpell {

	private static final NamespacedKey MARKER = new NamespacedKey(MagicSpells.getInstance(), "gill_spell_item");

	private final Map<UUID, GillData> entities;

	private final ConfigData<Material> headMaterial;

	private final ConfigData<Boolean> headEffect;
	private final ConfigData<Boolean> refillAirBar;

	public GillsSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		headMaterial = getConfigDataMaterial("head-block", Material.GLASS);

		headEffect = getConfigDataBoolean("head-effect", true);
		refillAirBar = getConfigDataBoolean("refill-air-bar", true);

		entities = new HashMap<>();
	}

	@Override
	public boolean castBuff(SpellData data) {
		GillData gillData;
		if (headEffect.get(data)) {
			Material headMaterial = this.headMaterial.get(data);
			if (headMaterial.getEquipmentSlot() != EquipmentSlot.HEAD && !headMaterial.isBlock()) return false;

			EntityEquipment eq = data.target().getEquipment();
			if (eq == null) return false;

			ItemStack item = new ItemStack(headMaterial);
			item.editMeta(meta -> meta.getPersistentDataContainer().set(MARKER, PersistentDataType.BYTE, (byte) 1));

			gillData = new GillData(refillAirBar.get(data), true, eq.getHelmet());
			eq.setHelmet(item);
		} else gillData = new GillData(refillAirBar.get(data), false, null);

		entities.put(data.target().getUniqueId(), gillData);
		return true;
	}

	@Override
	public boolean recastBuff(SpellData data) {
		turnOffBuff(data.target());
		return castBuff(data);
	}

	@Override
	public boolean isActive(LivingEntity entity) {
		return entities.containsKey(entity.getUniqueId());
	}

	@Override
	public void turnOffBuff(LivingEntity entity) {
		GillData data = entities.remove(entity.getUniqueId());
		if (data != null && data.headEffect) {
			EntityEquipment eq = entity.getEquipment();
			if (eq == null) return;

			eq.setHelmet(data.helmet);
		}
	}

	@Override
	protected void turnOff() {
		for (UUID uuid : entities.keySet()) {
			Entity entity = Bukkit.getEntity(uuid);
			if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isValid()) continue;

			turnOffBuff(livingEntity);
		}

		entities.clear();
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() != DamageCause.DROWNING) return;

		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity livingEntity) || !isActive(livingEntity)) return;

		if (isExpired(livingEntity)) {
			turnOff(livingEntity);
			return;
		}

		event.setCancelled(true);
		addUseAndChargeCost(livingEntity);

		GillData data = entities.get(livingEntity.getUniqueId());
		if (data.refillAirBar) livingEntity.setRemainingAir(livingEntity.getMaximumAir());
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) return;

		GillData data = entities.get(event.getPlayer().getUniqueId());
		if (data == null || !data.headEffect) return;

		EquipmentSlot slot = event.getMaterial().getEquipmentSlot();
		if (slot == EquipmentSlot.HEAD) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getSlotType() != InventoryType.SlotType.ARMOR || event.getRawSlot() != 5) return;

		GillData data = entities.get(event.getWhoClicked().getUniqueId());
		if (data != null && data.headEffect) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getInventory().getType() != InventoryType.PLAYER) return;

		HumanEntity entity = event.getWhoClicked();
		if (!isActive(entity)) return;

		Set<Integer> slots = event.getRawSlots();
		if (slots.contains(5)) event.setCancelled(true);
	}

	public Map<UUID, GillData> getEntities() {
		return entities;
	}

	public record GillData(boolean refillAirBar, boolean headEffect, ItemStack helmet) {
	}

}
