package utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class SpriteSheet {
    private final Texture sheet;
    private final TextureRegion[][] sheetArray;
    private final int widthInFrames;
    private final int heightInFrames;
    private final HashMap<String, Animation<TextureRegion>> animDict;

    public SpriteSheet(String sheetLocation, int tileWidth, int tileHeight,
                       int widthInFrames, int heightInFrames) {
        this.sheet = new Texture(sheetLocation);
        sheetArray = TextureRegion.split(sheet, tileWidth, tileHeight);
        this.widthInFrames = widthInFrames;
        this.heightInFrames = heightInFrames;
        animDict = new HashMap<>();
    }

    public void loadAnim(String name, float duration, int startIndex, int endIndex) {
        int framesAdded = 0, row, col;
        TextureRegion[] frames = new TextureRegion[endIndex - startIndex];
        assert endIndex < widthInFrames * heightInFrames;
        for (int frameIndex = startIndex; frameIndex < endIndex; frameIndex++) {
            row = frameIndex / widthInFrames;
            col = frameIndex % widthInFrames;
            frames[framesAdded++] = sheetArray[row][col];
        }
        animDict.put(name, new Animation<>(duration / framesAdded, frames));
    }

    public void loadAnim(String name, float duration, int... frameIndices) {
        int framesAdded = 0, row, col;
        TextureRegion[] frames = new TextureRegion[frameIndices.length];
        for (int frameIndex : frameIndices) {
            System.out.println(frameIndex);
            row = frameIndex / widthInFrames;
            col = frameIndex % widthInFrames;
            frames[framesAdded++] = sheetArray[row][col];
        }
        animDict.put(name, new Animation<>(duration / framesAdded, frames));
    }

    public TextureRegion getFrame(String animName, float time) {
        return animDict.get(animName).getKeyFrame(time, true);
    }

    public float getDuration(String animName) {
        return animDict.get(animName).getAnimationDuration();
    }

    public void dispose() {
        sheet.dispose();
    }
}
