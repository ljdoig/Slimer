package com.survivor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;

public class MenuScreen implements Screen {
    private final SurvivorGame game;
    private final Player player;

    public MenuScreen(SurvivorGame game) {
        this.game = game;
        player = new Player();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        game.updateCamera(player.getCentreX());
        game.batch.begin();

        game.batch.draw(game.sky, game.camera.position.x - SurvivorGame.WIDTH / 2f, 0);
        game.batch.draw(game.ground, 0, 0);
        game.batch.draw(game.ground, SurvivorGame.WIDTH, 0);

        player.update(game.batch, delta, game.camera.position.x, true);

        game.drawCentredText(
                "Press ENTER to play",
                SurvivorGame.WIDTH / 2f,
                SurvivorGame.HEIGHT * 3 / 4f
        );

        game.batch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            game.setScreen(new PlayScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
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
