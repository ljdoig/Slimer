package com.survivor;

import com.badlogic.gdx.Screen;

public class PlayScreen implements Screen {
    private final SurvivorGame game;
    private final Player player;

    public PlayScreen(SurvivorGame game) {
        this.game = game;
        player = new Player();
    }

    @Override
    public void render(float delta) {
        float playerX = player.getCentreX();
        game.updateCamera(playerX);

        game.batch.begin();
        // sky should appear stationary so draw relative to camera
        game.batch.draw(game.sky, game.camera.position.x - SurvivorGame.WIDTH / 2f, 0);
        // ground should appear to move under player
        int nearestGround = Math.floorDiv((int) playerX, SurvivorGame.WIDTH);
        // draw ground under player
        game.batch.draw(game.ground, nearestGround * SurvivorGame.WIDTH, 0);
        // and ground to the left and right of this to cover the screen
        game.batch.draw(game.ground, (nearestGround + 1) * SurvivorGame.WIDTH, 0);
        game.batch.draw(game.ground, (nearestGround - 1) * SurvivorGame.WIDTH, 0);
        player.update(game.batch, delta);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void show() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
