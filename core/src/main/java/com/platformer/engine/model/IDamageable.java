package com.platformer.engine.model;

public interface IDamageable {
    void takeDamage(int amount);
    int getHp();
    boolean isDead();
}
