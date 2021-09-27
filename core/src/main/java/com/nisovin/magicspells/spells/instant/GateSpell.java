package com.nisovin.magicspells.spells.instant;

import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.RegexUtil;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spelleffects.EffectPosition;

import io.papermc.lib.PaperLib;

public class GateSpell extends InstantSpell {

	private static final Pattern COORDINATE_PATTERN = Pattern.compile("^-?[0-9]+,[0-9]+,-?[0-9]+(,-?[0-9.]+,-?[0-9.]+)?$");

	private String world;
	private String coordinates;
	private String strGateFailed;

	public GateSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		world = getConfigString("world", "CURRENT");
		coordinates = getConfigString("coordinates", "SPAWN");
		strGateFailed = getConfigString("str-gate-failed", "Unable to teleport.");
	}

	@Override
	public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			World effectiveWorld;
			if (world.equals("CURRENT")) effectiveWorld = caster.getWorld();
			else if (world.equals("DEFAULT")) effectiveWorld = Bukkit.getServer().getWorlds().get(0);
			else effectiveWorld = Bukkit.getServer().getWorld(world);

			if (effectiveWorld == null) {
				MagicSpells.error("GateSpell '" + internalName + "' has a non existent world defined!");
				sendMessage(strGateFailed, caster, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// Get location
			Location location = null;
			coordinates = coordinates.replace(" ", "");

			if (RegexUtil.matches(COORDINATE_PATTERN, coordinates)) {
				String[] c = coordinates.split(",");
				int x = Integer.parseInt(c[0]);
				int y = Integer.parseInt(c[1]);
				int z = Integer.parseInt(c[2]);
				float yaw = 0;
				float pitch = 0;
				if (c.length > 3) {
					yaw = Float.parseFloat(c[3]);
					pitch = Float.parseFloat(c[4]);
				}
				location = new Location(effectiveWorld, x, y, z, yaw, pitch);
			}
			
			if (location == null) {
				switch (coordinates.toUpperCase()) {
					case "SPAWN" -> {
						location = effectiveWorld.getSpawnLocation();
						location = new Location(effectiveWorld, location.getX(), effectiveWorld.getHighestBlockYAt(location) + 1, location.getZ());
					}
					case "EXACTSPAWN" -> location = effectiveWorld.getSpawnLocation();
					case "CURRENT" -> {
						Location l = caster.getLocation();
						location = new Location(effectiveWorld, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getYaw(), l.getPitch());
					}
					default -> {
						MagicSpells.error("GateSpell '" + internalName + "' has invalid coordinates defined!");
						sendMessage(strGateFailed, caster, args);
						return PostCastAction.ALREADY_HANDLED;
					}
				}
			}

			location.setX(location.getX() + .5);
			location.setZ(location.getZ() + .5);
			MagicSpells.debug(3, "Gate location: " + location);
			
			Block b = location.getBlock();
			if (!BlockUtils.isPathable(b) || !BlockUtils.isPathable(b.getRelative(0, 1, 0))) {
				MagicSpells.error("GateSpell '" + internalName + "' has landing spot blocked!");
				sendMessage(strGateFailed, caster, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			Location from = caster.getLocation();
			Location to = b.getLocation();
			boolean canTeleport = (!(caster instanceof Vehicle)) && !caster.isDead();
			if (!canTeleport) {
				MagicSpells.error("GateSpell '" + internalName + "': teleport prevented!");
				sendMessage(strGateFailed, caster, args);
				return PostCastAction.ALREADY_HANDLED;
			}
			PaperLib.teleportAsync(caster, location);

			playSpellEffects(EffectPosition.CASTER, from);
			playSpellEffects(EffectPosition.TARGET, to);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	public static Pattern getCoordinatePattern() {
		return COORDINATE_PATTERN;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public String getStrGateFailed() {
		return strGateFailed;
	}

	public void setStrGateFailed(String strGateFailed) {
		this.strGateFailed = strGateFailed;
	}

}
