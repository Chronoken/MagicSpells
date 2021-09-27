package com.nisovin.magicspells.util;

import java.util.function.Consumer;

import com.nisovin.magicspells.MagicSpells;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;

public class DataUtil {

	private static NamespacedKey createKey(String key) {
		return new NamespacedKey(MagicSpells.getInstance(), key);
	}

	private static Object get(ItemStack item, String key, PersistentDataType<?, ?> type) {
		return item.getItemMeta().getPersistentDataContainer().get(createKey(key), type);
	}

	private static void handle(ItemStack item, Consumer<PersistentDataContainer> consumer) {
		ItemMeta meta = item.getItemMeta();
		consumer.accept(meta.getPersistentDataContainer());
		item.setItemMeta(meta);
	}

	private static <T, Z> void set(ItemStack item, String key, PersistentDataType<T, Z> type, Z value) {
		handle(item, container -> container.set(createKey(key), type, value));
	}

	public static void remove(ItemStack item, String key) {
		handle(item, container -> container.remove(createKey(key)));
	}

	public static String getString(ItemStack item, String key) {
		return (String) get(item, key, PersistentDataType.STRING);
	}

	public static void setString(ItemStack item, String key, String value) {
		set(item, key, PersistentDataType.STRING, value);
	}

	public static boolean getBool(ItemStack item, String key) {
		Byte old = (Byte) get(item, key, PersistentDataType.BYTE);
		return old != null && old == 1;
	}

	public static void setBoolean(ItemStack item, String key, boolean value) {
		set(item, key, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
	}

}
