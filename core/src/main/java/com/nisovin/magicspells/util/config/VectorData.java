package com.nisovin.magicspells.util.config;

import org.bukkit.util.Vector;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.util.MagicConfig;

public class VectorData extends ConfigData<Vector> {

	private DoubleData x;
	private DoubleData y;
	private DoubleData z;

	public VectorData(MagicConfig config, String path, String def) {
		String vectorString = config.getString(path, def);
		String[] data = vectorString.split(",");

		if (data.length != 3) return;

		x = createDouble(data[0]);
		y = createDouble(data[1]);
		z = createDouble(data[2]);
	}

	@Override
	public Vector get(LivingEntity caster) {
		if (x != null && y != null && z != null) return new Vector(x.get(caster), y.get(caster), z.get(caster));

		return null;
	}

	private static DoubleData createDouble(String data) {
		Double value = null;
		try {
			value = Double.parseDouble(data);
		} catch (NumberFormatException e) {
		}

		return new DoubleData(value == null ? data : null, value);
	}

}
