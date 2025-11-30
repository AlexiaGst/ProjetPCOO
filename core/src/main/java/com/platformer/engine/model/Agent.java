package com.platformer.engine.model;


import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public abstract class Agent extends Entity implements IMovable,IDamageable {
    private final float hitboxOffsetX;
    private final float hitboxOffsetY;
    private int hp;
    protected float velocityX = 0;
    protected float velocityY = 0;
    protected float speed = 10f;
    private boolean facingRight = true;
    protected Rectangle hitBox;
    protected Polygon polygonHitBox;
    protected Array<Polygon> worldObstacles;
    private boolean onGround = true;
    private float invincibilityTimer = 0;
    private boolean isDead = false;

    public Agent(String textureKey, float x, float y, float width, float height, int hp) {
        super(textureKey, x, y, width, height);
        this.hp = hp;
        this.hitBox = new Rectangle(x, y, width, height);

        // --- HITBOX POLYGONE ---
        // On la réduit très légèrement (Skin width) pour éviter les frottements
        float w = width * 0.98f;  // 98% de la largeur
        float h = height * 0.98f; // 98% de la hauteur
        float offsetX = (width - w) / 2f; // Pour centrer
        float offsetY = (height - h) / 2f;

        float[] vertices = new float[] {
            0, 0,
            w, 0,
            w, h,
            0, h
        };

        this.polygonHitBox = new Polygon(vertices);
        this.polygonHitBox.setOrigin(0, 0);

        // IMPORTANT : On stocke cet offset pour l'utiliser dans updateHitbox
        this.hitboxOffsetX = offsetX;
        this.hitboxOffsetY = offsetY;
    }

    public void setWorldObstacles(Array<Polygon> obstacles) {
        this.worldObstacles = obstacles;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Gestion purement visuelle des états
        if (onGround) {
            if (Math.abs(velocityX) > 0.1f) setState("RUN");
            else setState("IDLE");
        } else {
            setState("JUMP");
        }

        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaTime;
        }
    }

    public void updateHitbox() {
        hitBox.setPosition(getX(), getY());
        polygonHitBox.setPosition(getX() + hitboxOffsetX, getY() + hitboxOffsetY);
    }

    @Override
    public void move(float dx, float dy) {
        // CAS 1 : L'utilisateur appuie sur une touche (Accélération immédiate)
        if (dx != 0) {
            this.velocityX = dx * speed;
            this.facingRight = (dx > 0);
        }
        // CAS 2 : L'utilisateur ne touche rien (Friction)
        else {
            // Ta formule de friction
            this.velocityX *= 0.8f; // On réduit la vitesse de 10% à chaque frame

            // Seuil d'arrêt complet (pour éviter les micro-glissements infinis)
            if (Math.abs(this.velocityX) < 0.5f) {
                this.velocityX = 0;
            }
        }

        // --- GESTION DE L'ÉTAT (ANIMATION) ---
        // Correction importante : L'état dépend de la VITESSE réelle, pas de l'input dx.
        // Si on glisse encore un peu, on reste en "RUN".

        // On ne change l'état que si on est au sol (en l'air c'est JUMP)
        if (onGround) {
            if (Math.abs(this.velocityX) > 0.5f) { // Si on bouge encore significativement
                setState("RUN");
            } else {
                setState("IDLE");
            }
        }
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void die(){
        // 1. On change l'état pour que le Renderer change d'image
        setState("DEAD");

        // 2. On coupe la physique (pour qu'il arrête de bouger)
        this.velocityX = 0;

        // 3. On désactive sa Hitbox pour ne plus blesser le joueur
        // (Une astuce simple est de déplacer la hitbox très loin ou de mettre un flag 'isDead')
        this.hitBox.set(0, -1000, 0, 0);

        // 4. Timer pour supprimer l'entité après l'animation (ex: 1 seconde)
        // (Tu devras gérer ça dans le update avec un deathTimer)
    }

    // Méthode pour recevoir des dégâts
    public void takeDamage(int amount) {
        if (isDead || invincibilityTimer > 0) return;

        this.hp -= amount;

        // Petit effet de recul universel (optionnel)
        // this.velocityX = -this.velocityX;

        if (this.hp <= 0) {
            this.hp = 0;
            this.isDead = true;
            die(); // Appel abstrait ou méthode locale
        } else {
            // Invincible pendant 0.5s après un coup
            invincibilityTimer = 0.5f;
        }
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public float getVelocityX() {
        return velocityX;
    }
    public float getVelocityY() {
        return velocityY;
    }
    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }
    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }
    public boolean isOnGround() {
        return onGround;
    }
    public boolean isDead() { return isDead; }
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public Polygon getPolygonHitBox() {
        return polygonHitBox;
    }


}
