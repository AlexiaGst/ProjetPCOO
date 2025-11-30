package com.platformer.engine.model;

import com.platformer.engine.event.IGameObserver;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {
    private float x,y,width,height;
    private String textureKey;
    private String state="IDLE";
    private float stateTimer=0;
    protected transient List<IGameObserver> observers = new ArrayList<>();

    public Entity(String textureKey, float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureKey = textureKey;
    }
    public float getX() {return x;}
    public void setX(float x) {this.x = x;}
    public float getY() {return y;}
    public void setY(float y) {this.y = y;}
    public float getWidth() {return width;}
    public float getHeight() {return height;}
    public String getTextureKey() { return textureKey; }
    public String getState() { return state; }
    public void setState(String newState) {
        if (!this.state.equals(newState)) {
            this.state = newState;
            this.stateTimer = 0;
        }
    }    public float getStateTimer() { return stateTimer; }

    public void update(float deltaTime) {
        stateTimer += deltaTime;
    }

    public void addObserver(IGameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(IGameObserver observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(String eventName) {
        for (IGameObserver observer : observers) {
            observer.onNotify(this, eventName);
        }
    }
}
