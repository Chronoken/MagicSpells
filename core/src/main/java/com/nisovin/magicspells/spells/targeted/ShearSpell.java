package com.nisovin.magicspells.spells.targeted;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

//This spell currently support the shearing of sheep at the moment.
//Future tweaks for the shearing of other mobs will be added.

public class ShearSpell extends TargetedSpell implements TargetedEntitySpell {

	private static final DyeColor[] colors = DyeColor.values();

	private DyeColor dye;

	private String requestedColor;

	private int minWool;
	private int maxWool;

	private double dropOffset;

	private boolean forceWoolColor;
	private boolean randomWoolColor;
	private boolean configuredCorrectly;

	public ShearSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		requestedColor = getConfigString("wool-color", "");

		minWool = getConfigInt("min-wool-drop", 1);
		maxWool = getConfigInt("max-wool-drop", 3);

		dropOffset = getConfigDouble("drop-offset", 1);

		forceWoolColor = getConfigBoolean("force-wool-color", false);
		randomWoolColor = getConfigBoolean("random-wool-color", false);
	}

	@Override
	public void initialize() {
		super.initialize();

		configuredCorrectly = parseSpell();
		if (!configuredCorrectly) MagicSpells.error("ShearSpell " + internalName + " was configured incorrectly!");
	}

	@Override
	public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			TargetInfo<LivingEntity> target = getTargetedEntity(caster, power);
			if (target == null) return PostCastAction.ALREADY_HANDLED;
			if (!(target.getTarget() instanceof Sheep)) return PostCastAction.ALREADY_HANDLED;

			boolean done = shear((Sheep) target.getTarget());
			if (!done) return noTarget(caster);

			sendMessages(caster, target.getTarget(), args);
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
		if (!(target instanceof Sheep)) return false;
		return shear((Sheep) target);
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (!(target instanceof Sheep)) return false;
		return shear((Sheep) target);
	}

	private boolean parseSpell() {
		if (forceWoolColor && requestedColor != null) {
			try {
			  dye = DyeColor.valueOf(requestedColor.toUpperCase());
			} catch (IllegalArgumentException e) {
				MagicSpells.error("Invalid wool color defined. Will use sheep's color instead.");
				requestedColor = null;
				return false;
			}
		}
		return true;
	}

	private boolean shear(Sheep sheep) {
		if (!configuredCorrectly) return false;
		if (sheep.isSheared()) return false;
		if (!sheep.isAdult()) return false;

		DyeColor color = null;
		if (forceWoolColor) {
			if (randomWoolColor) color = colors[Util.getRandomInt(colors.length)];
			else if (dye != null) color = dye;
		}
		else color = sheep.getColor();
		if (color == null) color = DyeColor.WHITE;
		Material woolColor = Material.getMaterial(color.name() + "_WOOL");
		if (woolColor == null) woolColor = Material.WHITE_WOOL;

		int count;
		if (maxWool != 0) count = random.nextInt((maxWool - minWool) + 1) + minWool;
		else count = random.nextInt(minWool + 1);

		sheep.setSheared(true);
		sheep.getWorld().dropItemNaturally(sheep.getLocation().add(0, dropOffset, 0), new ItemStack(woolColor, count));
		return true;
	}

}
