package com.fiskmods.lightsabers.client.model.lightsaber;

import com.fiskmods.lightsabers.client.model.legacy.LegacyGlState;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelRenderer;
import net.minecraft.world.entity.Entity;

public class ModelPommelMauler extends LegacyModelBase
{
    public LegacyModelRenderer top1;
    public LegacyModelRenderer outerRing1;
    public LegacyModelRenderer innerRing1;
    public LegacyModelRenderer top2;
    public LegacyModelRenderer top3;
    public LegacyModelRenderer top4;
    public LegacyModelRenderer top5;
    public LegacyModelRenderer top6;
    public LegacyModelRenderer top7;
    public LegacyModelRenderer top8;
    public LegacyModelRenderer outerRing2;
    public LegacyModelRenderer outerRing3;
    public LegacyModelRenderer outerRing4;
    public LegacyModelRenderer outerRing5;
    public LegacyModelRenderer outerRing6;
    public LegacyModelRenderer outerRing7;
    public LegacyModelRenderer outerRing8;
    public LegacyModelRenderer innerRing2;
    public LegacyModelRenderer innerRing3;
    public LegacyModelRenderer innerRing4;
    public LegacyModelRenderer innerRing5;
    public LegacyModelRenderer innerRing6;
    public LegacyModelRenderer innerRing7;
    public LegacyModelRenderer innerRing8;

    public ModelPommelMauler()
    {
        textureWidth = 64;
        textureHeight = 32;
        outerRing1 = new LegacyModelRenderer(this, 0, 0);
        outerRing1.setRotationPoint(0.0F, 0.0F, 0.0F);
        outerRing1.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        top7 = new LegacyModelRenderer(this, 8, 0);
        top7.setRotationPoint(0.0F, 0.0F, 0.0F);
        top7.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top7, 0.0F, -1.5707963267948966F, 0.0F);
        outerRing3 = new LegacyModelRenderer(this, 0, 0);
        outerRing3.setRotationPoint(0.0F, 0.0F, 0.0F);
        outerRing3.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(outerRing3, 0.0F, 1.5707963267948966F, 0.0F);
        outerRing6 = new LegacyModelRenderer(this, 0, 0);
        outerRing6.setRotationPoint(0.0F, 0.0F, 0.0F);
        outerRing6.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(outerRing6, 0.0F, -2.356194490192345F, 0.0F);
        innerRing8 = new LegacyModelRenderer(this, 0, 2);
        innerRing8.setRotationPoint(0.0F, 0.0F, 0.0F);
        innerRing8.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(innerRing8, 0.0F, -0.7853981633974483F, 0.0F);
        innerRing3 = new LegacyModelRenderer(this, 0, 2);
        innerRing3.setRotationPoint(0.0F, 0.0F, 0.0F);
        innerRing3.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(innerRing3, 0.0F, 1.5707963267948966F, 0.0F);
        outerRing8 = new LegacyModelRenderer(this, 0, 0);
        outerRing8.setRotationPoint(0.0F, 0.0F, 0.0F);
        outerRing8.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(outerRing8, 0.0F, -0.7853981633974483F, 0.0F);
        top1 = new LegacyModelRenderer(this, 8, 0);
        top1.setRotationPoint(0.0F, 0.0F, 0.0F);
        top1.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        top3 = new LegacyModelRenderer(this, 8, 0);
        top3.setRotationPoint(0.0F, 0.0F, 0.0F);
        top3.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top3, 0.0F, 1.5707963267948966F, 0.0F);
        outerRing2 = new LegacyModelRenderer(this, 0, 0);
        outerRing2.setRotationPoint(0.0F, 0.0F, 0.0F);
        outerRing2.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(outerRing2, 0.0F, 0.7853981633974483F, 0.0F);
        top8 = new LegacyModelRenderer(this, 8, 0);
        top8.setRotationPoint(0.0F, 0.0F, 0.0F);
        top8.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top8, 0.0F, -0.7853981633974483F, 0.0F);
        outerRing7 = new LegacyModelRenderer(this, 0, 0);
        outerRing7.setRotationPoint(0.0F, 0.0F, 0.0F);
        outerRing7.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(outerRing7, 0.0F, -1.5707963267948966F, 0.0F);
        innerRing5 = new LegacyModelRenderer(this, 0, 2);
        innerRing5.setRotationPoint(0.0F, 0.0F, 0.0F);
        innerRing5.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(innerRing5, 0.0F, 3.141592653589793F, 0.0F);
        innerRing2 = new LegacyModelRenderer(this, 0, 2);
        innerRing2.setRotationPoint(0.0F, 0.0F, 0.0F);
        innerRing2.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(innerRing2, 0.0F, 0.7853981633974483F, 0.0F);
        top4 = new LegacyModelRenderer(this, 8, 0);
        top4.setRotationPoint(0.0F, 0.0F, 0.0F);
        top4.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top4, 0.0F, 2.356194490192345F, 0.0F);
        top2 = new LegacyModelRenderer(this, 8, 0);
        top2.setRotationPoint(0.0F, 0.0F, 0.0F);
        top2.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top2, 0.0F, 0.7853981633974483F, 0.0F);
        top6 = new LegacyModelRenderer(this, 8, 0);
        top6.setRotationPoint(0.0F, 0.0F, 0.0F);
        top6.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top6, 0.0F, -2.356194490192345F, 0.0F);
        innerRing6 = new LegacyModelRenderer(this, 0, 2);
        innerRing6.setRotationPoint(0.0F, 0.0F, 0.0F);
        innerRing6.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(innerRing6, 0.0F, -2.356194490192345F, 0.0F);
        innerRing4 = new LegacyModelRenderer(this, 0, 2);
        innerRing4.setRotationPoint(0.0F, 0.0F, 0.0F);
        innerRing4.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(innerRing4, 0.0F, 2.356194490192345F, 0.0F);
        outerRing4 = new LegacyModelRenderer(this, 0, 0);
        outerRing4.setRotationPoint(0.0F, 0.0F, 0.0F);
        outerRing4.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(outerRing4, 0.0F, 2.356194490192345F, 0.0F);
        innerRing1 = new LegacyModelRenderer(this, 0, 2);
        innerRing1.setRotationPoint(0.0F, -1.0F, 0.0F);
        innerRing1.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        innerRing7 = new LegacyModelRenderer(this, 0, 2);
        innerRing7.setRotationPoint(0.0F, 0.0F, 0.0F);
        innerRing7.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(innerRing7, 0.0F, -1.5707963267948966F, 0.0F);
        outerRing5 = new LegacyModelRenderer(this, 0, 0);
        outerRing5.setRotationPoint(0.0F, 0.0F, 0.0F);
        outerRing5.addBox(-1.5F, 0.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(outerRing5, 0.0F, 3.141592653589793F, 0.0F);
        top5 = new LegacyModelRenderer(this, 8, 0);
        top5.setRotationPoint(0.0F, 0.0F, 0.0F);
        top5.addBox(-1.5F, -1.0F, 2.62F, 3, 1, 1, 0.0F);
        setRotateAngle(top5, 0.0F, 3.141592653589793F, 0.0F);
        top1.addChild(top7);
        outerRing1.addChild(outerRing3);
        outerRing1.addChild(outerRing6);
        innerRing1.addChild(innerRing8);
        innerRing1.addChild(innerRing3);
        outerRing1.addChild(outerRing8);
        top1.addChild(top3);
        outerRing1.addChild(outerRing2);
        top1.addChild(top8);
        outerRing1.addChild(outerRing7);
        innerRing1.addChild(innerRing5);
        innerRing1.addChild(innerRing2);
        top1.addChild(top4);
        top1.addChild(top2);
        top1.addChild(top6);
        innerRing1.addChild(innerRing6);
        innerRing1.addChild(innerRing4);
        outerRing1.addChild(outerRing4);
        innerRing1.addChild(innerRing7);
        outerRing1.addChild(outerRing5);
        top1.addChild(top5);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(outerRing1.offsetX, outerRing1.offsetY, outerRing1.offsetZ);
        LegacyGlState.glTranslatef(outerRing1.rotationPointX * f5, outerRing1.rotationPointY * f5, outerRing1.rotationPointZ * f5);
        LegacyGlState.glScaled(1.0D, 0.25D, 1.0D);
        LegacyGlState.glTranslatef(-outerRing1.offsetX, -outerRing1.offsetY, -outerRing1.offsetZ);
        LegacyGlState.glTranslatef(-outerRing1.rotationPointX * f5, -outerRing1.rotationPointY * f5, -outerRing1.rotationPointZ * f5);
        outerRing1.render(f5);
        LegacyGlState.glPopMatrix();
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(top1.offsetX, top1.offsetY, top1.offsetZ);
        LegacyGlState.glTranslatef(top1.rotationPointX * f5, top1.rotationPointY * f5, top1.rotationPointZ * f5);
        LegacyGlState.glScaled(0.8D, 1.0D, 0.8D);
        LegacyGlState.glTranslatef(-top1.offsetX, -top1.offsetY, -top1.offsetZ);
        LegacyGlState.glTranslatef(-top1.rotationPointX * f5, -top1.rotationPointY * f5, -top1.rotationPointZ * f5);
        top1.render(f5);
        LegacyGlState.glPopMatrix();
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(innerRing1.offsetX, innerRing1.offsetY, innerRing1.offsetZ);
        LegacyGlState.glTranslatef(innerRing1.rotationPointX * f5, innerRing1.rotationPointY * f5, innerRing1.rotationPointZ * f5);
        LegacyGlState.glScaled(0.65D, 1.3D, 0.65D);
        LegacyGlState.glTranslatef(-innerRing1.offsetX, -innerRing1.offsetY, -innerRing1.offsetZ);
        LegacyGlState.glTranslatef(-innerRing1.rotationPointX * f5, -innerRing1.rotationPointY * f5, -innerRing1.rotationPointZ * f5);
        innerRing1.render(f5);
        LegacyGlState.glPopMatrix();
    }

    public void setRotateAngle(LegacyModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
