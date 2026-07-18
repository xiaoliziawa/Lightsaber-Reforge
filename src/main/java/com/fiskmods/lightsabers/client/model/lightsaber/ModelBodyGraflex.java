package com.fiskmods.lightsabers.client.model.lightsaber;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelRenderer;
import net.minecraft.world.entity.Entity;

public class ModelBodyGraflex extends LegacyModelBase
{
    public LegacyModelRenderer body1;
    public LegacyModelRenderer body2;
    public LegacyModelRenderer body5;
    public LegacyModelRenderer body8;
    public LegacyModelRenderer body11;
    public LegacyModelRenderer body14;
    public LegacyModelRenderer body17;
    public LegacyModelRenderer body20;
    public LegacyModelRenderer body23;
    public LegacyModelRenderer body3;
    public LegacyModelRenderer body4;
    public LegacyModelRenderer body6;
    public LegacyModelRenderer body7;
    public LegacyModelRenderer body9;
    public LegacyModelRenderer body10;
    public LegacyModelRenderer body12;
    public LegacyModelRenderer body13;
    public LegacyModelRenderer body15;
    public LegacyModelRenderer body16;
    public LegacyModelRenderer body18;
    public LegacyModelRenderer body19;
    public LegacyModelRenderer body21;
    public LegacyModelRenderer body22;
    public LegacyModelRenderer body24;

    public ModelBodyGraflex()
    {
        textureWidth = 64;
        textureHeight = 32;
        body14 = new LegacyModelRenderer(this, 0, 0);
        body14.setRotationPoint(0.0F, 0.0F, 0.0F);
        body14.addBox(-1.5F, 0.0F, 2.62F, 3, 16, 1, 0.0F);
        setRotateAngle(body14, 0.0F, -2.356194490192345F, 0.0F);
        body8 = new LegacyModelRenderer(this, 0, 0);
        body8.setRotationPoint(0.0F, 0.0F, 0.0F);
        body8.addBox(-1.5F, 0.0F, 2.62F, 3, 16, 1, 0.0F);
        setRotateAngle(body8, 0.0F, 2.356194490192345F, 0.0F);
        body18 = new LegacyModelRenderer(this, 8, 0);
        body18.setRotationPoint(0.0F, 8.3F, 2.6F);
        body18.addBox(-1.0F, -7.5F, 0.5F, 2, 15, 1, 0.0F);
        body3 = new LegacyModelRenderer(this, 8, 0);
        body3.setRotationPoint(0.0F, 8.3F, 2.6F);
        body3.addBox(-1.0F, -7.5F, 0.5F, 2, 15, 1, 0.0F);
        body7 = new LegacyModelRenderer(this, 14, 0);
        body7.setRotationPoint(0.0F, 0.0F, 0.6F);
        body7.addBox(-0.5F, -7.5F, 0.5F, 1, 15, 1, 0.0F);
        body16 = new LegacyModelRenderer(this, 14, 0);
        body16.setRotationPoint(0.0F, 0.0F, 0.6F);
        body16.addBox(-0.5F, -7.5F, 0.5F, 1, 15, 1, 0.0F);
        body12 = new LegacyModelRenderer(this, 8, 0);
        body12.setRotationPoint(0.0F, 8.3F, 2.6F);
        body12.addBox(-1.0F, -7.5F, 0.5F, 2, 15, 1, 0.0F);
        body1 = new LegacyModelRenderer(this, 0, 0);
        body1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body1.addBox(-1.5F, 0.0F, 2.62F, 3, 16, 1, 0.0F);
        body22 = new LegacyModelRenderer(this, 14, 0);
        body22.setRotationPoint(0.0F, 0.0F, 0.6F);
        body22.addBox(-0.5F, -7.5F, 0.5F, 1, 15, 1, 0.0F);
        body20 = new LegacyModelRenderer(this, 0, 0);
        body20.setRotationPoint(0.0F, 0.0F, 0.0F);
        body20.addBox(-1.5F, 0.0F, 2.62F, 3, 16, 1, 0.0F);
        setRotateAngle(body20, 0.0F, -0.7853981633974483F, 0.0F);
        body19 = new LegacyModelRenderer(this, 14, 0);
        body19.setRotationPoint(0.0F, 0.0F, 0.6F);
        body19.addBox(-0.5F, -7.5F, 0.5F, 1, 15, 1, 0.0F);
        body9 = new LegacyModelRenderer(this, 8, 0);
        body9.setRotationPoint(0.0F, 8.3F, 2.6F);
        body9.addBox(-1.0F, -7.5F, 0.5F, 2, 15, 1, 0.0F);
        body10 = new LegacyModelRenderer(this, 14, 0);
        body10.setRotationPoint(0.0F, 0.0F, 0.6F);
        body10.addBox(-0.5F, -7.5F, 0.5F, 1, 15, 1, 0.0F);
        body2 = new LegacyModelRenderer(this, 0, 0);
        body2.setRotationPoint(0.0F, 0.0F, 0.0F);
        body2.addBox(-1.5F, 0.0F, 2.62F, 3, 16, 1, 0.0F);
        setRotateAngle(body2, 0.0F, 0.7853981633974483F, 0.0F);
        body23 = new LegacyModelRenderer(this, 8, 0);
        body23.setRotationPoint(0.0F, 8.3F, 2.6F);
        body23.addBox(-1.0F, -7.5F, 0.5F, 2, 15, 1, 0.0F);
        body15 = new LegacyModelRenderer(this, 8, 0);
        body15.setRotationPoint(0.0F, 8.3F, 2.6F);
        body15.addBox(-1.0F, -7.5F, 0.5F, 2, 15, 1, 0.0F);
        body11 = new LegacyModelRenderer(this, 0, 0);
        body11.setRotationPoint(0.0F, 0.0F, 0.0F);
        body11.addBox(-1.5F, 0.0F, 2.62F, 3, 16, 1, 0.0F);
        setRotateAngle(body11, 0.0F, 3.141592653589793F, 0.0F);
        body21 = new LegacyModelRenderer(this, 8, 0);
        body21.setRotationPoint(0.0F, 8.3F, 2.6F);
        body21.addBox(-1.0F, -7.5F, 0.5F, 2, 15, 1, 0.0F);
        body17 = new LegacyModelRenderer(this, 0, 0);
        body17.setRotationPoint(0.0F, 0.0F, 0.0F);
        body17.addBox(-1.5F, 0.0F, 2.62F, 3, 16, 1, 0.0F);
        setRotateAngle(body17, 0.0F, -1.5707963267948966F, 0.0F);
        body13 = new LegacyModelRenderer(this, 14, 0);
        body13.setRotationPoint(0.0F, 0.0F, 0.6F);
        body13.addBox(-0.5F, -7.5F, 0.5F, 1, 15, 1, 0.0F);
        body5 = new LegacyModelRenderer(this, 0, 0);
        body5.setRotationPoint(0.0F, 0.0F, 0.0F);
        body5.addBox(-1.5F, 0.0F, 2.62F, 3, 16, 1, 0.0F);
        setRotateAngle(body5, 0.0F, 1.5707963267948966F, 0.0F);
        body6 = new LegacyModelRenderer(this, 8, 0);
        body6.setRotationPoint(0.0F, 8.3F, 2.6F);
        body6.addBox(-1.0F, -7.5F, 0.5F, 2, 15, 1, 0.0F);
        body24 = new LegacyModelRenderer(this, 14, 0);
        body24.setRotationPoint(0.0F, 0.0F, 0.6F);
        body24.addBox(-0.5F, -7.5F, 0.5F, 1, 15, 1, 0.0F);
        body4 = new LegacyModelRenderer(this, 14, 0);
        body4.setRotationPoint(0.0F, 0.0F, 0.6F);
        body4.addBox(-0.5F, -7.5F, 0.5F, 1, 15, 1, 0.0F);
        body1.addChild(body14);
        body1.addChild(body8);
        body17.addChild(body18);
        body2.addChild(body3);
        body6.addChild(body7);
        body15.addChild(body16);
        body11.addChild(body12);
        body21.addChild(body22);
        body1.addChild(body20);
        body18.addChild(body19);
        body8.addChild(body9);
        body9.addChild(body10);
        body1.addChild(body2);
        body1.addChild(body23);
        body14.addChild(body15);
        body1.addChild(body11);
        body20.addChild(body21);
        body1.addChild(body17);
        body12.addChild(body13);
        body1.addChild(body5);
        body5.addChild(body6);
        body23.addChild(body24);
        body3.addChild(body4);
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
