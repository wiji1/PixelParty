package dev.wiji.pixelparty.objects;

import dev.wiji.pixelparty.PixelParty;
import dev.wiji.pixelparty.util.SchematicTools;
import org.bukkit.DyeColor;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Floor {
	public String name;
	public List<DyeColor> staticColors;

	public byte[] blueprint;
	public String schematicName;
	public FloorType floorType;


	public Floor(String name, String schematicName, FloorType floorType, DyeColor... staticColors) {
		this.name = name;
		this.schematicName = schematicName;
		this.floorType = floorType;
		this.staticColors = Arrays.asList(staticColors);

		generateBlueprint();
	}

	public void generateBlueprint() {
		File schematic = new File(PixelParty.INSTANCE.getDataFolder().getPath() +
				"/schematics/" + schematicName + ".schematic");
		this.blueprint = SchematicTools.getSchematicMaterialData(schematic);
	}

	public List<DyeColor> getMixedStaticColors() {
		return staticColors;
	}

	public enum FloorType {
		STATIC,
		DYNAMIC,
		MIXED;
	}
}
