package com.nisovin.magicspells.util;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class MobUtil {

	static Map<String, EntityType> entityTypeMap = new HashMap<>();

	static {
		for (EntityType type : EntityType.values()) {
			if (type == null) continue;
			if (type == EntityType.UNKNOWN) continue;

			entityTypeMap.put(type.name().toLowerCase(), type);
			entityTypeMap.put(type.name().toLowerCase().replace("_", ""), type);

			entityTypeMap.put(type.getKey().getKey(), type);
			entityTypeMap.put(type.getKey().getKey().replace("_", ""), type);
		}

		Map<String, EntityType> types = new HashMap<>();
		for (Map.Entry<String, EntityType> entry : entityTypeMap.entrySet()) {
			types.put(entry.getKey() + 's', entry.getValue());
		}
		entityTypeMap.putAll(types);
	}

	public static EntityType getEntityType(String type) {
		if (type.equalsIgnoreCase("player")) return EntityType.PLAYER;
		return entityTypeMap.get(type.toLowerCase());
	}

	public static ItemStack getEggItemForEntityType(EntityType type) {
		Material eggMaterial = Util.getMaterial(type.getKey().getKey() + "_SPAWN_EGG");
		if (eggMaterial == null) return null;

		return new ItemStack(eggMaterial);
	}

	public static void setTarget(LivingEntity mob, LivingEntity target) {
		if (!(mob instanceof Creature creature)) return;
		creature.setTarget(target);
	}

	public static EntityType getPigZombieEntityType() {
		try {
			return EntityType.valueOf("ZOMBIFIED_PIGLIN");
		} catch (IllegalArgumentException ex) {
			return EntityType.valueOf("PIG_ZOMBIE");
		}
	}

}
