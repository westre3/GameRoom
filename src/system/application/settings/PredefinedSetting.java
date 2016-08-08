package system.application.settings;

import system.application.OnLaunchAction;
import system.os.PowerMode;
import ui.Main;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import static system.application.settings.SettingValue.*;

/**
 * Created by LM on 08/08/2016.
 */
public enum PredefinedSetting {
    LOCALE("locale", new SettingValue(Locale.getDefault(),Locale.class,CATEGORY_GENERAL))
    ,TILE_ZOOM("tileZoom", new SettingValue(0.45, Double.class, CATEGORY_NONE))
    ,ON_GAME_LAUNCH_ACTION("onGameLaunchAction", new SettingValue<OnLaunchAction>(OnLaunchAction.DO_NOTHING,OnLaunchAction.class,CATEGORY_ON_GAME_START))
    ,FULL_SCREEN("fullScreen", new SettingValue(false,Boolean.class,CATEGORY_NONE))
    ,WINDOW_WIDTH("windowWidth", new SettingValue(1366,Integer.class,CATEGORY_NONE))
    ,WINDOW_HEIGHT("windowHeight", new SettingValue(768,Integer.class,CATEGORY_NONE))
    ,GAMING_POWER_MODE("gamingPowerMode", new SettingValue(PowerMode.getActivePowerMode(),PowerMode.class,CATEGORY_ON_GAME_START))
    ,ENABLE_GAMING_POWER_MODE("enableGamingPowerMode", new SettingValue(false,Boolean.class,CATEGORY_NONE))
    , NO_NOTIFICATIONS("noNotifications", new SettingValue(false,Boolean.class,CATEGORY_NONE))
    ,START_MINIMIZED("startMinimized", new SettingValue(false,Boolean.class,CATEGORY_NONE))
    ,NO_MORE_ICON_TRAY_WARNING("noMoreIconTrayWarning", new SettingValue(false,Boolean.class,CATEGORY_NONE))
    ,ENABLE_XBOX_CONTROLLER_SUPPORT("enableXboxControllerSupport", new SettingValue(false,Boolean.class,CATEGORY_NONE))
    ,GAMES_FOLDER("gamesFolder", new SettingValue(null,File.class,CATEGORY_GENERAL))
    ,DONATION_KEY("donationKey", new SettingValue("",String.class,CATEGORY_GENERAL))
    ,DISABLE_MAINSCENE_WALLPAPER("disableMainSceneWallpaper", new SettingValue(false,Boolean.class,CATEGORY_NONE))
    ;


    private String key;
    private SettingValue defaultValue;

    PredefinedSetting(String key, SettingValue setting){
        this.key = key;
        this.defaultValue = setting;
    }

    public String getLabel(){
        String label = Main.SETTINGS_BUNDLE.getString(key.toString()+"_label");
        return label!=null ? label : key.toString();
    }

    public String getTooltip(){
        String label = Main.SETTINGS_BUNDLE.getString(key.toString()+"_tooltip");
        return label!=null ? label : null;
    }

    public String getKey() {
        return key;
    }

    public void setDefaultValue(SettingValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public SettingValue getDefaultValue() {
        return defaultValue;
    }
    public boolean isClass(Class c){
        return c.equals(defaultValue.getValueClass());
    }

    @Override
    public String toString(){
        return key;
    }
}