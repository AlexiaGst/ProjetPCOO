package com.platformer.engine.model;

public interface IStrategy {
    /**
     * Exécute le comportement de l'IA.
     * @param agent L'agent qui possède cette stratégie
     * @param deltaTime Temps écoulé
     */
    void execute(Agent agent, float deltaTime);
}
