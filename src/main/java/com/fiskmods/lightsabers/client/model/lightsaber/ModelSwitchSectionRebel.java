package com.fiskmods.lightsabers.client.model.lightsaber;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelRenderer;
import net.minecraft.world.entity.Entity;
import com.fiskmods.lightsabers.client.model.legacy.LegacyGlState;

public class ModelSwitchSectionRebel extends LegacyModelBase
{
    public LegacyModelRenderer body1;
    public LegacyModelRenderer lowerSideButton1;
    public LegacyModelRenderer innerBody1;
    public LegacyModelRenderer body2;
    public LegacyModelRenderer body3;
    public LegacyModelRenderer body4;
    public LegacyModelRenderer body5;
    public LegacyModelRenderer body6;
    public LegacyModelRenderer body7;
    public LegacyModelRenderer body9;
    public LegacyModelRenderer lowerSideButton2;
    public LegacyModelRenderer lowerSideButton3;
    public LegacyModelRenderer lowerSideButton4;
    public LegacyModelRenderer lowerSideButton5;
    public LegacyModelRenderer lowerSideButton6;
    public LegacyModelRenderer lowerSideButton7;
    public LegacyModelRenderer lowerSideButton8;
    public LegacyModelRenderer body2_1;
    public LegacyModelRenderer body3_1;
    public LegacyModelRenderer body4_1;
    public LegacyModelRenderer body5_1;
    public LegacyModelRenderer body6_1;
    public LegacyModelRenderer body7_1;
    public LegacyModelRenderer body9_1;

    public ModelSwitchSectionRebel()
    {
        textureWidth = 64;
        textureHeight = 32;
        innerBody1 = new LegacyModelRenderer(this, 0, 0);
        innerBody1.setRotationPoint(0.0F, -2.2F, 0.0F);
        innerBody1.addBox(-1.5F, -8.0F, 2.62F, 3, 5, 1, 0.0F);
        setRotateAngle(innerBody1, 0.0F, -1.5707963267948966F, 0.0F);
        lowerSideButton5 = new LegacyModelRenderer(this, 10, 9);
        lowerSideButton5.setRotationPoint(0.0F, 0.0F, 0.0F);
        lowerSideButton5.addBox(-1.5F, -4.0F, -0.38F, 3, 4, 4, 0.0F);
        setRotateAngle(lowerSideButton5, 0.0F, 3.141592653589793F, 0.0F);
        body4 = new LegacyModelRenderer(this, 0, 9);
        body4.setRotationPoint(0.0F, 0.0F, 0.0F);
        body4.addBox(-1.5F, -8.0F, 2.62F, 3, 4, 1, 0.0F);
        setRotateAngle(body4, 0.0F, 2.356194490192345F, 0.0F);
        body1 = new LegacyModelRenderer(this, 0, 9);
        body1.setRotationPoint(0.0F, 4.0F, 0.0F);
        body1.addBox(-1.5F, -8.0F, 2.62F, 3, 4, 1, 0.0F);
        setRotateAngle(body1, 0.0F, -1.5707963267948966F, 0.0F);
        body7 = new LegacyModelRenderer(this, 0, 15);
        body7.setRotationPoint(0.0F, 0.0F, 0.0F);
        body7.addBox(-1.5F, -8.0F, 2.62F, 3, 4, 1, 0.0F);
        setRotateAngle(body7, 0.0F, -1.5707963267948966F, 0.0F);
        body5 = new LegacyModelRenderer(this, 0, 9);
        body5.setRotationPoint(0.0F, 0.0F, 0.0F);
        body5.addBox(-1.5F, -8.0F, 2.62F, 3, 4, 1, 0.0F);
        setRotateAngle(body5, 0.0F, 3.141592653589793F, 0.0F);
        body2_1 = new LegacyModelRenderer(this, 0, 0);
        body2_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body2_1.addBox(-1.5F, -8.0F, 2.62F, 3, 5, 1, 0.0F);
        setRotateAngle(body2_1, 0.0F, 0.7853981633974483F, 0.0F);
        lowerSideButton1 = new LegacyModelRenderer(this, 10, 9);
        lowerSideButton1.setRotationPoint(-3.62F, -2.0999999999999996F, 0.0F);
        lowerSideButton1.addBox(-1.5F, -4.0F, -0.38F, 3, 4, 4, 0.0F);
        setRotateAngle(lowerSideButton1, 1.5707963267948966F, 1.5707963267948966F, 0.0F);
        body9 = new LegacyModelRenderer(this, 0, 9);
        body9.setRotationPoint(0.0F, 0.0F, 0.0F);
        body9.addBox(-1.5F, -8.0F, 2.62F, 3, 4, 1, 0.0F);
        setRotateAngle(body9, 0.0F, -0.7853981633974483F, 0.0F);
        body2 = new LegacyModelRenderer(this, 0, 9);
        body2.setRotationPoint(0.0F, 0.0F, 0.0F);
        body2.addBox(-1.5F, -8.0F, 2.62F, 3, 4, 1, 0.0F);
        setRotateAngle(body2, 0.0F, 0.7853981633974483F, 0.0F);
        lowerSideButton7 = new LegacyModelRenderer(this, 10, 9);
        lowerSideButton7.setRotationPoint(0.0F, 0.0F, 0.0F);
        lowerSideButton7.addBox(-1.5F, -4.0F, -0.38F, 3, 4, 4, 0.0F);
        setRotateAngle(lowerSideButton7, 0.0F, -1.5707963267948966F, 0.0F);
        body5_1 = new LegacyModelRenderer(this, 0, 0);
        body5_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body5_1.addBox(-1.5F, -8.0F, 2.62F, 3, 5, 1, 0.0F);
        setRotateAngle(body5_1, 0.0F, 3.141592653589793F, 0.0F);
        lowerSideButton3 = new LegacyModelRenderer(this, 10, 9);
        lowerSideButton3.setRotationPoint(0.0F, 0.0F, 0.0F);
        lowerSideButton3.addBox(-1.5F, -4.0F, -0.38F, 3, 4, 4, 0.0F);
        setRotateAngle(lowerSideButton3, 0.0F, 1.5707963267948966F, 0.0F);
        body6_1 = new LegacyModelRenderer(this, 0, 0);
        body6_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body6_1.addBox(-1.5F, -8.0F, 2.62F, 3, 5, 1, 0.0F);
        setRotateAngle(body6_1, 0.0F, -2.356194490192345F, 0.0F);
        body3 = new LegacyModelRenderer(this, 0, 9);
        body3.setRotationPoint(0.0F, 0.0F, 0.0F);
        body3.addBox(-1.5F, -8.0F, 2.62F, 3, 4, 1, 0.0F);
        setRotateAngle(body3, 0.0F, 1.5707963267948966F, 0.0F);
        lowerSideButton6 = new LegacyModelRenderer(this, 10, 9);
        lowerSideButton6.setRotationPoint(0.0F, 0.0F, 0.0F);
        lowerSideButton6.addBox(-1.5F, -4.0F, -0.38F, 3, 4, 4, 0.0F);
        setRotateAngle(lowerSideButton6, 0.0F, -2.356194490192345F, 0.0F);
        body7_1 = new LegacyModelRenderer(this, 0, 0);
        body7_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body7_1.addBox(-1.5F, -8.0F, 2.62F, 3, 5, 1, 0.0F);
        setRotateAngle(body7_1, 0.0F, -1.5707963267948966F, 0.0F);
        body3_1 = new LegacyModelRenderer(this, 0, 0);
        body3_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body3_1.addBox(-1.5F, -8.0F, 2.62F, 3, 5, 1, 0.0F);
        setRotateAngle(body3_1, 0.0F, 1.5707963267948966F, 0.0F);
        lowerSideButton2 = new LegacyModelRenderer(this, 10, 9);
        lowerSideButton2.setRotationPoint(0.0F, 0.0F, 0.0F);
        lowerSideButton2.addBox(-1.5F, -4.0F, -0.38F, 3, 4, 4, 0.0F);
        setRotateAngle(lowerSideButton2, 0.0F, 0.7853981633974483F, 0.0F);
        lowerSideButton8 = new LegacyModelRenderer(this, 10, 9);
        lowerSideButton8.setRotationPoint(0.0F, 0.0F, 0.0F);
        lowerSideButton8.addBox(-1.5F, -4.0F, -0.38F, 3, 4, 4, 0.0F);
        setRotateAngle(lowerSideButton8, 0.0F, -0.7853981633974483F, 0.0F);
        body6 = new LegacyModelRenderer(this, 0, 9);
        body6.setRotationPoint(0.0F, 0.0F, 0.0F);
        body6.addBox(-1.5F, -8.0F, 2.62F, 3, 4, 1, 0.0F);
        setRotateAngle(body6, 0.0F, -2.356194490192345F, 0.0F);
        lowerSideButton4 = new LegacyModelRenderer(this, 10, 9);
        lowerSideButton4.setRotationPoint(0.0F, 0.0F, 0.0F);
        lowerSideButton4.addBox(-1.5F, -4.0F, -0.38F, 3, 4, 4, 0.0F);
        setRotateAngle(lowerSideButton4, 0.0F, 2.356194490192345F, 0.0F);
        body9_1 = new LegacyModelRenderer(this, 0, 0);
        body9_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body9_1.addBox(-1.5F, -8.0F, 2.62F, 3, 5, 1, 0.0F);
        setRotateAngle(body9_1, 0.0F, -0.7853981633974483F, 0.0F);
        body4_1 = new LegacyModelRenderer(this, 0, 0);
        body4_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body4_1.addBox(-1.5F, -8.0F, 2.62F, 3, 5, 1, 0.0F);
        setRotateAngle(body4_1, 0.0F, 2.356194490192345F, 0.0F);
        lowerSideButton1.addChild(lowerSideButton5);
        body1.addChild(body4);
        body1.addChild(body7);
        body1.addChild(body5);
        innerBody1.addChild(body2_1);
        body1.addChild(body9);
        body1.addChild(body2);
        lowerSideButton1.addChild(lowerSideButton7);
        innerBody1.addChild(body5_1);
        lowerSideButton1.addChild(lowerSideButton3);
        innerBody1.addChild(body6_1);
        body1.addChild(body3);
        lowerSideButton1.addChild(lowerSideButton6);
        innerBody1.addChild(body7_1);
        innerBody1.addChild(body3_1);
        lowerSideButton1.addChild(lowerSideButton2);
        lowerSideButton1.addChild(lowerSideButton8);
        body1.addChild(body6);
        lowerSideButton1.addChild(lowerSideButton4);
        innerBody1.addChild(body9_1);
        innerBody1.addChild(body4_1);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(innerBody1.offsetX, innerBody1.offsetY, innerBody1.offsetZ);
        LegacyGlState.glTranslatef(innerBody1.rotationPointX * f5, innerBody1.rotationPointY * f5, innerBody1.rotationPointZ * f5);
        LegacyGlState.glScaled(0.8D, 0.6D, 0.8D);
        LegacyGlState.glTranslatef(-innerBody1.offsetX, -innerBody1.offsetY, -innerBody1.offsetZ);
        LegacyGlState.glTranslatef(-innerBody1.rotationPointX * f5, -innerBody1.rotationPointY * f5, -innerBody1.rotationPointZ * f5);
        innerBody1.render(f5);
        LegacyGlState.glPopMatrix();
        body1.render(f5);
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(lowerSideButton1.offsetX, lowerSideButton1.offsetY, lowerSideButton1.offsetZ);
        LegacyGlState.glTranslatef(lowerSideButton1.rotationPointX * f5, lowerSideButton1.rotationPointY * f5, lowerSideButton1.rotationPointZ * f5);
        LegacyGlState.glScaled(0.2D, 0.2D, 0.2D);
        LegacyGlState.glTranslatef(-lowerSideButton1.offsetX, -lowerSideButton1.offsetY, -lowerSideButton1.offsetZ);
        LegacyGlState.glTranslatef(-lowerSideButton1.rotationPointX * f5, -lowerSideButton1.rotationPointY * f5, -lowerSideButton1.rotationPointZ * f5);
        lowerSideButton1.render(f5);
        LegacyGlState.glPopMatrix();
    }

    public void setRotateAngle(LegacyModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
