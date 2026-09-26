package mrkartoshki.fling.mixin;

import mrkartoshki.fling.ItemEntityRotationExt;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements ItemEntityRotationExt {
	@Unique
	private float fling$rotation;

	@Override
	public float fling$getRotation() {
		return fling$rotation;
	}

	@Override
	public void fling$setRotation(float rotation) {
		fling$rotation = rotation;
	}
}
