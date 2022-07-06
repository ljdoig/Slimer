package com.survivor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import utils.FontLoader;

public class SurvivorGame extends Game {
	public static final int WIDTH = 1920;
	public static final int HEIGHT = 1080;
	public static final int GROUND_HEIGHT = 250;
	public static final int FONT_SIZE = 34;

	public static final float GRAVITY = 2000;
	public static final float FRICTION = 1000;

	public Texture sky;
	public Texture ground;
	public OrthographicCamera camera;
	private FitViewport viewport;
	public SpriteBatch batch;
	private GlyphLayout glyphLayout;
	private BitmapFont font;

	@Override
	public void create() {
		Player.create();

		sky = new Texture("sky.png");
		ground = new Texture("ground.png");

		camera = new OrthographicCamera();
		camera.setToOrtho(false, WIDTH, HEIGHT);
		viewport = new FitViewport(WIDTH, HEIGHT, camera);
		viewport.apply();
		batch = new SpriteBatch();
		glyphLayout = new GlyphLayout();
		font = FontLoader.load("Lotuscoder.ttf", FONT_SIZE);

		setScreen(new PlayScreen(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		Player.dispose();

		batch.dispose();
		sky.dispose();
		ground.dispose();
		font.dispose();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	public void drawCentredText(String s, float x, float y) {
		glyphLayout.setText(font, s);
		font.draw(
				batch,
				glyphLayout,
				x - glyphLayout.width / 2,
				y + glyphLayout.height / 2
		);
	}

	public void updateCamera(float positionX) {
		camera.position.set(positionX, HEIGHT / 2f, 0);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
	}
}
