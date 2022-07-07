package com.survivor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import utils.SpriteSheet;

public class Slime {
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 25;
    private static final int SCALE_FACTOR = 6;
    private static final int WIDTH = FRAME_WIDTH * SCALE_FACTOR;
    private static final int HEIGHT = FRAME_HEIGHT * SCALE_FACTOR;
    private static final float COLLIDER_WIDTH = WIDTH / 2f;
    private static final float COLLIDER_HEIGHT = HEIGHT / 2f;
    private static final float COLLIDER_OFFSET = (WIDTH - COLLIDER_WIDTH) / 2;
    private static final float SPAWN_INTERVAL = 10;
    private static final float ATTACK_COOLDOWN = 1;
    private static final int HORIZONTAL_SPEED = 50;
    private static final int ATTACK_SPEED = 500;
    private static SpriteSheet spriteSheet;

    private static Array<Slime> slimes;
    private static float spawnTimer;

    public final Rectangle renderPosition;
    public final Rectangle bodyCollider;
    private final Vector2 velocity;
    private float directionTimer;
    private float animTimer;
    private float lastAttackTimer;
    private float lifeTime;
    private boolean rightFacing;
    private boolean attacking;
    private boolean dying;

    public Slime(float x) {
        renderPosition = new Rectangle(
                x, SurvivorGame.GROUND_HEIGHT,
                WIDTH, HEIGHT
        );
        bodyCollider = new Rectangle(
                renderPosition.x + COLLIDER_OFFSET, SurvivorGame.GROUND_HEIGHT,
                COLLIDER_WIDTH, COLLIDER_HEIGHT
        );
        velocity = new Vector2();
    }

    public void update(SpriteBatch batch, float delta, float playerX) {
        lifeTime += delta;
        lastAttackTimer += delta;

        // don't begin attack while attacking or while cooling down
        if (!attacking && lastAttackTimer > ATTACK_COOLDOWN) {
            if (bodyCollider.x - WIDTH < playerX && playerX < bodyCollider.x) {
                rightFacing = false;
                velocity.x = -ATTACK_SPEED;
                attacking = true;
                animTimer = 0;
                directionTimer = 0;
                lastAttackTimer = 0;
            }
            if (bodyCollider.x + 2 * WIDTH > playerX &&
                    playerX > bodyCollider.x + WIDTH) {
                rightFacing = true;
                velocity.x = ATTACK_SPEED;
                attacking = true;
                animTimer = 0;
                directionTimer = 0;
                lastAttackTimer = 0;
            }
        }

        if (!attacking) {
            if (playerX > bodyCollider.x + bodyCollider.width) {
                velocity.x = HORIZONTAL_SPEED;
                // Update direction
                if (!rightFacing) {
                    directionTimer = 0;
                    rightFacing = true;
                }
                // Used to update move animation frame
                directionTimer += delta;
            } else if (playerX < bodyCollider.x) {
                velocity.x = -HORIZONTAL_SPEED;
                // Update direction
                if (rightFacing) {
                    directionTimer = 0;
                    rightFacing = false;
                }
                // Used to update move animation frame
                directionTimer -= delta;
            } else {
                directionTimer = 0;
                velocity.x = 0;
            }
        }

        // decide what to render
        TextureRegion renderedImage;
        if (dying) {
            renderedImage = spriteSheet.getFrame("die", animTimer);
        } else if (attacking) {
            renderedImage = spriteSheet.getFrame("attack", animTimer);
            animTimer += delta;
            if (animTimer > spriteSheet.getDuration("attack")) {
                attacking = false;
            }
        } else {
            if (velocity.x > 0) {
                renderedImage = spriteSheet.getFrame("move", directionTimer);
            } else if (velocity.x < 0) {
                renderedImage = spriteSheet.getFrame("move", -directionTimer);
            } else {
                renderedImage = spriteSheet.getFrame("idle", lifeTime);
            }
        }

        updateMotion(delta);

        if (rightFacing) renderedImage.flip(true, false);
        batch.draw(
                renderedImage,
                renderPosition.x, renderPosition.y,
                renderPosition.width, renderPosition.height
        );
        if (rightFacing) renderedImage.flip(true, false);
    }

    private void updateMotion(float delta) {
        bodyCollider.x += velocity.x * delta;
        bodyCollider.y += velocity.y * delta;

        // on the ground: apply friction in opposite direction to motion
        if (velocity.x > 0) {
            velocity.x -= SurvivorGame.FRICTION * delta;
            // check we don't flip the sign
            if (velocity.x < 0) velocity.x = 0;
        } else if (velocity.x < 0) {
            velocity.x += SurvivorGame.FRICTION * delta;
            // check we don't flip the sign
            if (velocity.x > 0) velocity.x = 0;
        }

        if (bodyCollider.x < 0) {
            bodyCollider.x = 0;
        }
        if (bodyCollider.x > SurvivorGame.SCENE_WIDTH - bodyCollider.width) {
            bodyCollider.x = SurvivorGame.SCENE_WIDTH - bodyCollider.width;
        }

        renderPosition.x = bodyCollider.x - COLLIDER_OFFSET;
        renderPosition.y = bodyCollider.y;
    }

    public static void create() {
        spriteSheet = new SpriteSheet(
                "slime.png",
                FRAME_WIDTH, FRAME_HEIGHT,
                8, 3
        );
        spriteSheet.loadAnim("idle", 0, 4, 0.8f);
        spriteSheet.loadAnim("move", 4, 8, 0.8f);
        spriteSheet.loadAnim("attack", 8, 12, 0.5f);
        spriteSheet.loadAnim("hurt", 12, 16, 0.12f);
        spriteSheet.loadAnim("die", 16, 21, 0.12f);

        slimes = new Array<>();
        spawn();
    }

    public static void dispose() {
        spriteSheet.dispose();
    }

    public static void spawn() {
        float x = MathUtils.random(
                COLLIDER_OFFSET,
                SurvivorGame.SCENE_WIDTH - COLLIDER_WIDTH
        );
        slimes.add(new Slime(x));
    }

    public static void updateAll(SpriteBatch batch, float delta, float playerX) {
        spawnTimer += delta;
        if (spawnTimer > SPAWN_INTERVAL) {
            spawn();
            spawnTimer = 0;
        }
        for (Slime slime : slimes) {
            slime.update(batch, delta, playerX);
        }
    }

    public static void debug(SurvivorGame game) {
        for (Slime slime : slimes) {
            game.drawRedRectangle(slime.renderPosition);
            game.drawRedRectangle(slime.bodyCollider);
        }
    }
}
