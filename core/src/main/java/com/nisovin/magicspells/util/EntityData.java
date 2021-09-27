package com.nisovin.magicspells.util;

// this should probably be kept as a star import for version safety
import org.bukkit.entity.*;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.util.EulerAngle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.MagicSpells;

public class EntityData {

	private Vector relativeOffset;

	private EntityType entityType;

	private Material material;
	private DyeColor dyeColor;
	private DyeColor patternDyeColor;

	private Cat.Type catType;
	private Fox.Type foxType;
	private Panda.Gene mainGene;
	private Panda.Gene hiddenGene;
	private Horse.Color horseColor;
	private Horse.Style horseStyle;
	private Rabbit.Type rabbitType;
	private Llama.Color llamaColor;
	private Parrot.Variant parrotVariant;
	private Villager.Profession profession;
	private MushroomCow.Variant cowVariant;
	private TropicalFish.Pattern fishPattern;

	private EulerAngle headAngle;
	private EulerAngle bodyAngle;
	private EulerAngle leftArmAngle;
	private EulerAngle rightArmAngle;
	private EulerAngle leftLegAngle;
	private EulerAngle rightLegAngle;

	private boolean baby;
	private boolean tamed;
	private boolean angry;
	private boolean small;
	private boolean marker;
	private boolean visible;
	private boolean hasArms;
	private boolean powered;
	private boolean chested;
	private boolean saddled;
	private boolean sheared;
	private boolean hasBasePlate;

	private int size;

	private boolean isMob = false;
	private boolean isMisc = false;
	private boolean isPlayer = false;

	public EntityData(ConfigurationSection section) {
		if (section == null) throw new NullPointerException("section");

		relativeOffset = Util.getVector(section.getString("relative-offset", "0,0,0"));

		size = section.getInt("size", 0);

		baby = section.getBoolean("baby", false);
		tamed = section.getBoolean("tamed", false);
		angry = section.getBoolean("angry", false);
		small = section.getBoolean("small", false);
		marker = section.getBoolean("marker", false);
		visible = section.getBoolean("visible", true);
		hasArms = section.getBoolean("has-arms", true);
		chested = section.getBoolean("chested", false);
		powered = section.getBoolean("powered", false);
		saddled = section.getBoolean("saddled", false);
		sheared = section.getBoolean("sheared", false);
		hasBasePlate = section.getBoolean("has-base-plate", true);

		Vector head = Util.getVector(section.getString("head-angle", "0,0,0"));
		Vector body = Util.getVector(section.getString("body-angle", "0,0,0"));
		Vector leftArm = Util.getVector(section.getString("left-arm-angle", "0,0,0"));
		Vector rightArm = Util.getVector(section.getString("right-arm-angle", "0,0,0"));
		Vector leftLeg = Util.getVector(section.getString("left-leg-angle", "0,0,0"));
		Vector rightLeg = Util.getVector(section.getString("right-leg-angle", "0,0,0"));

		headAngle = new EulerAngle(head.getX(), head.getY(), head.getZ());
		bodyAngle = new EulerAngle(body.getX(), body.getY(), body.getZ());
		leftArmAngle = new EulerAngle(leftArm.getX(), leftArm.getY(), leftArm.getZ());
		rightArmAngle = new EulerAngle(rightArm.getX(), rightArm.getY(), rightArm.getZ());
		leftLegAngle = new EulerAngle(leftLeg.getX(), leftLeg.getY(), leftLeg.getZ());
		rightLegAngle = new EulerAngle(rightLeg.getX(), rightLeg.getY(), rightLeg.getZ());

		String mat = section.getString("material", "");
		String type = section.getString("type", "");
		String color = section.getString("color", "");
		String patternColor = section.getString("pattern-color", "");
		String style = section.getString("style", "");
		String entity = section.getString("entity", "");
		String mainGeneType = section.getString("main-gene", "");
		String hiddenGeneType = section.getString("hidden-gene", "");
		switch (entity.toLowerCase()) {
			case "player" -> {
				entityType = EntityType.PLAYER;
				isPlayer = true;
			}
			case "pillager" -> {
				entityType = EntityType.PILLAGER;
				isMob = true;
			}
			case "ravager" -> {
				entityType = EntityType.RAVAGER;
				isMob = true;
			}
			case "trader_llama" -> {
				entityType = EntityType.TRADER_LLAMA;
				isMob = true;
			}
			case "wandering_trader" -> {
				entityType = EntityType.WANDERING_TRADER;
				isMob = true;
			}
			case "hoglin" -> {
				entityType = EntityType.HOGLIN;
				isMob = true;
			}
			case "piglin" -> {
				entityType = EntityType.PIGLIN;
				isMob = true;
			}
			case "piglin_brute" -> {
				entityType = EntityType.PIGLIN_BRUTE;
				isMob = true;
			}
			case "zoglin" -> {
				entityType = EntityType.ZOGLIN;
				isMob = true;
			}
			case "strider" -> {
				entityType = EntityType.STRIDER;
				isMob = true;
			}
			case "bee" -> {
				entityType = EntityType.BEE;
				isMob = true;
			}
			case "wither_skeleton" -> {
				entityType = EntityType.WITHER_SKELETON;
				isMob = true;
			}
			case "zombie_villager" -> {
				entityType = EntityType.ZOMBIE_VILLAGER;
				isMob = true;
			}
			case "creeper" -> {
				entityType = EntityType.CREEPER;
				isMob = true;
			}
			case "panda" -> {
				entityType = EntityType.PANDA;
				isMob = true;
				try {
					mainGene = Panda.Gene.valueOf(mainGeneType.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid panda main gene: " + mainGeneType);
					mainGene = null;
				}
				try {
					hiddenGene = Panda.Gene.valueOf(hiddenGeneType.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid panda hidden gene: " + hiddenGeneType);
					hiddenGene = null;
				}
			}
			case "villager" -> {
				entityType = EntityType.VILLAGER;
				isMob = true;
				try {
					profession = Villager.Profession.valueOf(type.toUpperCase());
				} catch (Exception e) {
					MagicSpells.error("Invalid villager profession: " + type);
					profession = null;
				}
			}
			case "sheep" -> {
				entityType = EntityType.SHEEP;
				isMob = true;
				try {
					dyeColor = DyeColor.valueOf(color.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid sheep color: " + color);
					dyeColor = null;
				}
			}
			case "rabbit" -> {
				entityType = EntityType.RABBIT;
				isMob = true;
				try {
					rabbitType = Rabbit.Type.valueOf(type.toUpperCase());
				} catch (Exception e) {
					MagicSpells.error("Invalid rabbit type: " + type);
					rabbitType = null;
				}
			}
			case "wolf" -> {
				entityType = EntityType.WOLF;
				isMob = true;
				try {
					dyeColor = DyeColor.valueOf(color.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid wolf collar color: " + color);
					dyeColor = null;
				}
			}
			case "fox" -> {
				entityType = EntityType.FOX;
				isMob = true;
				try {
					foxType = Fox.Type.valueOf(type.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid fox type: " + type);
					foxType = null;
				}
			}
			case "cat" -> {
				entityType = EntityType.CAT;
				isMob = true;
				try {
					catType = Cat.Type.valueOf(type.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid cat type: " + type);
					catType = null;
				}
			}
			case "pig" -> {
				entityType = EntityType.PIG;
				isMob = true;
			}
			case "iron_golem" -> {
				entityType = EntityType.IRON_GOLEM;
				isMob = true;
			}
			case "mooshroom" -> {
				entityType = EntityType.MUSHROOM_COW;
				isMob = true;
				try {
					cowVariant = MushroomCow.Variant.valueOf(type.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid mooshroom type: " + type);
					cowVariant = null;
				}
			}
			case "magma_cube" -> {
				entityType = EntityType.MAGMA_CUBE;
				isMob = true;
			}
			case "ocelot" -> {
				entityType = EntityType.OCELOT;
				isMob = true;
			}
			case "snow_golem" -> {
				entityType = EntityType.SNOWMAN;
				isMob = true;
			}
			case "wither" -> {
				entityType = EntityType.WITHER;
				isMob = true;
			}
			case "ender_dragon" -> {
				entityType = EntityType.ENDER_DRAGON;
				isMob = true;
			}
			case "horse" -> {
				entityType = EntityType.HORSE;
				isMob = true;
				try {
					horseColor = Horse.Color.valueOf(color.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid horse color: " + color);
					horseColor = null;
				}
				try {
					horseStyle = Horse.Style.valueOf(style.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid horse style: " + style);
					horseStyle = null;
				}
			}
			case "skeleton_horse" -> {
				entityType = EntityType.SKELETON_HORSE;
				isMob = true;
			}
			case "zombie_horse" -> {
				entityType = EntityType.ZOMBIE_HORSE;
				isMob = true;
			}
			case "mule" -> {
				entityType = EntityType.MULE;
				isMob = true;
			}
			case "donkey" -> {
				entityType = EntityType.DONKEY;
				isMob = true;
			}
			case "elder_guardian" -> {
				entityType = EntityType.ELDER_GUARDIAN;
				isMob = true;
			}
			case "slime" -> {
				entityType = EntityType.SLIME;
				isMob = true;
			}
			case "cod" -> {
				entityType = EntityType.COD;
				isMob = true;
			}
			case "salmon" -> {
				entityType = EntityType.SALMON;
				isMob = true;
			}
			case "pufferfish" -> {
				entityType = EntityType.PUFFERFISH;
				isMob = true;
			}
			case "tropical_fish" -> {
				entityType = EntityType.TROPICAL_FISH;
				isMob = true;
				try {
					fishPattern = TropicalFish.Pattern.valueOf(type.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid fish pattern: " + type);
					fishPattern = null;
				}
				try {
					dyeColor = DyeColor.valueOf(color.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid fish body color: " + color);
					dyeColor = null;
				}
				try {
					patternDyeColor = DyeColor.valueOf(patternColor.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid fish pattern color: " + patternColor);
					patternDyeColor = null;
				}
			}
			case "llama" -> {
				entityType = EntityType.LLAMA;
				isMob = true;
				try {
					llamaColor = Llama.Color.valueOf(color.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid llama color: " + color);
					llamaColor = null;
				}
				material = Util.getMaterial(mat);
				if (material == null) {
					MagicSpells.error("Invalid llama material: " + mat);
					material = null;
				}
			}
			case "polar_bear" -> {
				entityType = EntityType.POLAR_BEAR;
				isMob = true;
			}
			case "armor_stand" -> {
				entityType = EntityType.ARMOR_STAND;
				isMisc = true;
			}
			case "bat" -> {
				entityType = EntityType.BAT;
				isMob = true;
			}
			case "blaze" -> {
				entityType = EntityType.BLAZE;
				isMob = true;
			}
			case "dolphin" -> {
				entityType = EntityType.DOLPHIN;
				isMob = true;
			}
			case "enderman" -> {
				entityType = EntityType.ENDERMAN;
				isMob = true;
				material = Util.getMaterial(mat);
				if (material == null) {
					MagicSpells.error("Invalid enderman material: " + mat);
					material = null;
				}
			}
			case "ghast" -> {
				entityType = EntityType.GHAST;
				isMob = true;
			}
			case "guardian" -> {
				entityType = EntityType.GUARDIAN;
				isMob = true;
			}
			case "illusioner" -> {
				entityType = EntityType.ILLUSIONER;
				isMob = true;
			}
			case "vindicator" -> {
				entityType = EntityType.VINDICATOR;
				isMob = true;
			}
			case "evoker" -> {
				entityType = EntityType.EVOKER;
				isMob = true;
			}
			case "skeleton" -> {
				entityType = EntityType.SKELETON;
				isMob = true;
			}
			case "spider" -> {
				entityType = EntityType.SPIDER;
				isMob = true;
			}
			case "cave_spider" -> {
				entityType = EntityType.CAVE_SPIDER;
				isMob = true;
			}
			case "giant" -> {
				entityType = EntityType.GIANT;
				isMob = true;
			}
			case "zombie" -> {
				entityType = EntityType.ZOMBIE;
				isMob = true;
			}
			case "zombie_pigman", "zombified_piglin" -> {
				entityType = MobUtil.getPigZombieEntityType();
				isMob = true;
			}
			case "silverfish" -> {
				entityType = EntityType.SILVERFISH;
				isMob = true;
			}
			case "witch" -> {
				entityType = EntityType.WITCH;
				isMob = true;
			}
			case "endermite" -> {
				entityType = EntityType.ENDERMITE;
				isMob = true;
			}
			case "shulker" -> {
				entityType = EntityType.SHULKER;
				isMob = true;
			}
			case "cow" -> {
				entityType = EntityType.COW;
				isMob = true;
			}
			case "chicken" -> {
				entityType = EntityType.CHICKEN;
				isMob = true;
			}
			case "squid" -> {
				entityType = EntityType.SQUID;
				isMob = true;
			}
			case "parrot" -> {
				entityType = EntityType.PARROT;
				isMob = true;
				try {
					parrotVariant = Parrot.Variant.valueOf(type.toUpperCase());
				} catch (Exception exception) {
					MagicSpells.error("Invalid parrot variant: " + type);
					parrotVariant = null;
				}
			}
			case "turtle" -> {
				entityType = EntityType.TURTLE;
				isMob = true;
			}
			case "phantom" -> {
				entityType = EntityType.PHANTOM;
				isMob = true;
			}
			case "drowned" -> {
				entityType = EntityType.DROWNED;
				isMob = true;
			}
			case "falling_block" -> {
				entityType = EntityType.FALLING_BLOCK;
				isMisc = true;
				material = Util.getMaterial(mat);
				if (material == null || !material.isBlock()) {
					MagicSpells.error("Invalid falling block material: " + mat);
					material = null;
				}
			}
			case "item" -> {
				entityType = EntityType.DROPPED_ITEM;
				isMisc = true;
				material = Util.getMaterial(mat);
				if (material == null) MagicSpells.error("Invalid item material: " + mat);
			}
		}
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	public Material getMaterial() {
		return material;
	}

	public boolean isPlayer() {
		return isPlayer;
	}

	public boolean isMob() {
		return isMob;
	}

	public boolean isMisc() {
		return isMisc;
	}

	public boolean isBaby() {
		return baby;
	}

	public boolean isTamed() {
		return tamed;
	}

	public boolean isAngry() {
		return angry;
	}

	public boolean isPowered() {
		return powered;
	}

	public boolean isChested() {
		return chested;
	}

	public boolean isSaddled() {
		return saddled;
	}

	public boolean isSheared() {
		return sheared;
	}

	public int getSize() {
		return size;
	}

	public Fox.Type getFoxType() {
		return foxType;
	}

	public Villager.Profession getProfession() {
		return profession;
	}

	public Horse.Color getHorseColor() {
		return horseColor;
	}

	public Horse.Style getHorseStyle() {
		return horseStyle;
	}

	public DyeColor getDyeColor() {
		return dyeColor;
	}

	public DyeColor getPatternDyeColor() {
		return patternDyeColor;
	}

	public TropicalFish.Pattern getFishPattern() {
		return fishPattern;
	}

	public Llama.Color getLlamaColor() {
		return llamaColor;
	}

	public Parrot.Variant getParrotVariant() {
		return parrotVariant;
	}

	public Rabbit.Type getRabbitType() {
		return rabbitType;
	}

	public Cat.Type getCatType() {
		return catType;
	}

	public MushroomCow.Variant getCowVariant() {
		return cowVariant;
	}

	public Panda.Gene getMainGene() {
		return mainGene;
	}

	public Panda.Gene getHiddenGene() {
		return hiddenGene;
	}

	public Entity spawn(Location location) {
		if (location == null) throw new NullPointerException("location");

		Location startLoc = location.clone();
		Vector dir = startLoc.getDirection().normalize();

		Vector horizOffset = new Vector(-dir.getZ(), 0, dir.getX()).normalize();
		startLoc.add(horizOffset.multiply(relativeOffset.getZ())).getBlock().getLocation();
		startLoc.add(startLoc.getDirection().clone().multiply(relativeOffset.getX()));
		startLoc.setY(startLoc.getY() + relativeOffset.getY());
		
		Entity entity = switch (entityType) {
			case FALLING_BLOCK -> startLoc.getWorld().spawnFallingBlock(startLoc, material.createBlockData());
			case DROPPED_ITEM -> startLoc.getWorld().dropItem(startLoc, new ItemStack(material));
			default -> startLoc.getWorld().spawnEntity(startLoc, entityType);
		};

		if (entity instanceof Ageable) {
			if (isBaby()) ((Ageable) entity).setBaby();
			else ((Ageable) entity).setAdult();
		}

		if (entity instanceof AbstractHorse && isSaddled()) ((AbstractHorse) entity).getInventory().setSaddle(new ItemStack(Material.SADDLE));

		if (entity instanceof ChestedHorse) ((ChestedHorse) entity).setCarryingChest(isChested());

		if (entity instanceof Slime) ((Slime) entity).setSize(getSize());

		if (entity instanceof Phantom) ((Phantom) entity).setSize(getSize());

		switch (entityType) {
			case ARMOR_STAND -> {
				((ArmorStand) entity).setSmall(small);
				((ArmorStand) entity).setArms(hasArms);
				((ArmorStand) entity).setMarker(marker);
				((ArmorStand) entity).setVisible(visible);
				((ArmorStand) entity).setBasePlate(hasBasePlate);
				((ArmorStand) entity).setHeadPose(headAngle);
				((ArmorStand) entity).setBodyPose(bodyAngle);
				((ArmorStand) entity).setLeftArmPose(leftArmAngle);
				((ArmorStand) entity).setRightArmPose(rightArmAngle);
				((ArmorStand) entity).setLeftLegPose(leftLegAngle);
				((ArmorStand) entity).setRightLegPose(rightLegAngle);
			}
			case ZOMBIE -> {
				if (isBaby()) ((Ageable) entity).setBaby();
			}
			case CREEPER -> ((Creeper) entity).setPowered(isPowered());
			case ENDERMAN -> ((Enderman) entity).setCarriedBlock(material.createBlockData());
			case WOLF -> {
				((Wolf) entity).setAngry(isAngry());
				((Wolf) entity).setTamed(isTamed());
				if (isTamed()) ((Wolf) entity).setCollarColor(getDyeColor());
			}
			case VILLAGER -> ((Villager) entity).setProfession(getProfession());
			case PIG -> ((Pig) entity).setSaddle(isSaddled());
			case SHEEP -> {
				((Sheep) entity).setSheared(isSheared());
				((Sheep) entity).setColor(getDyeColor());
			}
			case RABBIT -> ((Rabbit) entity).setRabbitType(getRabbitType());
			case HORSE -> {
				((Horse) entity).setColor(getHorseColor());
				((Horse) entity).setStyle(getHorseStyle());
			}
			case LLAMA -> {
				((Llama) entity).setColor(getLlamaColor());
				((Llama) entity).getInventory().setDecor(new ItemStack(getMaterial()));
			}
			case PUFFERFISH -> ((PufferFish) entity).setPuffState(getSize());
			case TROPICAL_FISH -> {
				((TropicalFish) entity).setBodyColor(getDyeColor());
				((TropicalFish) entity).setPatternColor(getPatternDyeColor());
				((TropicalFish) entity).setPattern(getFishPattern());
			}
			case PARROT -> ((Parrot) entity).setVariant(getParrotVariant());
			case FOX -> ((Fox) entity).setFoxType(getFoxType());
			case CAT -> ((Cat) entity).setCatType(getCatType());
			case MUSHROOM_COW -> ((MushroomCow) entity).setVariant(getCowVariant());
			case PANDA -> {
				((Panda) entity).setMainGene(getMainGene());
				((Panda) entity).setHiddenGene(getMainGene());
			}
		}

		return entity;
	}

}
