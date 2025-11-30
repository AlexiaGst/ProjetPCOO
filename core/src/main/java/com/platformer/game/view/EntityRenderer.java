package com.platformer.game.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.platformer.engine.model.Entity;
import com.platformer.engine.model.Agent; // On a besoin de Agent pour voir l'état

import java.util.HashMap;

public class EntityRenderer extends com.platformer.engine.view.EntityRenderer {

    // Le dictionnaire des animations (Etat -> Animation)
    private HashMap<String, Animation<TextureRegion>> animations;
    private float stateTime = 0;

    // Une liste pour se souvenir des textures à nettoyer à la fin
    private Array<Texture> texturesToDispose;

    public EntityRenderer() {
        this.animations = new HashMap<>();
        this.texturesToDispose = new Array<>();
    }

    /**
     * Méthode utilitaire pour ajouter une animation à cet ennemi.
     * @param state Le nom de l'état (ex: "RUN", "DEAD", "ATTACK")
     * @param path Le chemin du fichier image
     * @param frameCount Le nombre d'images dans la sheet
     * @param loop Est-ce que ça boucle ? (Vrai pour marcher, Faux pour mourir)
     */
    public void addAnimation(String state, String path, int frameCount, boolean loop) {
        // 1. Chargement
        Texture sheet = new Texture(Gdx.files.internal(path));
        texturesToDispose.add(sheet); // On le garde pour le dispose()

        // 2. Découpe
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / frameCount, sheet.getHeight());
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            frames.add(tmp[0][i]);
        }

        // 3. Création
        Animation.PlayMode mode = loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL;
        Animation<TextureRegion> anim = new Animation<>(0.1f, frames, mode);

        // 4. Stockage
        animations.put(state, anim);
    }

    @Override
    public void render(Entity entity, SpriteBatch batch) {
        if (entity instanceof Agent) {
            Agent agent = (Agent) entity;
            stateTime += Gdx.graphics.getDeltaTime();

            // 1. On récupère l'état actuel de l'ennemi (défini par le CollisionManager ou l'IA)
            String currentState = agent.getState();

            // 2. On cherche l'animation correspondante
            Animation<TextureRegion> currentAnim = animations.get(currentState);


            // Sécurité : Si l'animation n'existe pas (ex: pas d'anim "ATTACK"), on prend "RUN" ou "IDLE" par défaut
            if (currentAnim == null) {
                System.err.println("Attention animation de secours pour "+ currentState+"\n"+animations);
                currentAnim = animations.get("RUN");
                if (currentAnim == null){
                    System.err.println("Rien à afficher pour cette entité");
                    return; // Vraiment rien à afficher...
                }
            }

            // 3. Calcul de la frame
            // Astuce : Si l'état change, il faut idéalement reset le timer dans l'Agent (comme vu précédemment)
            TextureRegion currentFrame = currentAnim.getKeyFrame(agent.getStateTimer(), true);

            // 4. Flip (Miroir)
            boolean isMovingLeft = agent.getVelocityX() < 0;
            boolean isMovingRight = agent.getVelocityX() > 0;

            // Si on va à gauche mais que l'image regarde à droite -> Flip
            if (isMovingLeft && !currentFrame.isFlipX()) currentFrame.flip(true, false);
            // Si on va à droite mais que l'image regarde à gauche -> Flip
            if (isMovingRight && currentFrame.isFlipX()) currentFrame.flip(true, false);

            batch.draw(currentFrame, agent.getX(), agent.getY(), agent.getWidth(), agent.getHeight());
        }
    }

    @Override
    public void dispose() {
        for (Texture t : texturesToDispose) {
            t.dispose();
        }
    }
}
