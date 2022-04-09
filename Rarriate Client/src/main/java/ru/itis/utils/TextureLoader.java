package ru.itis.utils;

import javafx.scene.image.Image;
import ru.itis.exceptions.ImageFileNotFoundException;
import ru.itis.start.RarriateStart;

import java.util.Objects;

public class TextureLoader {
    public static Image getDirtTexture() {
        try {
            return new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/blocks/dirt_block.jpg")));
        } catch (NullPointerException e) {
            RarriateStart.showError(new ImageFileNotFoundException("Can't find dirt_block.jpg file", e));
        }
        return null;
    }

    public static Image getGrassTexture() {
        try {
            return new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/blocks/grass_block.jpg")));
        } catch (NullPointerException e) {
            RarriateStart.showError(new ImageFileNotFoundException("Can't find grass_block.jpg file", e));
        }
        return null;
    }

    public static Image getStoneTexture() {
        try {
            return new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/blocks/stone_block.png")));
        } catch (NullPointerException e) {
            RarriateStart.showError(new ImageFileNotFoundException("Can't find stone_block.png file", e));
        }
        return null;
    }

    public static Image[] getPlayer1SpriteRight() {
        try {
            Image[] images = new Image[3];
            images[0] = new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/players/Spider Man/Run Right/1.png")));
            images[1] = new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/players/Spider Man/Run Right/2.png")));
            images[2] = new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/players/Spider Man/Run Right/3.png")));
            return images;
        } catch (NullPointerException e) {
            RarriateStart.showError(new ImageFileNotFoundException("Can't find Spider Man/Run Right files", e));
        }
        return null;
    }

    public static Image[] getPlayer1SpriteLeft() {
        try {
            Image[] images = new Image[3];
            images[0] = new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/players/Spider Man/Run Left/1.png")));
            images[1] = new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/players/Spider Man/Run Left/2.png")));
            images[2] = new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/players/Spider Man/Run Left/3.png")));
            return images;
        } catch (NullPointerException e) {
            RarriateStart.showError(new ImageFileNotFoundException("Can't find Spider Man/Run Left files", e));
        }
        return null;
    }

    public static Image[] getPlayer1SpriteIdle() {
        try {
            Image[] images = new Image[1];
            images[0] = new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/players/Spider Man/Idle/1.png")));
            return images;
        } catch (NullPointerException e) {
            RarriateStart.showError(new ImageFileNotFoundException("Can't find Spider Man/Idle files", e));
        }
        return null;
    }

    public static Image getInventoryImage() {
        try {
            return new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/inventory.png")));
        } catch (NullPointerException e) {
            RarriateStart.showError(new ImageFileNotFoundException("Can't find inventory.png file", e));
        }
        return null;
    }

    public static Image getBedrockTexture() {
        try {
            return new Image(Objects.requireNonNull(TextureLoader.class
                    .getClassLoader()
                    .getResourceAsStream("img/blocks/bedrock_block.png")));
        } catch (NullPointerException e) {
            RarriateStart.showError(new ImageFileNotFoundException("Can't find bedrock_block.png file", e));
        }
        return null;
    }
}
