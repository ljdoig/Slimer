package com.survivor;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;

public class PlayScreen implements Screen {
    private final SurvivorGame game;
    private final Player player;

    public PlayScreen(SurvivorGame game) {
        this.game = game;
        player = new Player();
        Slime.reset(player.getCentreX() + SurvivorGame.WIDTH / 4f *
                (MathUtils.randomBoolean() ? 1 : -1)
        );
    }

    @Override
    public void render(float delta) {
        float playerX = player.getCentreX();
        game.updateCamera(playerX, true);

        game.batch.begin();

        // sky should appear stationary so draw relative to camera
        game.batch.draw(game.sky, game.camera.position.x - SurvivorGame.WIDTH / 2f, 0);
        // ground should appear to move under player, covers multiple 'grounds'
        game.batch.draw(game.ground, 0, 0);
        game.batch.draw(game.ground, SurvivorGame.WIDTH, 0);

        Slime.updateAll(game.batch, delta, player);
        player.update(game.batch, delta, game.camera.position.x, false);

        if (!player.isDying()) {
            game.drawCentredText(
                    "Score: "  + Slime.getDeadSlimeCount(),
                    SurvivorGame.WIDTH * 0.9f,
                    SurvivorGame.HEIGHT * 0.915f
            );
        }

        if (SurvivorGame.DEBUG) {
            player.debug(game);
            Slime.debug(game);
        }

        game.batch.end();

        if (player.isDead()) {
            game.setScreen(new GameOverScreen(game, playerX, Slime.getDeadSlimeCount()));
        }
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
