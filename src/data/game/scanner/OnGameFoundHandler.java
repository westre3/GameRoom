package data.game.scanner;

import data.game.entry.GameEntry;
import ui.control.button.gamebutton.GameButton;

/**
 * Created by LM on 17/08/2016.
 */
public interface OnGameFoundHandler {

    GameButton gameToAddFound(GameEntry entry);

    void onAllGamesFound();
}
