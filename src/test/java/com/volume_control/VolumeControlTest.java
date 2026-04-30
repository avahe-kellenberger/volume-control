package com.volume_control;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VolumeControlTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VolumeControl.class);
		RuneLite.main(args);
	}
}