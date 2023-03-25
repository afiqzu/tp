package tfifteenfour.clipboard.ui;

import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tfifteenfour.clipboard.commons.core.GuiSettings;
import tfifteenfour.clipboard.commons.core.LogsCenter;
import tfifteenfour.clipboard.logic.CurrentSelection;
import tfifteenfour.clipboard.logic.Logic;
import tfifteenfour.clipboard.logic.PageType;
import tfifteenfour.clipboard.logic.commands.BackCommand;
import tfifteenfour.clipboard.logic.commands.CommandResult;
import tfifteenfour.clipboard.logic.commands.ExitCommand;
import tfifteenfour.clipboard.logic.commands.HelpCommand;
import tfifteenfour.clipboard.logic.commands.HomeCommand;
import tfifteenfour.clipboard.logic.commands.SelectCommand;
import tfifteenfour.clipboard.logic.commands.exceptions.CommandException;
import tfifteenfour.clipboard.logic.parser.exceptions.ParseException;
import tfifteenfour.clipboard.model.course.Course;
import tfifteenfour.clipboard.model.course.Group;
import tfifteenfour.clipboard.ui.pagetab.ActiveGroupTab;
import tfifteenfour.clipboard.ui.pagetab.ActiveModuleTab;
import tfifteenfour.clipboard.ui.pagetab.ActiveStudentTab;
import tfifteenfour.clipboard.ui.pagetab.InactiveGroupTab;
import tfifteenfour.clipboard.ui.pagetab.InactiveModuleTab;
import tfifteenfour.clipboard.ui.pagetab.InactiveStudentTab;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private CourseListPanel courseListPanel;
    private StudentViewCard studentViewCard;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane leftPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private HBox studentPanelPlaceholder;

    @FXML
    private StackPane rightPanelPlaceholder;

    @FXML
    private VBox moduleTabPlaceholder;

    @FXML
    private VBox groupTabPlaceholder;

    @FXML
    private VBox studentTabPlaceholder;


    /**
     * Creates a {@code MainWindow} with the given {@code Stage} and {@code Logic}.
     */
    public MainWindow(Stage primaryStage, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        courseListPanel = new CourseListPanel(logic.getRoster().getUnmodifiableCourseList());
        leftPanelPlaceholder.getChildren().add(courseListPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(logic.getRosterFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        moduleTabPlaceholder.getChildren().add(new ActiveModuleTab().getRoot());
        groupTabPlaceholder.getChildren().add(new InactiveGroupTab().getRoot());
        studentTabPlaceholder.getChildren().add(new InactiveStudentTab().getRoot());
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    /**
     * Navigates GUI back to main course page.
     */
    private void handleHome() {
        showCoursePane();
        showModuleTab();
        closeViewPane();
        closeGroupTab();
        closeStudentTab();
        logic.getCurrentSelection().navigateBackToCoursePage();
    }

    /**
     * Displays currently viewed student in right pane.
     */
    public void refreshViewPane() {
        studentViewCard = new StudentViewCard(logic.getCurrentSelection().getSelectedStudent());
        rightPanelPlaceholder.getChildren().add(studentViewCard.getRoot());
    }

    /**
     * Closes viewed student
     */
    public void closeViewPane() {
        rightPanelPlaceholder.getChildren().clear();
        logic.getCurrentSelection().emptySelectedStudent();
    }

    //@FXML
    //private void handleUndo() {
    //    studentListPanel.setPersonListView(logic.getUnmodifiableFilteredStudentList());
    //}

    /**
     * Shows course pane.
     */
    private void showCoursePane() {
        courseListPanel = new CourseListPanel(logic.getRoster().getUnmodifiableCourseList());
        leftPanelPlaceholder.getChildren().add(courseListPanel.getRoot());
    }

    /**
     * Show group pane.
     * @param course that groups belong to.
     */
    private void showGroupPane(Course course) {
        GroupListPanel groupListPanel = new GroupListPanel(course.getUnmodifiableGroupList());
        leftPanelPlaceholder.getChildren().add(groupListPanel.getRoot());
    }

    /**
     * Show student pane.
     * @param group that students belong to.
     */
    private void showStudentPane(Group group) {
        StudentListPanel studentListPanel = new StudentListPanel(group.getUnmodifiableStudentList());
        leftPanelPlaceholder.getChildren().add(studentListPanel.getRoot());
    }

    private void showModuleTab() {
        moduleTabPlaceholder.getChildren().clear();
        moduleTabPlaceholder.getChildren().add(new ActiveModuleTab().getRoot());
    }

    private void showGroupTab() {
        groupTabPlaceholder.getChildren().clear();
        groupTabPlaceholder.getChildren().add(new ActiveGroupTab().getRoot());
    }

    private void showStudentTab() {
        studentTabPlaceholder.getChildren().clear();
        studentTabPlaceholder.getChildren().add(new ActiveStudentTab().getRoot());
    }

    private void closeModuleTab() {
        moduleTabPlaceholder.getChildren().clear();
        moduleTabPlaceholder.getChildren().add(new InactiveModuleTab().getRoot());
    }

    private void closeGroupTab() {
        groupTabPlaceholder.getChildren().clear();
        groupTabPlaceholder.getChildren().add(new InactiveGroupTab().getRoot());
    }

    private void closeStudentTab() {
        studentTabPlaceholder.getChildren().clear();
        studentTabPlaceholder.getChildren().add(new InactiveStudentTab().getRoot());
    }

    /**
     * Handles UI for select command.
     */
    private void handleSelectCommand() {
        if (logic.getCurrentSelection().getCurrentPage().equals(PageType.GROUP_PAGE)) {

            showGroupPane(logic.getCurrentSelection().getSelectedCourse());
            closeModuleTab();
            showGroupTab();

        } else if (logic.getCurrentSelection().getCurrentPage().equals(PageType.STUDENT_PAGE)
                && !logic.getCurrentSelection().getSelectedStudent().equals(CurrentSelection.NON_EXISTENT_STUDENT)) {

            showStudentPane(logic.getCurrentSelection().getSelectedGroup());
            refreshViewPane();

        } else if (logic.getCurrentSelection().getCurrentPage().equals(PageType.STUDENT_PAGE)) {

            showStudentPane(logic.getCurrentSelection().getSelectedGroup());
            closeGroupTab();
            showStudentTab();

        }
    }

    /**
     * Handles UI for back command.
     * @param backCommand
     */
    private void handleBackCommand(BackCommand backCommand) {
        if (logic.getCurrentSelection().getCurrentPage().equals(PageType.COURSE_PAGE)) {
            showCoursePane();
            showModuleTab();
            closeGroupTab();
        } else if (logic.getCurrentSelection().getCurrentPage().equals(PageType.GROUP_PAGE)) {
            showGroupPane(backCommand.getPreviousSelection().getSelectedCourse());
            showGroupTab();
            closeViewPane();
            closeStudentTab();
        }
    }

    private void handleSpecialCommandConsiderations(CommandResult commandResult) {

        if (commandResult.getCommand() instanceof SelectCommand) {
            handleSelectCommand();

        } else if (commandResult.getCommand() instanceof BackCommand) {
            handleBackCommand((BackCommand) commandResult.getCommand());

        } else if (commandResult.getCommand() instanceof ExitCommand) {
            handleExit();

        } else if (commandResult.getCommand() instanceof HelpCommand) {
            handleHelp();

        } else if (commandResult.getCommand() instanceof HomeCommand) {
            handleHome();
        }

        //} else if (commandResult.getCommand() instanceof UndoCommand) {
        //handleUndo();
    }

    /**
     * Executes the command and returns the result.
     *
     * @see tfifteenfour.clipboard.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

            handleSpecialCommandConsiderations(commandResult);

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("Invalid command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }
}
