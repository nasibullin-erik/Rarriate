package ru.itis.view;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.itis.RarriateApplication;
import ru.itis.entities.Map;
import ru.itis.entities.World;
import ru.itis.entities.blocks.AbstractBlock;
import ru.itis.entities.blocks.implBlocks.*;
import ru.itis.entities.blocks.implBlocks.DirtBlock;
import ru.itis.entities.blocks.implBlocks.StoneBlock;
import ru.itis.entities.items.AbstractItem;
import ru.itis.entities.items.implItems.DirtBlockItem;
import ru.itis.entities.items.implItems.GrassBlockItem;
import ru.itis.entities.items.implItems.StoneBlockItem;
import ru.itis.entities.player.AbstractPlayer;
import ru.itis.entities.player.implPlayers.Player;
import ru.itis.exceptions.ClientException;
import ru.itis.network.dto.BlockDto;
import ru.itis.utils.FileLoader;
import ru.itis.utils.MediaLoader;
import ru.itis.utils.PropertiesLoader;
import ru.itis.utils.TextureLoader;
import ru.itis.view.components.ModernButton;
import ru.itis.view.components.ModernText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Game {

    protected static final int SPEED = 10;
    protected static final int FALLING_SPEED = SPEED /2;
    protected static final int MESSAGE_COUNT = 5;

    protected Stage mainStage;
    protected Scene mainScene;
    protected Pane mainPane;
    protected ModernText chat;
    protected Pane inventoryPane;

    protected AbstractPlayer player;
    protected List<AbstractPlayer> players;
    protected ImageView inventorySprite;
    protected List<AbstractBlock> abstractBlocks;

    protected ViewManager viewManager;

    protected MediaPlayer mediaPlayer;

    protected World world;

    protected String[] messages;
    protected int fillMessages;

    protected Integer port;

    protected AnimationTimer timer;

    protected boolean up;
    protected boolean down;
    protected boolean left;
    protected boolean right;

    //SinglePlayer game
    public Game(Stage stage, ViewManager viewManager){
        log.info("Создается одиночная игра");
        createGUI(stage, viewManager);
        log.info("Одиночная игра создалась");
    }

    //MultiPlayer game
    public Game(Stage stage, ViewManager viewManager, World world, AbstractPlayer player, int port) {
        log.info("Создается многопользовательская игра");
        this.player = player;
        this.world = world;
        this.players = world.getPlayers();
        this.port = port;
        createGUI(stage, viewManager);
        log.info("Многопользовательская игра создалась");
    }

    protected void createGUI(Stage stage, ViewManager viewManager) {
        if (players == null) {
            players = new ArrayList<>();
        }
        mainStage = stage;
        this.viewManager = viewManager;
        mainPane = new Pane();
        mainScene = new Scene(mainPane, mainStage.getWidth(), mainStage.getHeight());

        mainScene.setOnKeyPressed(e -> processKey(e.getCode(), true));
        mainScene.setOnKeyReleased(e -> processKey(e.getCode(), false));

        setBackground();
        generateLevel();
        createPlayer();
        createInventory();
        playGameBackgroundMusic();
        setChat();

        addListeners();

        addPlayers(players);
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();
    }

    protected void addListeners() {
        addMainSceneClickListener();
        addEscKeyListener();
        log.info("Слушатели установились");
//        addMovingKeyListener();
    }

//    protected void addMovingKeyListener() {
//        mainScene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent event) {
//                switch (event.getCode()) {
//                    case A:
//                        if (player.getTranslateX() > 0) {
//                            player.moveX(-SPEED);
//                        }
//                        break;
//                    case W:
//                        if (player.getTranslateY() >= SPEED) {
//                            jumpPlayer(player);
//                        }
//                        break;
//                    case D:
//                        if (player.getTranslateX() + player.getWidth() <= mainScene.getWidth()) {
//                            player.moveX(SPEED);
//                        }
//                        break;
//                }
//
//            }
//        });
//    }

    protected void addEscKeyListener() {
        Pane escapePane = new Pane();
        escapePane.setBackground(new Background(FileLoader.getEscapeBackground()));
        escapePane.setPrefSize(mainStage.getWidth(),mainStage.getHeight());
        ModernButton exit = createExitToMainMenuButton();
        exit.setTranslateX((mainStage.getWidth()-200)/2);
        exit.setTranslateY((mainStage.getHeight()-60)/2);
        escapePane.getChildren().add(exit);
        mainScene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    if (mainPane.getChildren().contains(escapePane)) {
                        mainPane.getChildren().remove(escapePane);
                    } else {
                        mainPane.getChildren().add(escapePane);
                    }
                }
            }
        });
    }

    protected void addPlayers(List<AbstractPlayer> players) {
        for (AbstractPlayer player: players) {
            if (!player.getName().equals(this.player.getName())) {
                mainPane.getChildren().add(player);
            }
        }
        log.info("Игроки установились");
    }

    protected void addMainSceneClickListener() {
        mainScene.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean isBlock;
                for (AbstractBlock abstractBlock : abstractBlocks) {
                    if (Math.abs(player.getTranslateX() - abstractBlock.getTranslateX()) <= 150 &&
                            Math.abs(player.getTranslateY() - abstractBlock.getTranslateY()) <= 150) {
                        if (abstractBlock.getBoundsInParent().intersects(event.getX(), event.getY(), 1, 1)) {
                            if (abstractBlock.isBreakable()) {
                                removeBlockAndAddToInventory(abstractBlock);
                            }
                            return;
                        }
                    }
                }
                if (Math.abs(player.getTranslateX() - event.getX()) <= 150 &&
                        Math.abs(player.getTranslateY() - event.getY()) <= 150) {
                    setBlockFromInventory(event.getX(), event.getY());
                }
            }
        });
    }

    protected void update() {
        if (up && player.getTranslateY() >= SPEED) {
            jumpPlayer(player);
        }
        if (left && player.getTranslateX() > 0) {
            movePlayerX(-SPEED, player);
        }

        if (right && player.getTranslateX() + player.getWidth() <= mainScene.getWidth()) {
            movePlayerX(SPEED, player);
        }

        if (player.getVelocity().getY() < 10) {
            player.setVelocity(player.getVelocity().add(0, 1));
        }

        movePlayerY((int)player.getVelocity().getY(), player);
//        System.out.println("Main player: " + player.getName() + " " + player.getTranslateX() + " " + player.getTranslateY());
    }


    //1 - addNewPlayer, 2 - movePlayer, ....
    public void updatePlayer(int type, String name, double x, double y) {
        AbstractPlayer player = null;
        switch (type) {
            case 1:
                addNewPlayer(name);
                break;
            case 2:
                for (AbstractPlayer abstractPlayer : players) {
                    if (abstractPlayer.getName().equals(name) && !abstractPlayer.getName().equals(this.player.getName())) {
                        abstractPlayer.setTranslateX(x);
                        abstractPlayer.setTranslateY(y);
//                        System.out.println("Other players: " + abstractPlayer.getName() + " " + abstractPlayer.getTranslateX() + " " + abstractPlayer.getTranslateY());
                    }
                }
                break;
            default:
                break;
        }
    }

    //1 -add, 2-delete
    public void updateBlocks(int type, int id, double x, double y) {
        switch (type) {
            case 1:
                setBlock(id, x, y);
                break;
            case 2:
                removeBlock(x,y);
                break;
            default:
                break;
        }
    }

    protected void addNewPlayer(String name) {
        AbstractPlayer newPlayer = new Player(name);
        players.add(newPlayer);
        createPlayer(newPlayer);
        //log.info("Пользователь " + name + " подключился");
    }

    protected void createPlayer() {
        if (player == null) {
            player = new Player();
        }
        if ((world.getPlayers() != null) && (!world.getPlayers().contains(player))) {
            world.getPlayers().add(player);
        }
        player.setTranslateX((mainScene.getWidth() - player.getWidth())/2);
        player.setTranslateY((mainScene.getHeight() - player.getHeight())/2);
        mainPane.getChildren().add(player);
        log.info("Игрок " + player.getName() + " добавлен");
    }

    protected void createPlayer(AbstractPlayer player) {
        player.setTranslateX((mainScene.getWidth() - player.getWidth())/2);
        player.setTranslateY((mainScene.getHeight() - player.getHeight())/2);
        mainPane.getChildren().add(player);
        addChatMessage(player.getName() + " has been connected");
        //log.info("Пользователь " + player.getName() + " подключился");
    }

    public void deletePlayer(String name) {
        for (AbstractPlayer player: players) {
            if (player.getName().equals(name)) {
                mainPane.getChildren().remove(player);
                players.remove(player);
                addChatMessage(player.getName() + " has been disconnected");
                log.info("Пользователь " + player.getName() + " отключился");
                return;
            }
        }
    }

    protected void jumpPlayer(AbstractPlayer player) {
        if (player.isCanJump()) {
            player.setVelocity(player.getVelocity().add(0, -30));
            player.setCanJump(false);
        }
    }

    protected void movePlayerX(int value, AbstractPlayer player) {
        boolean movingRight = value > 0;

        for (int i = 0; i < Math.abs(value); i++) {
            for (AbstractBlock abstractBlock : abstractBlocks) {
                if (player.getBoundsInParent().intersects(abstractBlock.getBoundsInParent())) {
                    if (movingRight) {
                        if (player.getTranslateX() + player.getWidth() == abstractBlock.getTranslateX()) {
                            return;
                        }
                    }
                    else {
                        if (player.getTranslateX() == abstractBlock.getTranslateX() + abstractBlock.getWidth()) {
                            return;
                        }
                    }
                }
            }
            player.moveX(movingRight ? 1 : -1);
            if (RarriateApplication.getClient() != null) {
                try {
                    RarriateApplication.getClient().sendUDPFrame(
                            RarriateApplication.getClient().getUdpFrameFactory().createUDPFrame(0,
                                    RarriateApplication.getClient().getClientUuid(), player.getTranslateX(), player.getTranslateY())
                    );
                } catch (ClientException e) {
                    viewManager.setMainMenuWithInfoScene(e.getMessage());
                    System.err.println(e.getMessage());
                }
            }
            }

    }

    protected void movePlayerY(int value, AbstractPlayer player) {
        boolean movingDown = value > 0;
        for (int i = 0; i < Math.abs(value); i++) {
            for (AbstractBlock abstractBlock : abstractBlocks) {
                if (player.getBoundsInParent().intersects(abstractBlock.getBoundsInParent())) {
                    if (movingDown) {
                        if (player.getTranslateY() + player.getHeight() == abstractBlock.getTranslateY()) {
                            player.moveY(- 1);
                            player.setCanJump(true);
                            return;
                        }
                    }
                    else {
                        if (player.getTranslateY() == abstractBlock.getTranslateY() + abstractBlock.getHeight()) {
                            return;
                        }
                    }
                }
            }
            player.moveY(movingDown ? 1 : -1);
            if (RarriateApplication.getClient() != null) {
                try {
                    RarriateApplication.getClient().sendUDPFrame(
                            RarriateApplication.getClient().getUdpFrameFactory().createUDPFrame(0,
                                    RarriateApplication.getClient().getClientUuid(), player.getTranslateX(), player.getTranslateY())
                    );
                } catch (ClientException e) {
                    viewManager.setMainMenuWithInfoScene(e.getMessage());
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    protected void processKey(KeyCode code, boolean on) {
        switch (code) {
            case A:
                left = on ;
                player.setAnimation(AbstractPlayer.RUN_LEFT);
                if (!on) {
                    player.setAnimation(AbstractPlayer.IDLE);
                }
                break ;
            case D:
                right = on;
                player.setAnimation(AbstractPlayer.RUN_RIGHT);
                if (!on) {
                    player.setAnimation(AbstractPlayer.IDLE);
                }
                break;
            case W:
                up = on;
                break;
            case S:
                down = on;
                break;
        }
    }

    public Scene getGameScene() {
        return mainScene;
    }

    protected void generateLevel() {
        if (world == null) {
            world = new World(new Map(mainScene.getHeight()), null);
        }
        abstractBlocks = world.getMap().getAbstractBlocks();
        for (AbstractBlock abstractBlock : abstractBlocks) {
            setBlock(abstractBlock);
        }
        log.info("Мир инициализирован");
    }

    protected void removeBlock(double x, double y) {
        for (AbstractBlock abstractBlock : abstractBlocks) {
            if (abstractBlock.getBoundsInParent().intersects(x+1,y,1,1)) {
                abstractBlocks.remove(abstractBlock);
                mainPane.getChildren().remove(abstractBlock);
                break;
            }
        }
    }

    protected void removeBlockAndAddToInventory(AbstractBlock abstractBlock) {
        player.getInventory().addItem(getItemFromBlock(abstractBlock));
        mainPane.getChildren().remove(abstractBlock);
        abstractBlocks.remove(abstractBlock);
        updateInventory();
        if (RarriateApplication.getClient() != null) {
            try {
                RarriateApplication.getClient().sendTCPFrame(
                        RarriateApplication.getClient().getTcpFrameFactory().createTCPFrame(5,
                                UUID.randomUUID(), BlockDto.from(abstractBlock))
                );
            } catch (ClientException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    protected void setBlockFromInventory(double x, double y) {
        if (player.getInventory().getItems().size() > 0) {
            AbstractBlock abstractBlock = getBlockFromItem(player.getInventory().getItems().get(0));
            player.getInventory().getItems().remove(0);
            abstractBlock.setTranslateX(x - (x % AbstractBlock.WIDTH));
            abstractBlock.setTranslateY(y - (y % AbstractBlock.HEIGHT));
            abstractBlocks.add(abstractBlock);
            setBlock(abstractBlock);
            updateInventory();
            if (RarriateApplication.getClient() != null) {
                try {
                    RarriateApplication.getClient().sendTCPFrame(
                            RarriateApplication.getClient().getTcpFrameFactory().createTCPFrame(7,
                                    UUID.randomUUID(), BlockDto.from(abstractBlock))
                    );
                } catch (ClientException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    protected void createInventory() {
        inventorySprite = new ImageView(TextureLoader.getInventoryImage());
        inventorySprite.setFitHeight(100);
        inventorySprite.setFitWidth(800);
        inventorySprite.setTranslateX(10);
        inventorySprite.setTranslateY(10);
        mainPane.getChildren().add(inventorySprite);

        inventoryPane = new Pane();
        mainPane.getChildren().add(inventoryPane);
        log.info("Инвентарь инициализирован");
    }

    protected void updateInventory() {
        List<AbstractItem> items = player.getInventory().getItems();
        AbstractItem item;

        mainPane.getChildren().remove(inventoryPane);
        inventoryPane = new Pane();
        inventoryPane.setTranslateX(inventorySprite.getTranslateX() + (double) AbstractItem.WIDTH/2);
        inventoryPane.setTranslateY(inventorySprite.getTranslateY() + (double) AbstractItem.HEIGHT/2);

        double offsetX = 0;
        for (int i = 0; i < items.size() && i < 8; i++) {
            item = items.get(i);
            item.setTranslateX(offsetX/2);
            item.setTranslateY((inventorySprite.getTranslateY()-10)/2);
            inventoryPane.getChildren().add(item);
            offsetX += AbstractItem.WIDTH * 4;
        }
        mainPane.getChildren().add(inventoryPane);
    }

    protected AbstractItem getItemFromBlock(AbstractBlock abstractBlock) {
        switch (abstractBlock.getBlockId()) {
            case 1:
                return new StoneBlockItem();
            case 2:
                return new DirtBlockItem();
            case 3:
                return new GrassBlockItem();
            default:
                return null;
        }
    }

    protected AbstractBlock getBlockFromItem(AbstractItem item) {
        switch (item.getItemId()) {
            case 1:
                return new StoneBlock();
            case 2:
                return new DirtBlock();
            case 3:
                return new GrassBlock();
            default:
                return null;
        }
    }

    public void exitToMainMenuWithInfo(String text) {
        viewManager.setMainMenuWithInfoScene(text);
        log.info("Произошла ошибка по причине " + text);
    }

    protected ModernButton createExitToMainMenuButton() {
        ModernButton exit = new ModernButton("EXIT");
        exit.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                timer.stop();
                RarriateApplication.disconnect();
                port = null;
                exitToMainMenu();
            }
        });
        return exit;
    }

    protected void setBlock(AbstractBlock abstractBlock) {
        mainPane.getChildren().add(abstractBlock);
    }

    protected void setBlock(int id, double x, double y) {
        AbstractBlock abstractBlock = null;

        switch (id) {
            case 1:
                abstractBlock = new StoneBlock();
                break;
            case 2:
                abstractBlock = new DirtBlock();
                break;
            case 3:
                abstractBlock = new GrassBlock();
                break;
            case 4:
                abstractBlock = new BedrockBlock();
                break;
            default:
                return;
        }
        abstractBlock.setTranslateX(x);
        abstractBlock.setTranslateY(y);
        abstractBlocks.add(abstractBlock);
        mainPane.getChildren().add(abstractBlock);

    }

    protected void setBackground(){
        mainPane.setBackground(new Background(FileLoader.getGameBackground()));
    }

    protected void exitToMainMenu() {
        stopPlayingBackgroundMusic();
        viewManager.setMainMenuScene();
        log.info("Игрок " + player.getName() + " отключился");
    }

    protected void playGameBackgroundMusic() {
        mediaPlayer = MediaLoader.getGameBackgroundMusic();
        mediaPlayer.setVolume(Integer.parseInt(PropertiesLoader.getInstance().getProperty("MUSIC_VOLUME")));
        mediaPlayer.play();
    }

    protected void setChat() {
        messages = new String[MESSAGE_COUNT];
        chat = new ModernText();
        chat.setTranslateX(20);
        chat.setTranslateY(mainScene.getHeight() - 200);
        chat.setFill(Color.WHITE);
        mainPane.getChildren().add(chat);

        if (RarriateApplication.getClient() != null) {
            addChatMessage("Port: " + port);
        }
    }

    public void addChatMessage(String message) {
        if (fillMessages < messages.length) {
            chat.setText("");
            messages[fillMessages] = message;
            fillMessages++;
            for (int j = 0; j < fillMessages; j++) {
                chat.setText(chat.getText() + messages[j] + "\n");
            }
        } else {
            chat.setText("");
            messages = Arrays.copyOf(Arrays.copyOfRange(messages, 1, messages.length), messages.length);
            messages[messages.length-1] = message;
            for (String string : messages) {
                chat.setText(chat.getText() + string + "\n");
            }
        }
    }

    protected void stopPlayingBackgroundMusic() {
        mediaPlayer.stop();
    }
}
