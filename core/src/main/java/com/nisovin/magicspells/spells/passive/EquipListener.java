package com.nisovin.magicspells.spells.passive;

import java.util.Set;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.OverridePriority;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.magicitems.MagicItemData;
import com.nisovin.magicspells.spells.passive.util.PassiveListener;

public class EquipListener extends PassiveListener {

	private final Set<MagicItemData> items = new HashSet<>();

	@Override
	public void initialize(String var) {
		if (var == null || var.isEmpty()) return;

		String[] split = var.split("\\|");
		for (String s : split) {
			s = s.trim();

			MagicItemData itemData = MagicItems.getMagicItemDataFromString(s);
			if (itemData == null) {
				MagicSpells.error("Invalid magic item '" + s + "' in equip trigger on passive spell '" + passiveSpell.getInternalName() + "'");
				continue;
			}

			items.add(itemData);
		}
	}

	@OverridePriority
	@EventHandler
	public void onEquip(PlayerArmorChangeEvent event) {
		Player caster = event.getPlayer();
		if (!canTrigger(caster) || !hasSpell(caster)) return;

		if (!items.isEmpty()) {
			ItemStack newItem = event.getNewItem();
			if (newItem == null) return;

			MagicItemData newData = MagicItems.getMagicItemDataFromItemStack(newItem);
			if (newData == null) return;

			ItemStack oldItem = event.getOldItem();
			MagicItemData oldData = MagicItems.getMagicItemDataFromItemStack(oldItem);

			if (!contains(oldData, newData)) return;
		}

		passiveSpell.activate(caster);
	}

	private boolean contains(MagicItemData oldData, MagicItemData newData) {
		for (MagicItemData data : items)
			if ((oldData == null || !data.matches(oldData)) && data.matches(newData))
				return true;

		return false;
	}

}
