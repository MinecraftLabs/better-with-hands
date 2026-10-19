package com.sajmonoriginal.betterwithhands.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface OffhandFirstPerson {
    boolean betterWithHands$isRenderingOffhand();
}
