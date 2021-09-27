package com.nisovin.magicspells.castmodifiers.customdata;

public class CustomDataFloat extends CustomData {

	private String invalidText;
	private float customData;
	private boolean isValid = false;

	public CustomDataFloat(String data) {
		if (data == null) {
			invalidText = "Number was not defined.";
			return;
		}

		try {
			customData = Float.parseFloat(data);
			isValid = true;
		} catch (NumberFormatException ignore) {
			invalidText = "Number formatting is invalid.";
		}
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Override
	public String getInvalidText() {
		return invalidText;
	}

	public float getCustomData() {
		return customData;
	}

	public static float from(CustomData data) {
		return ((CustomDataFloat) data).getCustomData();
	}

}
