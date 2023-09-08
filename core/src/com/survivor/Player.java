package com.survivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

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
    private static final float HURT_TIME_S = 0.8f;

    private static SpriteSheet spriteSheet;
    public static final float FRICTION = 3000;
    private static final int HORIZONTAL_SPEED = 500;
    private static final float JUMP_VELOCITY = 800;
    private static final float HURT_VELOCITY = 1250;
    private static final int MAX_HEALTH = 10;

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
    private boolean doubleJumped;
    private boolean hurt;
    private boolean knockedBack;
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

    public void update(SpriteBatch batch, float deltaT, float cameraX, boolean waiting) {
        totalTime += deltaT;
        animTimer += deltaT;

        updateMotion(deltaT);

        if (dying) {
            if (animTimer > spriteSheet.getDuration("die")) {
                dead = true;
            } else {
                draw(batch, spriteSheet.getFrame("die", animTimer));
            }
            return;
        }

        if (!waiting) {
            lifeBar.render(batch, health, cameraX);
            checkDamage();
        }

        // don't begin jump while attacking
        if (!waiting && !attacking && !knockedBack) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                attacking = true;
                animTimer = 0;
                directionTimer = 0;
            } else if (!doubleJumped && Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                if (jumping) {
                    doubleJumping = true;
                    doubleJumped = true;
                } else {
                    jumping = true;
                }
                velocity.y = JUMP_VELOCITY;
                animTimer = 0;
                directionTimer = 0;
            }
        }
        // don't allow movement while attacking
        if (!waiting && !attacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !knockedBack) {
                velocity.x = HORIZONTAL_SPEED;
                // Update direction
                if (!rightFacing) {
                    directionTimer = 0;
                    rightFacing = true;
                }
                // Used to update run animation frame
                directionTimer += deltaT;
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && !knockedBack) {
                velocity.x = -HORIZONTAL_SPEED;
                // Update direction
                if (rightFacing) {
                    directionTimer = 0;
                    rightFacing = false;
                }
                // Used to update run animation frame
                directionTimer -= deltaT;
            } else {
                directionTimer = 0;
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
            if (doubleJumping) {
                renderedImage = spriteSheet.getFrame("flip", animTimer);
                if (animTimer > spriteSheet.getDuration("flip")) {
                    doubleJumping = false;
                    doubleJumped = true;
                }
            } else {
                if (velocity.y > 0) {
                    renderedImage = spriteSheet.getFrame("jumpUp", animTimer);
                } else {
                    renderedImage = spriteSheet.getFrame("jumpDown", animTimer);
                    if (!renderedImage.isFlipX()) {
                        renderedImage.flip(true, false);
                    }
                }
            }
        } else if (knockedBack) {
            renderedImage = spriteSheet.getFrame("hurt", animTimer);
            if (animTimer >  spriteSheet.getDuration("hurt")) {
                knockedBack = false;
            }
        } else {
            // not jumping or attacking
            if (velocity.x > 0) {
                renderedImage = spriteSheet.getFrame("run", directionTimer);
            } else if (velocity.x < 0) {
                renderedImage = spriteSheet.getFrame("run", -directionTimer);
            } else {
                renderedImage = spriteSheet.getFrame("idle", totalTime);
            }
        }

        if (hurt) {
            hurtTimer += deltaT;
            if (hurtTimer > HURT_TIME_S) {
                hurt = false;
            }
        }

        draw(batch, renderedImage);
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
        } else if (bodyCollider.y < SurvivorGame.GROUND_HEIGHT) {
            // below the ground: finish jumping
            jumping = doubleJumping = doubleJumped = false;
            bodyCollider.y = SurvivorGame.GROUND_HEIGHT;
            velocity.y = 0;
        }

        // apply friction/resistance in opposite direction to motion
        if (velocity.x > 0) {
            velocity.x -= FRICTION * delta;
            // check we don't flip the sign
            if (velocity.x < 0) velocity.x = 0;
        } else if (velocity.x < 0) {
            velocity.x += FRICTION * delta;
            // check we don't flip the sign
            if (velocity.x > 0) velocity.x = 0;
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

    private void checkDamage() {
        Slime collidedSlime = Slime.collidedSlime(bodyCollider);
        if (collidedSlime != null) {
            takeDamage();
            if (collidedSlime.getCentreX() < getCentreX()) {
                velocity.x = HURT_VELOCITY;
            } else {
                velocity.x = -HURT_VELOCITY;
            }
            velocity.y = 0;
            knockedBack = true;
            jumping = false;
            animTimer = 0;
            directionTimer = 0;
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
        spriteSheet.loadAnim("flip", 0.3f, 16, 23);
        spriteSheet.loadAnim("jumpUp", 0.12f, 77, 79);
        spriteSheet.loadAnim("jumpDown", 0.12f, 79, 81);
        spriteSheet.loadAnim("hurt", 0.7f, 62, 65);
        spriteSheet.loadAnim(
                "attack", 0.3f,
                42, 43, 44, 44, 45, 46, 47
        );
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
