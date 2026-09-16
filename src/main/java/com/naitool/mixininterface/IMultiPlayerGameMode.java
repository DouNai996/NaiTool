package com.naitool.mixininterface;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;

public interface IMultiPlayerGameMode {
    void naitool$syncSelected();

    void naitool$startPrediction(ClientLevel level, PredictiveAction action);
}
