import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.WString;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import system.application.settings.PredefinedSetting;
import system.device.ControllerButtonListener;
import system.device.XboxController;
import ui.Main;
import ui.dialog.ConsoleOutputDialog;
import ui.dialog.GameRoomAlert;
import ui.scene.MainScene;
import ui.scene.SettingsScene;

import java.awt.*;
import java.awt.MenuItem;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static ui.Main.*;

/**
 * Created by LM on 15/07/2016.
 */
public class Launcher extends Application {
    private int trayMessageCount = 0;
    private static ConsoleOutputDialog[] console = new ConsoleOutputDialog[1];

    public static void main(String[] args) throws URISyntaxException {
        setCurrentProcessExplicitAppUserModelID("GameRoom");

        System.setErr(new PrintStream(System.err){
            public void print(final String string) {
                //System.err.print(string);
                LOGGER.error(string);
                if(DEV_MODE) {
                    Platform.runLater(() -> {
                        if (console[0] == null) {
                            console[0] = new ConsoleOutputDialog();
                        }
                        console[0].appendLine(string);
                        console[0].showConsole();
                    });
                }
            }
        });
        System.setOut(new PrintStream(System.out){
            public void print(final String string) {
                //System.out.print(string);
                LOGGER.debug(string);
            }
        });
        System.out.println("\n\n==========================================NEW START============================================");

        Main.LOGGER.debug("Received args : ");
        for(String arg : args){
            Main.LOGGER.debug("\t\""+arg+"\"");
        }

        if (args.length > 0) {
            Main.DEV_MODE = args[0].equals("dev");
        }
        Main.main(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MAIN_SCENE = new MainScene(primaryStage);
        initIcons(primaryStage);
        initXboxController(primaryStage);

        primaryStage.setTitle("GameRoom");
        primaryStage.setScene(MAIN_SCENE);
        primaryStage.setFullScreen(GENERAL_SETTINGS.getBoolean(PredefinedSetting.FULL_SCREEN));

        if (GENERAL_SETTINGS.getBoolean(PredefinedSetting.START_MINIMIZED)) {
            primaryStage.setOpacity(0);
        }
        primaryStage.show();
        if (GENERAL_SETTINGS.getBoolean(PredefinedSetting.START_MINIMIZED)) {
            primaryStage.hide();
            primaryStage.setOpacity(1);
        }
        Platform.runLater(() -> {
            primaryStage.setWidth(primaryStage.getWidth());
            primaryStage.setHeight(primaryStage.getHeight());
        });
    }

    private void initXboxController(Stage primaryStage) {
        try {
            Robot r = new Robot();
            xboxController = new XboxController(new ControllerButtonListener() {
                @Override
                public void onButtonPressed(String buttonId) {
                    switch (buttonId) {
                        case XboxController.BUTTON_A:
                            r.keyPress(java.awt.event.KeyEvent.VK_ENTER);
                            break;
                        case XboxController.BUTTON_B:
                            r.keyPress(java.awt.event.KeyEvent.VK_ESCAPE);
                            break;
                        case XboxController.BUTTON_X:
                            r.keyPress(KeyEvent.VK_SPACE);
                            break;
                        case XboxController.BUTTON_Y:
                            r.keyPress(java.awt.event.KeyEvent.VK_I);
                            break;
                        case XboxController.BUTTON_DPAD_UP:
                            r.keyPress(java.awt.event.KeyEvent.VK_UP);
                            break;
                        case XboxController.BUTTON_DPAD_LEFt:
                            r.keyPress(java.awt.event.KeyEvent.VK_LEFT);
                            break;
                        case XboxController.BUTTON_DPAD_DOWN:
                            r.keyPress(java.awt.event.KeyEvent.VK_DOWN);
                            break;
                        case XboxController.BUTTON_DPAD_RIGHT:
                            r.keyPress(java.awt.event.KeyEvent.VK_RIGHT);
                            break;
                        default:
                            break;
                    }

                }

                @Override
                public void onButtonReleased(String buttonId) {

                }
            });
            if(Main.GENERAL_SETTINGS.getBoolean(PredefinedSetting.ENABLE_XBOX_CONTROLLER_SUPPORT)) {
                xboxController.startThreads();
            }

        } catch (AWTException e) {
            e.printStackTrace();
        }
        primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                MAIN_SCENE.setChangeBackgroundNextTime(true);
                if(newValue && Main.GENERAL_SETTINGS.getBoolean(PredefinedSetting.ENABLE_XBOX_CONTROLLER_SUPPORT)){
                   xboxController.startThreads();
                }else if(!newValue && Main.GENERAL_SETTINGS.getBoolean(PredefinedSetting.ENABLE_XBOX_CONTROLLER_SUPPORT)){
                    xboxController.stopThreads();
                }
            }
        });
    }

    @Override
    public void stop() {
        Main.LOGGER.info("Closing app, saving settings.");
        for (int i = 0; i < CACHE_FOLDER.listFiles().length; i++) {
            File temp = CACHE_FOLDER.listFiles()[i];
            temp.delete();
        }
        GENERAL_SETTINGS.saveSettings();

        System.exit(0);
    }

    private void initIcons(Stage stage) {

        for (int i = 32; i < 513; i *= 2) {
            stage.getIcons().add(new Image("res/ui/icon/icon" + i + ".png"));
        }

        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            Main.LOGGER.error("SystemTray not supported");
            return;
        }

        final PopupMenu popup = new PopupMenu();
        Image fxImage = new Image("res/ui/icon/icon16.png");
        TRAY_ICON = new TrayIcon(SwingFXUtils.fromFXImage(fxImage, null));
        TRAY_ICON.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    open(stage);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        TRAY_ICON.setImageAutoSize(true);
        Platform.setImplicitExit(DEV_MODE);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (event.getEventType().equals(WindowEvent.WINDOW_CLOSE_REQUEST)) {
                    if (!DEV_MODE) {
                        stage.hide();
                        if (trayMessageCount < 2 && !GENERAL_SETTINGS.getBoolean(PredefinedSetting.NO_MORE_ICON_TRAY_WARNING) && !GENERAL_SETTINGS.getBoolean(PredefinedSetting.NO_NOTIFICATIONS)) {
                            TRAY_ICON.displayMessage("GameRoom"
                                    , RESSOURCE_BUNDLE.getString("tray_icon_still_running_1")
                                            + RESSOURCE_BUNDLE.getString("always_in_background")
                                            + RESSOURCE_BUNDLE.getString("tray_icon_still_running_2"), TrayIcon.MessageType.INFO);
                            trayMessageCount++;
                        } else {
                            if (!GENERAL_SETTINGS.getBoolean(PredefinedSetting.NO_MORE_ICON_TRAY_WARNING)) {
                                GENERAL_SETTINGS.setSettingValue(PredefinedSetting.NO_MORE_ICON_TRAY_WARNING,true);
                            }
                        }
                    }
                }
            }
        });
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem openItem = new MenuItem(RESSOURCE_BUNDLE.getString("open"));
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open(stage);
            }
        });
        MenuItem gameRoomFolderItem = new MenuItem(RESSOURCE_BUNDLE.getString("gameroom_folder"));
        gameRoomFolderItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile());
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        MenuItem gamesFolderItem = new MenuItem(RESSOURCE_BUNDLE.getString("games_folder"));
        gamesFolderItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(GENERAL_SETTINGS.getFile(PredefinedSetting.GAMES_FOLDER));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        MenuItem settingsItem = new MenuItem(RESSOURCE_BUNDLE.getString("Settings"));
        settingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MAIN_SCENE.fadeTransitionTo(new SettingsScene(new StackPane(), stage, MAIN_SCENE), stage);
                open(stage);
            }
        });
        //CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
        //CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
        START_TRAY_MENU.setLabel(RESSOURCE_BUNDLE.getString("start"));
        MenuItem exitItem = new MenuItem(RESSOURCE_BUNDLE.getString("exit"));

        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                forceStop(stage);
            }
        });

        //Add components to pop-up menu
        popup.add(openItem);
        popup.add(START_TRAY_MENU);
        popup.addSeparator();
        popup.add(gameRoomFolderItem);

        if(GENERAL_SETTINGS.getFile(PredefinedSetting.GAMES_FOLDER) != null && GENERAL_SETTINGS.getFile(PredefinedSetting.GAMES_FOLDER).isDirectory()){
            popup.add(gamesFolderItem);
        }
        popup.add(settingsItem);
        popup.addSeparator();
        popup.add(exitItem);

        TRAY_ICON.setPopupMenu(popup);

        try {
            tray.add(TRAY_ICON);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    public static void setCurrentProcessExplicitAppUserModelID(final String appID) {
        if (SetCurrentProcessExplicitAppUserModelID(new WString(appID)).longValue() != 0)
            throw new RuntimeException("unable to set current process explicit AppUserModelID to: " + appID);
    }

    private static native NativeLong SetCurrentProcessExplicitAppUserModelID(WString appID);

    static {
        Native.register("shell32");
    }

}
