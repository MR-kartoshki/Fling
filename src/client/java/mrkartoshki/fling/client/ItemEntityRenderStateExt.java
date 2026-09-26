package mrkartoshki.fling.client;

public interface ItemEntityRenderStateExt {
	boolean fling$isBlock();
	float fling$getXRot();
	float fling$getYRot();
	void fling$extractPhysics(net.minecraft.world.entity.item.ItemEntity entity);
}
