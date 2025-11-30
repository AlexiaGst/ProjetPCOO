package com.platformer.engine.event;

import com.platformer.engine.model.Entity;

public interface IGameObserver {
    // On définit des types d'événements génériques (enum ou int)
    void onNotify(Entity entity, String eventName);
}
