package com.survivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import utils.SpriteSheet;

public class Player {
    private static final int FRAME_WIDTH = 50;
    private static final int FRAME_HEIGHT = 37;
    private static final int SCALE_FACTOR = 6;
    private static final int WIDTH = FRAME_WIDTH * SCALE_FACTOR;
    private static final int HEIGHT = FRAME_HEIGHT * SCALE_FACTOR;
    private static final float BODY_COLLIDER_WIDTH = WIDTH / 3f;
    private static final float BODY_COLLIDER_HEIGHT = HEIGHT * 0.8f;
    private static final float BODY_COLLIDER_OFFSET = (WIDTH - BODY_COLLIDER_WIDTH)/2;
    private static final float SWORD_COLLIDER_WIDTH = BODY_COLLIDER_OFFSET*0.9f;

    private static final int HORIZONTAL_SPEED = 500;
    private static SpriteSheet spriteSheet;
    private static final float JUMP_VELOCITY = 800;
    private static final float HURT_TIME_S = 1f;
    private static final int HURT_FLICKERS = 3;
    private static final int MAX_HEALTH = 3;

    private boolean rightFacing;
    private final Rectangle renderPosition;
    private final Rectangle bodyCollider;
    private final Rectangle swordCollider;
    private final Vector2 velocity;
    private final LifeBar lifeBar;
    private int health;
    private float directionTimer;
    private float animTimer;
    private float totalTime;
    private float hurtTimer;
    private boolean attacking;
    private boolean jumping;
    private boolean doubleJumping;
    private boolean hurt;
    private boolean dying;
    private boolean dead;

    public Player(float x) {
        renderPosition = new Rectangle(
                x,
                SurvivorGame.GROUND_HEIGHT,
                WIDTH,
                HEIGHT
        );
        bodyCollider = new Rectangle(
                renderPosition.x + BODY_COLLIDER_OFFSET,
                renderPosition.y,
                BODY_COLLIDER_WIDTH,
                BODY_COLLIDER_HEIGHT
        );
        swordCollider = new Rectangle(0, 0, SWORD_COLLIDER_WIDTH, HEIGHT);
        velocity = new Vector2();
        health = MAX_HEALTH;
        lifeBar = new LifeBar(MAX_HEALTH);
    }

    public Player() {
        this((SurvivorGame.SCENE_WIDTH - WIDTH) / 2f);
    }

    public void update(SpriteBatch batch, float delta, float cameraX, boolean _static) {
        totalTime += delta;
        animTimer += delta;

        updateMotion(delta);

        if (dying) {
            if (animTimer > spriteSheet.getDuration("die")) {
                dead = true;
            } else {
                draw(batch, spriteSheet.getFrame("die", animTimer));
            }
            return;
        }

        if (!_static) lifeBar.render(batch, health, cameraX);

        if (Slime.collidesWithAny(bodyCollider)) {
            takeDamage();
        }

        // don't begin jump while attacking
        if (!_static && !attacking) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                attacking = true;
                animTimer = 0;
                directionTimer = 0;
            } else if (!doubleJumping && Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                if (jumping) {
                    doubleJumping = true;
                } else {
                    jumping = true;
                }
                velocity.y = JUMP_VELOCITY;
                animTimer = 0;
                directionTimer = 0;
            }
        }
        // don't allow movement while attacking
        if (!attacking && !_static) {
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                velocity.x = HORIZONTAL_SPEED;
                // Update direction
                if (!rightFacing) {
                    directionTimer = 0;
                    rightFacing = true;
                }
                // Used to update run animation frame
                directionTimer += delta;
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                velocity.x = -HORIZONTAL_SPEED;
                // Update direction
                if (rightFacing) {
                    directionTimer = 0;
                    rightFacing = false;
                }
                // Used to update run animation frame
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
        } else if (jumping) {
            if (velocity.y > 0) {
                renderedImage = spriteSheet.getFrame("jumpUp", animTimer);
            } else {
                renderedImage = spriteSheet.getFrame("jumpDown", animTimer);
                if (!renderedImage.isFlipX()) {
                    renderedImage.flip(true, false);
                }
            }
        } else {
            if (velocity.x > 0) {
                renderedImage = spriteSheet.getFrame("run", directionTimer);
            } else if (velocity.x < 0) {
                renderedImage = spriteSheet.getFrame("run", -directionTimer);
            } else {
                renderedImage = spriteSheet.getFrame("idle", totalTime);
            }
        }

        if (hurt) {
            hurtTimer += Gdx.graphics.getDeltaTime();
            if (hurtTimer > HURT_TIME_S) {
                hurt = false;
            }
            float hurtProportion = hurtTimer / HURT_TIME_S;
            if ((hurtProportion * HURT_FLICKERS) % 1 > 0.5f) {
                draw(batch, renderedImage);
            }
        } else {
            draw(batch, renderedImage);
        }
    }

    private void draw(SpriteBatch batch, TextureRegion renderedImage) {
        if (!rightFacing) renderedImage.flip(true, false);
        batch.draw(
                renderedImage,
                renderPosition.x, renderPosition.y,
                renderPosition.width, renderPosition.height
        );
        if (!rightFacing) renderedImage.flip(true, false);
    }

    private void updateMotion(float delta) {
        bodyCollider.x += velocity.x * delta;
        bodyCollider.y += velocity.y * delta;
        if (bodyCollider.y > SurvivorGame.GROUND_HEIGHT) {
            // in the air: apply gravity
            velocity.y -= SurvivorGame.GRAVITY * delta;
        } else if (bodyCollider.y == SurvivorGame.GROUND_HEIGHT) {
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
        } else {
            // below the ground: finish jumping
            jumping = doubleJumping = false;
            bodyCollider.y = SurvivorGame.GROUND_HEIGHT;
            velocity.y = 0;
        }
        if (bodyCollider.x < 0) {
            bodyCollider.x = 0;
        }
        if (bodyCollider.x > SurvivorGame.SCENE_WIDTH - bodyCollider.width) {
            bodyCollider.x = SurvivorGame.SCENE_WIDTH - bodyCollider.width;
        }

        renderPosition.x = bodyCollider.x - BODY_COLLIDER_OFFSET;
        renderPosition.y = bodyCollider.y;

        if (attacking) {
            if (rightFacing) {
                swordCollider.x = bodyCollider.x + bodyCollider.width;
            } else {
                swordCollider.x = bodyCollider.x - swordCollider.width;
            }
            swordCollider.y = bodyCollider.y;
        }

    }

    private void takeDamage() {
        if (!hurt) {
            health--;
            hurt = true;
            hurtTimer = 0;
            if (health == 0) {
                dying = true;
                animTimer = 0;
            }
        }
    }

    public boolean isDying() {
        return dying;
    }

    public boolean isDead() {
        return dead;
    }

    public float getCentreX() {
        return renderPosition.x + renderPosition.width / 2f;
    }

    public Rectangle getSwordCollider() {
        float attackProportion = animTimer / spriteSheet.getDuration("attack");
        boolean swordOut = 0.25 < attackProportion && attackProportion < 0.5;
        return attacking && swordOut ? swordCollider : null;
    }

    public static void create() {
        spriteSheet = new SpriteSheet(
                "adventurer.png",
                FRAME_WIDTH, FRAME_HEIGHT,
                8, 12
        );
        spriteSheet.loadAnim("idle", 0.8f, 0, 4);
        spriteSheet.loadAnim("run", 0.8f, 8, 14);
        spriteSheet.loadAnim("attack", 0.3f, 42, 48);
        spriteSheet.loadAnim("jumpUp", 0.12f, 77, 79);
        spriteSheet.loadAnim("jumpDown", 0.12f, 79, 81);
        spriteSheet.loadAnim(
                "die", 4f,
                62, 63, 64, 65, 66, 67, 68, 66, 67, 68, 68, 68, 68
        );
    }

    public static void dispose() {
        spriteSheet.dispose();
    }

    public void debug(SurvivorGame game) {
        if (getSwordCollider() != null) {
            game.drawRedRectangle(swordCollider);
        } else {
            game.drawRedRectangle(bodyCollider);
        }
    }

}
