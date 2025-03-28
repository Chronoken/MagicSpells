package com.nisovin.magicspells.util;

import java.util.Objects;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;
import org.bukkit.command.BlockCommandSender;

import org.apache.commons.math4.core.jdkmath.AccurateMath;

public class LocationUtil {

	/**
	 * @param string Formatted: "world,x,y,z,yaw,pitch", where x, y, z are
	 *               double values, and yaw & pitch are optional float values.
	 * @return Parsed {@link Location}
	 */
	public static Location fromString(String string) {
		try {
			String[] split = string.split(",");

			World world = Bukkit.getWorld(split[0]);
			double x = Double.parseDouble(split[1]);
			double y = Double.parseDouble(split[2]);
			double z = Double.parseDouble(split[3]);
			float yaw = split.length > 4 ? Float.parseFloat(split[4]) : 0;
			float pitch = split.length > 5 ? Float.parseFloat(split[5]) : 0;

			return new Location(world, x, y, z, yaw, pitch);
		} catch (Exception e) {
			return null;
		}
	}
	
	// -------------------------------------------- //
	// IS SAME X LOGIC
	// -------------------------------------------- //
	
	public static boolean isSameWorld(Object loc1, Object loc2) {
		World world1 = getWorld(loc1);
		if (world1 == null) return false;
		World world2 = getWorld(loc2);
		if (world2 == null) return false;
		return world1.equals(world2);
	}
	
	public static boolean isSameBlock(Object loc1, Object loc2) {
		Location location1 = getLocation(loc1);
		if (location1 == null) return false;
		Location location2 = getLocation(loc2);
		if (location2 == null) return false;
		if (!Objects.equals(location1.getWorld(), location2.getWorld())) return false;
		if (location1.getBlockX() != location2.getBlockX()) return false;
		if (location1.getBlockY() != location2.getBlockY()) return false;
		if (location1.getBlockZ() != location2.getBlockZ()) return false;
		return true;
	}
	
	public static boolean isSameChunk(Object one, Object two) {
		Location location1 = getLocation(one);
		if (location1 == null) return false;
		Location location2 = getLocation(two);
		if (location2 == null) return false;
		if (location1.getBlockX() >> 4 != location2.getBlockX() >> 4) return false;
		if (location1.getBlockY() >> 4 != location2.getBlockY() >> 4) return false;
		if (location1.getBlockZ() >> 4 != location2.getBlockZ() >> 4) return false;
		return Objects.equals(location1.getWorld(), location2.getWorld());
	}
	
	// -------------------------------------------- //
	// DISTANCE
	// -------------------------------------------- //
	
	// Returns -1.0 if something didn't work
	// TODO see if this should do the vector convert first for cross world
	public static double distanceSquared(Object one, Object two) {
		Location location1 = getLocation(one);
		if (location1 == null) return -1D;
		Location location2 = getLocation(two);
		if (location2 == null) return -1D;
		
		try {
			return location1.distanceSquared(location2);
		} catch (Exception exception) {
			// In case we had some issue with distances between two worlds
			return -1D;
		}
	}
	
	public static boolean distanceLessThan(Object one, Object two, double distance) {
		double actualDistanceSquared = distanceSquared(one, two);
		if (actualDistanceSquared == -1D) return false;
		return actualDistanceSquared < distance * distance;
	}
	
	public static boolean distanceGreaterThan(Object one, Object two, double distance) {
		double actualDistanceSquared = distanceSquared(one, two);
		if (actualDistanceSquared == -1D) return false;
		return actualDistanceSquared > distance * distance;
	}
	
	// -------------------------------------------- //
	// COMMON COMBINED LOGIC
	// -------------------------------------------- //
	
	// Are the locations in a different world or further away than distance
	// Returns false if either of the locations are null
	public static boolean differentWorldDistanceGreaterThan(Object location1, Object location2, double distance) {
		Location loc1 = getLocation(location1);
		if (loc1 == null) return false;
		Location loc2 = getLocation(location2);
		if (loc2 == null) return false;
		if (!isSameWorld(loc1, loc2)) return true;
		return distanceGreaterThan(loc1, loc2, distance);
	}
	
	// -------------------------------------------- //
	// EXTRACTOR LOGIC
	// -------------------------------------------- //
	
	// This should redirect to other internal methods depending on what the type of object is
	public static Location getLocation(Object object) {
		return switch (object) {
			case Location location -> location;
			case Entity entity -> entity.getLocation();
			case Block block -> block.getLocation();
			case BlockCommandSender blockCommandSender -> getLocation(blockCommandSender.getBlock());
			case null, default -> null;
		};
	}
	
	// This should redirect to other internal methods depending on what the type of object is
	public static World getWorld(Object object) {
		return switch (object) {
			case World world -> world;
			case Location location -> location.getWorld();
			case String s -> Bukkit.getWorld(s);
			case Entity entity -> entity.getWorld();
			case Block block -> block.getWorld();
			case BlockCommandSender blockCommandSender -> getWorld(blockCommandSender.getBlock());
			case null, default -> null;
		};
	}

	// setDirection function with fast math
	public static Location setDirection(Location loc, Vector v) {
		final double _2PI = 2 * AccurateMath.PI;
		final double x = v.getX();
		final double z = v.getZ();

		if (x == 0 && z == 0) {
			loc.setPitch(v.getY() > 0 ? -90 : 90);
			return loc;
		}

		double theta = AccurateMath.atan2(-x, z);
		loc.setYaw((float) AccurateMath.toDegrees((theta + _2PI) % _2PI));

		double x2 = NumberConversions.square(x);
		double z2 = NumberConversions.square(z);
		double xz = Math.sqrt(x2 + z2);
		loc.setPitch((float) AccurateMath.toDegrees(AccurateMath.atan(-v.getY() / xz)));

		return loc;
	}

	// Returns a vector pointing from startLoc to endLoc
	public static Vector getDirection(Location startLoc, Location endLoc) {
		return endLoc.toVector().subtract(startLoc.toVector()).normalize();
	}

}
