package com.gameroom.ui.dialog;

import com.gameroom.data.game.entry.Platform;
import com.gameroom.ui.UIValues;
import com.gameroom.ui.scene.SettingsScene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Window;
import com.gameroom.ui.Main;
import com.gameroom.ui.control.specific.SearchBar;
import com.gameroom.ui.pane.platform.PlatformSettingsPane;

import javax.swing.*;

import static com.gameroom.system.application.settings.GeneralSettings.settings;
import static com.gameroom.ui.Main.SCREEN_HEIGHT;
import static com.gameroom.ui.Main.SCREEN_WIDTH;
import static com.gameroom.ui.Main.SUPPORTER_MODE;

/**
 * @author LM. Garret (admin@gameroom.me)
 * @date 09/06/2017.
 */
public class EmulationDialog extends GameRoomDialog<ButtonType> {
    private PlatformSettingsPane currentCenterPane;

    public EmulationDialog() {
        this(null);
    }

    public EmulationDialog(Platform focusedPlatform){
        this(focusedPlatform,null);
    }

    public EmulationDialog(Platform focusedPlatform, ButtonType buttonType) {
        super();
        mainPane.getStyleClass().add("container");
        ButtonType okButton = new ButtonType(Main.getString("close"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(okButton);

        if(buttonType != null){
            getDialogPane().getButtonTypes().add(buttonType);
        }

        mainPane.setCenter(new EmulationPane(focusedPlatform,getOwner()));
    }

    public static class EmulationPane extends StackPane{
        private BorderPane borderPane;
        private PlatformSettingsPane currentCenterPane;

        public EmulationPane(Window window){
            this(null,window);
        }
        public EmulationPane(Platform focusedPlatform, Window window){
            super();
            getStyleClass().add("container");
            borderPane = new BorderPane();
            borderPane.setLeft(createLeftPane(focusedPlatform,window));
            getChildren().add(borderPane);
            if(!SUPPORTER_MODE){
                getChildren().add(createNonSupporterPane());
            }
        }

        private Node createNonSupporterPane(){
            Label label = new Label(Main.getString("sorry_supporters_only"));

            Button button = new Button(Main.getString("more_infos"));
            button.setOnAction(event -> SettingsScene.checkAndDisplayRegisterDialog());

            VBox vbox = new VBox(UIValues.Constants.offsetXSmall());
            vbox.getChildren().addAll(label,button);
            vbox.setAlignment(Pos.CENTER);

            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(vbox);
            StackPane.setAlignment(vbox, Pos.CENTER);
            StackPane.setAlignment(label, Pos.CENTER);
            StackPane.setAlignment(button, Pos.CENTER);
            stackPane.setStyle("-fx-background-color: -dark; -fx-opacity: 0.9;");
            return stackPane;
        }

        private Node createLeftPane(Platform focusedPlatform, Window window) {

            ListView<Platform> listView = new ListView<Platform>();
            ObservableList<Platform> items = FXCollections.observableArrayList (Platform.getEmulablePlatforms());
            listView.setItems(items);

            listView.setCellFactory(param -> new ListCell<Platform>() {
                private ImageView imageView = new ImageView();
                @Override
                public void updateItem(Platform platform, boolean empty) {
                    super.updateItem(platform, empty);
                    if (empty || platform == null) {
                        imageView.setId("");
                        setText(null);
                        setGraphic(null);
                    } else {
                        double width = 30*Main.SCREEN_WIDTH/1920;
                        double height =  30*Main.SCREEN_HEIGHT/1080;

                        platform.setCSSIcon(imageView,settings().getTheme().useDarkPlatformIconsInList());
                        imageView.setFitWidth(width);
                        imageView.setFitHeight(height);
                        imageView.setSmooth(true);

                        setText(platform.getName());
                        setGraphic(imageView);
                    }
                }
            });
            listView.setEditable(false);
            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue != null) {
                    currentCenterPane = new PlatformSettingsPane(newValue,window);
                    currentCenterPane.setMaxWidth(3 * settings().getWindowWidth() / 5.0);
                    currentCenterPane.setPrefWidth(2.5 * settings().getWindowWidth() / 5.0);
                    currentCenterPane.setPadding(new Insets(10 * Main.SCREEN_WIDTH / 1920,
                            20 * Main.SCREEN_HEIGHT / 1080,
                            10 * Main.SCREEN_WIDTH / 1920,
                            20 * Main.SCREEN_HEIGHT / 1080
                    ));
                    borderPane.setCenter(currentCenterPane);
                }
            });
            listView.getSelectionModel().select(focusedPlatform == null ? 0 : items.indexOf(focusedPlatform));
            listView.setPrefWidth(1.2 * settings().getWindowWidth() / 5.0);
            listView.setPrefHeight(2.5 * settings().getWindowHeight() / 5.0);

            SearchBar bar = new SearchBar((observable, oldValue, newValue) -> {
                listView.setItems(
                        items.filtered(platform -> platform.getName().trim().toLowerCase().contains(newValue.trim().toLowerCase()))
                );
                listView.refresh();
            });
            bar.setId("search-bar-embedded");
            bar.prefWidthProperty().bind(listView.widthProperty());
            bar.setPadding(new Insets(10*SCREEN_HEIGHT/1080,0*SCREEN_WIDTH/1920,10*SCREEN_HEIGHT/1080,0*SCREEN_WIDTH/1920));


            VBox box = new VBox();
            box.setSpacing(5*Main.SCREEN_HEIGHT/1080);
            box.getChildren().addAll(bar,listView);
            VBox.setVgrow(listView, Priority.ALWAYS);
            VBox.setVgrow(bar, Priority.NEVER);
            box.setPadding(new Insets(10 * Main.SCREEN_WIDTH / 1920,
                    0 * Main.SCREEN_HEIGHT / 1080,
                    10 * Main.SCREEN_WIDTH / 1920,
                    20 * Main.SCREEN_HEIGHT / 1080
            ));
            return box;
        }
    }
}
