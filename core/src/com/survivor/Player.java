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
    private static final float COLLIDER_WIDTH = WIDTH / 2.5f;
    private static final float COLLIDER_HEIGHT = HEIGHT * 0.8f;
    private static final float COLLIDER_OFFSET = (WIDTH - COLLIDER_WIDTH) / 2;

    private static final int HORIZONTAL_SPEED = 500;
    private static SpriteSheet spriteSheet;
    private static final float JUMP_VELOCITY = 800;

    private boolean rightFacing;
    public final Rectangle renderPosition;
    public final Rectangle bodyCollider;
    public final Rectangle swordCollider;
    private final Vector2 velocity;
    private float directionTimer;
    private float animTimer;
    private float totalTime;
    private boolean attacking;
    private boolean jumping;

    public Player() {
        renderPosition = new Rectangle(
                (SurvivorGame.SCENE_WIDTH - WIDTH) / 2f,
                SurvivorGame.GROUND_HEIGHT,
                WIDTH,
                HEIGHT
        );
        bodyCollider = new Rectangle(
                (SurvivorGame.SCENE_WIDTH - COLLIDER_WIDTH) / 2f,
                SurvivorGame.GROUND_HEIGHT,
                COLLIDER_WIDTH,
                COLLIDER_HEIGHT
        );
        swordCollider = new Rectangle(
                (SurvivorGame.SCENE_WIDTH - WIDTH) / 2f,
                SurvivorGame.GROUND_HEIGHT,
                COLLIDER_OFFSET,
                HEIGHT
        );
        velocity = new Vector2();
    }

    public void update(SpriteBatch batch, float delta) {
        totalTime += delta;

        // don't begin jump while attacking
        if (!attacking) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                attacking = true;
                animTimer = 0;
                directionTimer = 0;
            } else if (!jumping && Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                jumping = true;
                velocity.y = JUMP_VELOCITY;
                animTimer = 0;
                directionTimer = 0;
            }
        }
        // don't allow movement while attacking
        if (!attacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                velocity.x = HORIZONTAL_SPEED;
                // Update direction
                if (!rightFacing) {
                    directionTimer = 0;
                    rightFacing = true;
                }
                // Used to update run animation frame
                directionTimer += Gdx.graphics.getDeltaTime();
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                velocity.x = -HORIZONTAL_SPEED;
                // Update direction
                if (rightFacing) {
                    directionTimer = 0;
                    rightFacing = false;
                }
                // Used to update run animation frame
                directionTimer -= Gdx.graphics.getDeltaTime();
            } else {
                directionTimer = 0;
                velocity.x = 0;
            }
        }

        // decide what to render
        TextureRegion renderedImage;
        if (attacking) {
            renderedImage = spriteSheet.getFrame("attack", animTimer);
            animTimer += delta;
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
            animTimer += delta;
        } else {
            if (velocity.x > 0) {
                renderedImage = spriteSheet.getFrame("run", directionTimer);
            } else if (velocity.x < 0) {
                renderedImage = spriteSheet.getFrame("run", -directionTimer);
            } else {
                renderedImage = spriteSheet.getFrame("idle", totalTime);
            }
        }

        updateMotion(delta);

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
            jumping = false;
            bodyCollider.y = SurvivorGame.GROUND_HEIGHT;
            velocity.y = 0;
        }
        if (bodyCollider.x < 0) {
            bodyCollider.x = 0;
        }
        if (bodyCollider.x > SurvivorGame.SCENE_WIDTH - bodyCollider.width) {
            bodyCollider.x = SurvivorGame.SCENE_WIDTH - bodyCollider.width;
        }

        renderPosition.x = bodyCollider.x - COLLIDER_OFFSET;
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

    public float getCentreX() {
        return renderPosition.x + WIDTH / 2f;
    }

    public static void create() {
        spriteSheet = new SpriteSheet(
                "adventurer.png",
                FRAME_WIDTH, FRAME_HEIGHT,
                8, 12
        );
        spriteSheet.loadAnim("idle", 0, 4, 0.8f);
        spriteSheet.loadAnim("run", 8, 14, 0.8f);
        spriteSheet.loadAnim("attack", 42, 48, 0.4f);
        spriteSheet.loadAnim("jumpUp", 77, 79, 0.12f);
        spriteSheet.loadAnim("jumpDown", 79, 81, 0.12f);
    }

    public static void dispose() {
        spriteSheet.dispose();
    }

    public void debug(SurvivorGame game) {
        if (attacking) {
            game.drawRedRectangle(swordCollider);
        } else {
            game.drawRedRectangle(renderPosition);
            game.drawRedRectangle(bodyCollider);
        }
    }
}
