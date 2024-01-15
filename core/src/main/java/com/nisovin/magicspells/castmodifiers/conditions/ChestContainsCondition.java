package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicLocation;
import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.magicitems.MagicItemData;

public class ChestContainsCondition extends Condition {

	//world,x,y,z,item

	private MagicLocation location;

	private MagicItemData itemData;

	@Override
	public boolean initialize(@NotNull String var) {
		try {
			String[] vars = var.split(",");
			location = new MagicLocation(vars[0], Integer.parseInt(vars[1]), Integer.parseInt(vars[2]), Integer.parseInt(vars[3]));

			itemData = MagicItems.getMagicItemDataFromString(vars[4].trim());
			return itemData != null;
		} catch (Exception e) {
			DebugHandler.debugGeneral(e);
			return false;
		}
	}

	@Override
	public boolean check(LivingEntity caster) {
		return checkChest();
	}

	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return checkChest();
	}

	@Override
	public boolean check(LivingEntity caster, Location location) {
		return checkChest();
	}

	private boolean checkChest() {
		Block block = location.getLocation().getBlock();
		if (!BlockUtils.isChest(block)) return false;

		for (ItemStack item : ((Chest) block.getState()).getInventory().getContents()) {
			MagicItemData data = MagicItems.getMagicItemDataFromItemStack(item);
			if (data == null) continue;
			if (itemData.matches(data)) return true;
		}

		return false;
	}

}
