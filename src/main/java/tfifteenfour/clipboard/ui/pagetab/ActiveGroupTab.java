package tfifteenfour.clipboard.ui.pagetab;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import tfifteenfour.clipboard.ui.UiPart;

/**
 * A UI for the group tab.
 */
public class ActiveGroupTab extends UiPart<Region> {

    private static final String FXML = "GroupTab.fxml";

    @FXML
    private Label name;

    public ActiveGroupTab() {
        super(FXML);
    }

}
