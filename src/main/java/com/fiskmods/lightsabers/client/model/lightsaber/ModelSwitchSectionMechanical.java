package com.fiskmods.lightsabers.client.model.lightsaber;

import com.fiskmods.lightsabers.client.model.legacy.LegacyGlState;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelRenderer;
import net.minecraft.world.entity.Entity;

public class ModelSwitchSectionMechanical extends LegacyModelBase
{
    public LegacyModelRenderer body1;
    public LegacyModelRenderer switch1;
    public LegacyModelRenderer switch2;
    public LegacyModelRenderer switchPanel;
    public LegacyModelRenderer body2;
    public LegacyModelRenderer body3;
    public LegacyModelRenderer body4;
    public LegacyModelRenderer body5;
    public LegacyModelRenderer body6;
    public LegacyModelRenderer body7;
    public LegacyModelRenderer body9;
    public LegacyModelRenderer body8;

    public ModelSwitchSectionMechanical()
    {
        textureWidth = 64;
        textureHeight = 32;
        switch1 = new LegacyModelRenderer(this, 10, 3);
        switch1.setRotationPoint(-4.7F, -6.0F, -1.1F);
        switch1.addBox(0.1F, -0.52F, -1.87F, 2, 9, 2, 0.0F);
        setRotateAngle(switch1, 0.10471975511965977F, 0.0F, 0.0F);
        switchPanel = new LegacyModelRenderer(this, 8, 20);
        switchPanel.setRotationPoint(-3.0F, -4.5F, -2.2F);
        switchPanel.addBox(-2.0F, -3.9F, 0.0F, 2, 8, 3, 0.0F);
        body6 = new LegacyModelRenderer(this, 0, 0);
        body6.setRotationPoint(0.0F, 0.0F, 0.0F);
        body6.addBox(-1.5F, -8.0F, 2.62F, 3, 8, 1, 0.0F);
        setRotateAngle(body6, 0.0F, -2.356194490192345F, 0.0F);
        body1 = new LegacyModelRenderer(this, 0, 0);
        body1.setRotationPoint(0.0F, 0.0F, 0.0F);
        body1.addBox(-1.5F, -8.0F, 2.62F, 3, 8, 1, 0.0F);
        body2 = new LegacyModelRenderer(this, 0, 0);
        body2.setRotationPoint(0.0F, 0.0F, 0.0F);
        body2.addBox(-1.5F, -8.0F, 2.62F, 3, 8, 1, 0.0F);
        setRotateAngle(body2, 0.0F, 0.7853981633974483F, 0.0F);
        body4 = new LegacyModelRenderer(this, 0, 0);
        body4.setRotationPoint(0.0F, 0.0F, 0.0F);
        body4.addBox(-1.5F, -8.0F, 2.62F, 3, 8, 1, 0.0F);
        setRotateAngle(body4, 0.0F, 2.356194490192345F, 0.0F);
        body5 = new LegacyModelRenderer(this, 0, 0);
        body5.setRotationPoint(0.0F, 0.0F, 0.0F);
        body5.addBox(-1.5F, -8.0F, 2.62F, 3, 8, 1, 0.0F);
        setRotateAngle(body5, 0.0F, 3.141592653589793F, 0.0F);
        body3 = new LegacyModelRenderer(this, 0, 0);
        body3.setRotationPoint(0.0F, 0.0F, 0.0F);
        body3.addBox(-1.5F, -8.0F, 2.62F, 3, 8, 1, 0.0F);
        setRotateAngle(body3, 0.0F, 1.5707963267948966F, 0.0F);
        body7 = new LegacyModelRenderer(this, 0, 0);
        body7.setRotationPoint(0.0F, 0.0F, 0.0F);
        body7.addBox(-1.5F, -8.0F, 2.62F, 3, 8, 1, 0.0F);
        setRotateAngle(body7, 0.0F, -1.5707963267948966F, 0.0F);
        switch2 = new LegacyModelRenderer(this, 10, 3);
        switch2.setRotationPoint(-4.7F, -5.7F, -1.1F);
        switch2.addBox(0.1F, -1.55F, -2.02F, 2, 2, 2, 0.0F);
        setRotateAngle(switch2, -0.6108652381980153F, 0.0F, 0.0F);
        body9 = new LegacyModelRenderer(this, 0, 0);
        body9.setRotationPoint(0.0F, 0.0F, 0.0F);
        body9.addBox(-1.5F, -8.0F, 2.62F, 3, 8, 1, 0.0F);
        setRotateAngle(body9, 0.0F, -0.7853981633974483F, 0.0F);
        body8 = new LegacyModelRenderer(this, 0, 11);
        body8.setRotationPoint(0.0F, -4.0F, 3.0F);
        body8.addBox(-1.5F, -4.0F, 0.0F, 3, 8, 2, 0.0F);
        body1.addChild(body6);
        body1.addChild(body2);
        body1.addChild(body4);
        body1.addChild(body5);
        body1.addChild(body3);
        body1.addChild(body7);
        body1.addChild(body9);
        body7.addChild(body8);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(switch1.offsetX, switch1.offsetY, switch1.offsetZ);
        LegacyGlState.glTranslatef(switch1.rotationPointX * f5, switch1.rotationPointY * f5, switch1.rotationPointZ * f5);
        LegacyGlState.glScaled(0.5D, 0.6D, 1.0D);
        LegacyGlState.glTranslatef(-switch1.offsetX, -switch1.offsetY, -switch1.offsetZ);
        LegacyGlState.glTranslatef(-switch1.rotationPointX * f5, -switch1.rotationPointY * f5, -switch1.rotationPointZ * f5);
        switch1.render(f5);
        LegacyGlState.glPopMatrix();
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(switchPanel.offsetX, switchPanel.offsetY, switchPanel.offsetZ);
        LegacyGlState.glTranslatef(switchPanel.rotationPointX * f5, switchPanel.rotationPointY * f5, switchPanel.rotationPointZ * f5);
        LegacyGlState.glScaled(1.2D, 1.1D, 1.4D);
        LegacyGlState.glTranslatef(-switchPanel.offsetX, -switchPanel.offsetY, -switchPanel.offsetZ);
        LegacyGlState.glTranslatef(-switchPanel.rotationPointX * f5, -switchPanel.rotationPointY * f5, -switchPanel.rotationPointZ * f5);
        switchPanel.render(f5);
        LegacyGlState.glPopMatrix();
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(body1.offsetX, body1.offsetY, body1.offsetZ);
        LegacyGlState.glTranslatef(body1.rotationPointX * f5, body1.rotationPointY * f5, body1.rotationPointZ * f5);
        LegacyGlState.glScaled(1.1D, 1.1D, 1.1D);
        LegacyGlState.glTranslatef(-body1.offsetX, -body1.offsetY, -body1.offsetZ);
        LegacyGlState.glTranslatef(-body1.rotationPointX * f5, -body1.rotationPointY * f5, -body1.rotationPointZ * f5);
        body1.render(f5);
        LegacyGlState.glPopMatrix();
        LegacyGlState.glPushMatrix();
        LegacyGlState.glTranslatef(switch2.offsetX, switch2.offsetY, switch2.offsetZ);
        LegacyGlState.glTranslatef(switch2.rotationPointX * f5, switch2.rotationPointY * f5, switch2.rotationPointZ * f5);
        LegacyGlState.glScaled(0.5D, 0.6D, 1.0D);
        LegacyGlState.glTranslatef(-switch2.offsetX, -switch2.offsetY, -switch2.offsetZ);
        LegacyGlState.glTranslatef(-switch2.rotationPointX * f5, -switch2.rotationPointY * f5, -switch2.rotationPointZ * f5);
        switch2.render(f5);
        LegacyGlState.glPopMatrix();
    }

    public void setRotateAngle(LegacyModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
