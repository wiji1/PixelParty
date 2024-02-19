package dev.wiji.pixelparty.util;

import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class SchematicTools {

	public static byte[] getSchematicMaterialData(File schematic) {
		try {
			InputStream fis = Files.newInputStream(schematic.toPath());
			NBTTagCompound nbtData = NBTCompressedStreamTools.a(fis);

			byte[] data = nbtData.getByteArray("Data");

			fis.close();

			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

