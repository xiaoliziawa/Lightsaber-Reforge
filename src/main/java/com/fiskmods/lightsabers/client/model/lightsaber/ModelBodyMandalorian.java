package com.fiskmods.lightsabers.client.model.lightsaber;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelRenderer;
import net.minecraft.world.entity.Entity;

public class ModelBodyMandalorian extends LegacyModelBase
{
    public LegacyModelRenderer body1;
    public LegacyModelRenderer body2;
    public LegacyModelRenderer body4;
    public LegacyModelRenderer body6;
    public LegacyModelRenderer body7;
    public LegacyModelRenderer body8;
    public LegacyModelRenderer body3;
    public LegacyModelRenderer body5;
    public LegacyModelRenderer body9;
    public LegacyModelRenderer body11;
    public LegacyModelRenderer body10;
    public LegacyModelRenderer body12;

    public ModelBodyMandalorian()
    {
        textureWidth = 64;
        textureHeight = 32;
        body7 = new LegacyModelRenderer(this, 0, 0);
        body7.mirror = true;
        body7.setRotationPoint(0.0F, 0.0F, 0.0F);
        body7.addBox(1.98F, 0.0F, -2.5F, 1, 26, 5, 0.0F);
        body10 = new LegacyModelRenderer(this, 20, 0);
        body10.setRotationPoint(-1.0F, 0.0F, 0.0F);
        body10.addBox(-1.0F, 0.0F, -1.0F, 1, 26, 1, 0.0F);
        setRotateAngle(body10, 0.0F, -0.4721115626644662F, 0.0F);
        body3 = new LegacyModelRenderer(this, 20, 0);
        body3.setRotationPoint(-1.0F, 0.0F, 0.0F);
        body3.addBox(-1.0F, 0.0F, -1.0F, 1, 26, 1, 0.0F);
        setRotateAngle(body3, 0.0F, -0.4721115626644662F, 0.0F);
        body5 = new LegacyModelRenderer(this, 20, 0);
        body5.mirror = true;
        body5.setRotationPoint(1.0F, 0.0F, 0.0F);
        body5.addBox(0.0F, 0.0F, -1.0F, 1, 26, 1, 0.0F);
        setRotateAngle(body5, 0.0F, 0.4721115626644662F, 0.0F);
        body11 = new LegacyModelRenderer(this, 20, 0);
        body11.mirror = true;
        body11.setRotationPoint(1.5F, 0.0F, 3.76F);
        body11.addBox(0.0F, 0.0F, -1.0F, 1, 26, 1, 0.0F);
        setRotateAngle(body11, 0.0F, 0.46949356878647464F, 0.0F);
        body4 = new LegacyModelRenderer(this, 20, 0);
        body4.mirror = true;
        body4.setRotationPoint(1.5F, 0.0F, 3.76F);
        body4.addBox(0.0F, 0.0F, -1.0F, 1, 26, 1, 0.0F);
        setRotateAngle(body4, 0.0F, 0.46949356878647464F, 0.0F);
        body2 = new LegacyModelRenderer(this, 20, 0);
        body2.setRotationPoint(-1.5F, 0.0F, 3.76F);
        body2.addBox(-1.0F, 0.0F, -1.0F, 1, 26, 1, 0.0F);
        setRotateAngle(body2, 0.0F, -0.46949356878647464F, 0.0F);
        body8 = new LegacyModelRenderer(this, 12, 0);
        body8.setRotationPoint(0.0F, 0.0F, 0.0F);
        body8.addBox(-1.5F, 0.0F, 2.76F, 3, 26, 1, 0.0F);
        setRotateAngle(body8, 0.0F, 3.141592653589793F, 0.0F);
        body1 = new LegacyModelRenderer(this, 12, 0);
        body1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body1.addBox(-1.5F, 0.0F, 2.76F, 3, 26, 1, 0.0F);
        body6 = new LegacyModelRenderer(this, 0, 0);
        body6.setRotationPoint(0.0F, 0.0F, 0.0F);
        body6.addBox(-2.98F, 0.0F, -2.5F, 1, 26, 5, 0.0F);
        body9 = new LegacyModelRenderer(this, 20, 0);
        body9.setRotationPoint(-1.5F, 0.0F, 3.76F);
        body9.addBox(-1.0F, 0.0F, -1.0F, 1, 26, 1, 0.0F);
        setRotateAngle(body9, 0.0F, -0.46949356878647464F, 0.0F);
        body12 = new LegacyModelRenderer(this, 20, 0);
        body12.mirror = true;
        body12.setRotationPoint(1.0F, 0.0F, 0.0F);
        body12.addBox(0.0F, 0.0F, -1.0F, 1, 26, 1, 0.0F);
        setRotateAngle(body12, 0.0F, 0.4721115626644662F, 0.0F);
        body1.addChild(body7);
        body9.addChild(body10);
        body2.addChild(body3);
        body4.addChild(body5);
        body8.addChild(body11);
        body1.addChild(body4);
        body1.addChild(body2);
        body1.addChild(body8);
        body1.addChild(body6);
        body8.addChild(body9);
        body11.addChild(body12);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        body1.render(f5);
    }

    public void setRotateAngle(LegacyModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
