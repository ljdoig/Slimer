package com.survivor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import utils.SpriteSheet;

import java.util.Iterator;

public class Slime {
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 25;
    private static final int SCALE_FACTOR = 6;
    private static final int WIDTH = FRAME_WIDTH * SCALE_FACTOR;
    private static final int HEIGHT = FRAME_HEIGHT * SCALE_FACTOR;
    private static final float COLLIDER_WIDTH = WIDTH / 1.5f;
    private static final float COLLIDER_HEIGHT = HEIGHT / 2f;
    private static final float COLLIDER_OFFSET = (WIDTH - COLLIDER_WIDTH) / 2;
    private static final float ATTACK_COOLDOWN = 1;
    private static final int HORIZONTAL_SPEED = 75;
    private static final int ATTACK_SPEED = 750;
    private static final int RECOIL_SPEED = 750;
    private static final float DEFAULT_SPAWN_INTERVAL = 5;
    private static int deadSlimeCount = 0;
    private static float spawnInterval = DEFAULT_SPAWN_INTERVAL;
    private static SpriteSheet spriteSheet;

    private static Array<Slime> slimes;
    private static float spawnTimer;

    public final Rectangle renderPosition;
    public final Rectangle bodyCollider;
    private final Vector2 velocity;
    private float directionTimer;
    private float animTimer;
    private float lastAttackTimer;
    private float lifeTimer;
    private boolean rightFacing;
    private boolean attacking;
    private boolean spawning;
    private boolean dying;
    private boolean dead;

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
        spawning = true;
        rightFacing = MathUtils.randomBoolean();
    }

    public void update(SpriteBatch batch, float delta, Player player) {
        lifeTimer += delta;
        animTimer += delta;
        lastAttackTimer += delta;

        updateMotion(delta);

        assert !dead;
        // spawning or dying behaviour; in each case return early
        if (spawning || dying) {
            String animation = dying ? "die" : "spawn";
            if (animTimer > spriteSheet.getDuration(animation)) {
                // finished with current state
                if (spawning) {
                    spawning = false;
                } else {
                    dying = false;
                    dead = true;
                }
            } else {
                TextureRegion frame = spriteSheet.getFrame(animation, animTimer);
                if (rightFacing) frame.flip(true, false);
                batch.draw(
                        frame,
                        renderPosition.x, renderPosition.y,
                        renderPosition.width, renderPosition.height
                );
                if (rightFacing) frame.flip(true, false);
            }
            return;
        }

        // check if Slime has been struck
        Rectangle swordCollider = player.getSwordCollider();
        if ((swordCollider != null && swordCollider.overlaps(bodyCollider)) ||
                player.isDying()) {
            dying = true;
            animTimer = 0;
            if (!player.isDying()) {
                deadSlimeCount++;
                velocity.x = rightFacing ? -RECOIL_SPEED : RECOIL_SPEED;
            }
            return;
        }

        // don't begin attack while attacking or while cooling down
        float playerX = player.getCentreX();
        if (!attacking && lastAttackTimer > ATTACK_COOLDOWN) {
            // attack to the left
            if (bodyCollider.x - WIDTH < playerX && playerX < bodyCollider.x) {
                rightFacing = false;
                velocity.x = -ATTACK_SPEED;
                attacking = true;
                animTimer = 0;
                directionTimer = 0;
                lastAttackTimer = 0;
            }
            // attack to the rights
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
        if (attacking) {
            renderedImage = spriteSheet.getFrame("attack", animTimer);
            if (animTimer > spriteSheet.getDuration("attack")) {
                attacking = false;
            }
        } else {
            if (velocity.x > 0) {
                renderedImage = spriteSheet.getFrame("move", directionTimer);
            } else if (velocity.x < 0) {
                renderedImage = spriteSheet.getFrame("move", -directionTimer);
            } else {
                renderedImage = spriteSheet.getFrame("idle", lifeTimer);
            }
        }

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
        spriteSheet.loadAnim("idle", 0.6f, 0, 4);
        spriteSheet.loadAnim("move", 0.6f, 4, 8);
        spriteSheet.loadAnim("die", 0.4f, 16, 17, 18, 19, 19, 20, 20);
        spriteSheet.loadAnim("attack", 0.6f, 8, 9, 10, 10, 10, 11, 11, 12);
        spriteSheet.loadAnim("spawn", 1f, 20, 20, 19, 19, 18, 17, 16);

        slimes = new Array<>();
        spawn();
    }

    public static void reset(float firstSlimeX) {
        spawnInterval = DEFAULT_SPAWN_INTERVAL;
        deadSlimeCount = 0;
        slimes = new Array<>();
        spawn(firstSlimeX);
    }

    public static void dispose() {
        spriteSheet.dispose();
    }

    public static void spawn() {
        float x = MathUtils.random(
                COLLIDER_OFFSET,
                SurvivorGame.SCENE_WIDTH - COLLIDER_WIDTH
        );
        spawn(x);
    }

    public static void spawn(float x) {
        slimes.add(new Slime(x));
        spawnInterval *= 0.95;
        spawnTimer = 0;
    }

    public static void updateAll(SpriteBatch batch, float delta, Player player) {
        spawnTimer += delta;
        if (spawnTimer > spawnInterval && !player.isDying()) {
            spawn();
        }
        for (Iterator<Slime> iter = slimes.iterator(); iter.hasNext();) {
            Slime slime = iter.next();
            slime.update(batch, delta, player);
            if (slime.dead) {
                iter.remove();
            }
        }
    }

    public static boolean collidesWithAny(Rectangle rectangle) {
        for (Slime slime : slimes) {
            if (!slime.dying && !slime.spawning &&
                    slime.bodyCollider.overlaps(rectangle)) {
                return true;
            }
        }
        return false;
    }

    public static void debug(SurvivorGame game) {
        for (Slime slime : slimes) {
            game.drawRedRectangle(slime.bodyCollider);
        }
    }

    public static int getDeadSlimeCount() {
        return deadSlimeCount;
    }

}
