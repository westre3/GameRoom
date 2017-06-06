package data.game.entry;

import data.io.DataBase;
import ui.Main;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by LM on 11/02/2017.
 */
public class Platform {
    public final static int STEAM_ID = 1;
    public final static int STEAM_ONLINE_ID = 2;
    public final static int ORIGIN_ID = 3;
    public final static int UPLAY_ID = 4;
    public final static int BATTLENET_ID = 5;
    public final static int GOG_ID = 6;

    private final static HashMap<Integer, Platform> ID_MAP = new HashMap<>();

    public final static int DEFAULT_ID = -1;
    public final static int NONE_ID = -2;

    public final static Platform NONE = new Platform(NONE_ID, NONE_ID, "default");


    private int igdb_id = DEFAULT_ID;
    private int id = DEFAULT_ID;

    private String nameKey;


    private Platform(int id, int igdb_id, String nameKey) {
        if (nameKey == null || nameKey.isEmpty()) {
            throw new IllegalArgumentException("Platform's nameKey was either null or empty : \"" + nameKey + "\"");
        }
        this.igdb_id = igdb_id;
        this.nameKey = nameKey;
        this.id = id;

        if (id != NONE_ID) {
            insertInDB();
        } else {
            ID_MAP.put(id, this);
        }
    }

    public Platform(int igdb_id, String nameKey) {
        this(DEFAULT_ID, igdb_id, nameKey);
    }

    public Platform(String nameKey) {
        this(DEFAULT_ID, DEFAULT_ID, nameKey);
    }

    public int insertInDB() {
        try {
            id = getIdInDb();
            String sql = "UPDATE Platform set name_key=?" + (igdb_id < 0 ? "" : ", igdb_id=?") + " where id=?" + id;
            PreparedStatement platformStatement = DataBase.getUserConnection().prepareStatement(sql);
            platformStatement.setString(1, nameKey);
            if (igdb_id >= 0) {
                platformStatement.setInt(2, igdb_id);
            }
            platformStatement.execute();
            platformStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean isInDB() {
        return getIdInDb() != DEFAULT_ID;
    }

    private int getIdInDb() {
        if (nameKey == null || nameKey.isEmpty()) {
            throw new IllegalArgumentException("Platform's nameKey was either null or empty : \"" + nameKey + "\"");
        }
        int id = DEFAULT_ID;
        try {
            Connection connection = DataBase.getUserConnection();
            PreparedStatement getIdQuery = connection.prepareStatement("SELECT id FROM Platform WHERE name_key = ?");
            getIdQuery.setString(1, nameKey);
            ResultSet result = getIdQuery.executeQuery();

            if (result.next()) {
                id = result.getInt(1);
                result.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }

    public static Platform getFromIGDBId(int igdb_id) {
        if (ID_MAP.isEmpty()) {
            try {
                initWithDb();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        for (Platform platform : ID_MAP.values()) {
            if (platform.getIGDBId() == igdb_id) {
                return platform;
            }
        }
        //try to see if it exists in db
        try {
            Connection connection = DataBase.getUserConnection();
            PreparedStatement statement = connection.prepareStatement("select * from Platform where igdb_id = ?");
            statement.setInt(1, igdb_id);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                int platformId = set.getInt("id");
                String key = set.getString("name_key");
                Platform newPlatform = new Platform(platformId, igdb_id, key);
                ID_MAP.put(platformId, newPlatform);

                return newPlatform;
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Platform getFromId(int id) {
        if (ID_MAP.isEmpty()) {
            try {
                initWithDb();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Platform platform = ID_MAP.get(id);

        if (platform == null) {
            //try to see if it exists in db
            try {
                Connection connection = DataBase.getUserConnection();
                PreparedStatement statement = connection.prepareStatement("select * from Platform where id = ?");
                statement.setInt(1, id);
                ResultSet set = statement.executeQuery();
                if (set.next()) {
                    int platformId = set.getInt("id");
                    int igdbId = set.getInt("igdb_id");
                    String key = set.getString("name_key");
                    Platform newPlatform = new Platform(platformId, igdbId, key);
                    ID_MAP.put(platformId, newPlatform);

                    return newPlatform;
                }
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return platform;
    }

    private static void initWithDb() throws SQLException {
        Connection connection = DataBase.getUserConnection();
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery("select * from Platform");
        while (set.next()) {
            int id = set.getInt("id");
            int igdbId = set.getInt("igdb_id");
            String key = set.getString("name_key");
            ID_MAP.put(id, new Platform(id, igdbId, key));
        }
        statement.close();
    }

    public int getIGDBId() {
        return igdb_id;
    }

    public void setIGDBId(int IGDBId) {
        this.igdb_id = IGDBId;
    }

    public String getName() {
        return Main.getString(nameKey);
    }

    public String getIconCSSId() {
        if(id == STEAM_ONLINE_ID){
            //TODO implement a cleaner way to have icons for this
            return "steam-icon";
        }
        return nameKey + "-icon";
    }

    public static Collection<Platform> values() {
        return ID_MAP.values();
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        if (nameKey == null) {
            return "-";
        }
        return nameKey;
    }
}
