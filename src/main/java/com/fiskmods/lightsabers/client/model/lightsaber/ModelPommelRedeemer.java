package com.fiskmods.lightsabers.client.model.lightsaber;

import com.fiskmods.lightsabers.client.model.legacy.LegacyGlState;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelRenderer;
import net.minecraft.world.entity.Entity;

public class ModelPommelRedeemer extends LegacyModelBase
{
    public LegacyModelRenderer body1;
    public LegacyModelRenderer top1;
    public LegacyModelRenderer body2;
    public LegacyModelRenderer body3;
    public LegacyModelRenderer body4;
    public LegacyModelRenderer body5;
    public LegacyModelRenderer body6;
    public LegacyModelRenderer body7;
    public LegacyModelRenderer body8;
    public LegacyModelRenderer top2;
    public LegacyModelRenderer top3;
    public LegacyModelRenderer top4;
    public LegacyModelRenderer top5;
    public LegacyModelRenderer top6;
    public LegacyModelRenderer top7;
    public LegacyModelRenderer top8;

    public ModelPommelRedeemer()
    {
        textureWidth = 64;
        textureHeight = 32;
        top1 = new LegacyModelRenderer(this, 10, 0);
        top1.setRotationPoint(0.0F, 0.0F, 0.0F);
        top1.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        body2 = new LegacyModelRenderer(this, 0, 0);
        body2.setRotationPoint(0.0F, 0.0F, 0.0F);
        body2.addBox(-1.5F, 0.0F, -0.38F, 3, 1, 4, 0.0F);
        setRotateAngle(body2, 0.0F, 0.7853981633974483F, 0.0F);
        body8 = new LegacyModelRenderer(this, 0, 0);
        body8.setRotationPoint(0.0F, 0.0F, 0.0F);
        body8.addBox(-1.5F, 0.0F, -0.38F, 3, 1, 4, 0.0F);
        setRotateAngle(body8, 0.0F, -0.7853981633974483F, 0.0F);
        top5 = new LegacyModelRenderer(this, 10, 0);
        top5.setRotationPoint(0.0F, 0.0F, 0.0F);
        top5.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top5, 0.0F, 3.141592653589793F, 0.0F);
        body1 = new LegacyModelRenderer(this, 0, 0);
        body1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body1.addBox(-1.5F, 0.0F, -0.38F, 3, 1, 4, 0.0F);
        body5 = new LegacyModelRenderer(this, 0, 0);
        body5.setRotationPoint(0.0F, 0.0F, 0.0F);
        body5.addBox(-1.5F, 0.0F, -0.38F, 3, 1, 4, 0.0F);
        setRotateAngle(body5, 0.0F, 3.141592653589793F, 0.0F);
        top2 = new LegacyModelRenderer(this, 10, 0);
        top2.setRotationPoint(0.0F, 0.0F, 0.0F);
        top2.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top2, 0.0F, 0.7853981633974483F, 0.0F);
        body3 = new LegacyModelRenderer(this, 0, 0);
        body3.setRotationPoint(0.0F, 0.0F, 0.0F);
        body3.addBox(-1.5F, 0.0F, -0.38F, 3, 1, 4, 0.0F);
        setRotateAngle(body3, 0.0F, 1.5707963267948966F, 0.0F);
        body6 = new LegacyModelRenderer(this, 0, 0);
        body6.setRotationPoint(0.0F, 0.0F, 0.0F);
        body6.addBox(-1.5F, 0.0F, -0.38F, 3, 1, 4, 0.0F);
        setRotateAngle(body6, 0.0F, -2.356194490192345F, 0.0F);
        top7 = new LegacyModelRenderer(this, 10, 0);
        top7.setRotationPoint(0.0F, 0.0F, 0.0F);
        top7.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top7, 0.0F, -1.5707963267948966F, 0.0F);
        body4 = new LegacyModelRenderer(this, 0, 0);
        body4.setRotationPoint(0.0F, 0.0F, 0.0F);
        body4.addBox(-1.5F, 0.0F, -0.38F, 3, 1, 4, 0.0F);
        setRotateAngle(body4, 0.0F, 2.356194490192345F, 0.0F);
        top3 = new LegacyModelRenderer(this, 10, 0);
        top3.setRotationPoint(0.0F, 0.0F, 0.0F);
        top3.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top3, 0.0F, 1.5707963267948966F, 0.0F);
        top4 = new LegacyModelRenderer(this, 10, 0);
        top4.setRotationPoint(0.0F, 0.0F, 0.0F);
        top4.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top4, 0.0F, 2.356194490192345F, 0.0F);
        top8 = new LegacyModelRenderer(this, 10, 0);
        top8.setRotationPoint(0.0F, 0.0F, 0.0F);
        top8.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top8, 0.0F, -0.7853981633974483F, 0.0F);
        top6 = new LegacyModelRenderer(this, 10, 0);
        top6.setRotationPoint(0.0F, 0.0F, 0.0F);
        top6.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top6, 0.0F, -2.356194490192345F, 0.0F);
        body7 = new LegacyModelRenderer(this, 0, 0);
        body7.setRotationPoint(0.0F, 0.0F, 0.0F);
        body7.addBox(-1.5F, 0.0F, -0.38F, 3, 1, 4, 0.0F);
        setRotateAngle(body7, 0.0F, -1.5707963267948966F, 0.0F);
        body1.addChild(body2);
        body1.addChild(body8);
        top1.addChild(top5);
        body1.addChild(body5);
        top1.addChild(top2);
        body1.addChild(body3);
        body1.addChild(body6);
        top1.addChild(top7);
        body1.addChild(body4);
        top1.addChild(top3);
        top1.addChild(top4);
        top1.addChild(top8);
        top1.addChild(top6);
        body1.addChild(body7);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(top1.offsetX, top1.offsetY, top1.offsetZ);
        LegacyGlState.glTranslatef(top1.rotationPointX * f5, top1.rotationPointY * f5, top1.rotationPointZ * f5);
        LegacyGlState.glScaled(0.8D, 1.0D, 0.8D);
        LegacyGlState.glTranslatef(-top1.offsetX, -top1.offsetY, -top1.offsetZ);
        LegacyGlState.glTranslatef(-top1.rotationPointX * f5, -top1.rotationPointY * f5, -top1.rotationPointZ * f5);
        top1.render(f5);
        LegacyGlState.glPopMatrix();
        body1.render(f5);
    }

    public void setRotateAngle(LegacyModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
