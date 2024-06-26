package dev.wiji.pixelparty.objects;

import dev.wiji.pixelparty.PixelParty;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PixelVillager extends EntityVillager {
	BukkitTask task;

	public PixelVillager(World world) {
		super(world);

		getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.15);

		task = new BukkitRunnable() {
			@Override
			public void run() {
				Vec3D vec3d = RandomPositionGenerator.a(PixelVillager.this, 5, 4);
				double a = vec3d.a;
				double b = vec3d.b;
				double c = vec3d.c;

				getNavigation().a(a, b, c, 2.0);
			}
		}.runTaskTimer(PixelParty.INSTANCE, 0, 10);
	}

	public void remove() {
		this.die();
		task.cancel();
	}
}
