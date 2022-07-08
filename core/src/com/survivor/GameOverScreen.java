package com.survivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;

public class GameOverScreen implements Screen {
    private static int highScore;
    private final SurvivorGame game;
    private final float deadPlayerX;
    private final int score;

    public GameOverScreen(SurvivorGame game, float deadPlayerX, int score) {
        this.game = game;
        this.deadPlayerX = deadPlayerX;
        this.score = score;
        highScore = Math.max(score, highScore);
    }

    @Override
    public void render(float delta) {
        game.updateCamera(deadPlayerX);

        game.batch.begin();

        // sky should appear stationary so draw relative to camera
        game.batch.draw(game.sky, game.camera.position.x - SurvivorGame.WIDTH / 2f, 0);
        // ground should appear to move under player, covers multiple 'grounds'
        game.batch.draw(game.ground, 0, 0);
        game.batch.draw(game.ground, SurvivorGame.WIDTH, 0);

        game.drawCentredText(
                "GAME OVER!",
                SurvivorGame.WIDTH / 2f,
                SurvivorGame.HEIGHT * 14 / 16f
        );
        game.drawCentredText(
                String.format("SCORE: %d    HIGH SCORE: %d", score, highScore),
                SurvivorGame.WIDTH / 2f,
                SurvivorGame.HEIGHT * 13 / 16f
        );
        game.drawCentredText(
                "Press ENTER to play again",
                SurvivorGame.WIDTH / 2f,
                SurvivorGame.HEIGHT * 12 / 16f
        );

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            game.setScreen(new PlayScreen(game));
        }

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
