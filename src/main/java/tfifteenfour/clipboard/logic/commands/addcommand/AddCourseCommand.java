package tfifteenfour.clipboard.logic.commands.addcommand;

import static java.util.Objects.requireNonNull;

import tfifteenfour.clipboard.logic.CurrentSelection;
import tfifteenfour.clipboard.logic.PageType;
import tfifteenfour.clipboard.logic.commands.CommandResult;
import tfifteenfour.clipboard.logic.commands.exceptions.CommandException;
import tfifteenfour.clipboard.model.Model;
import tfifteenfour.clipboard.model.course.Course;

/**
 * Adds a course to the roster.
 */
public class AddCourseCommand extends AddCommand {
    public static final String COMMAND_TYPE_WORD = "course";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " " + COMMAND_TYPE_WORD
            + ": Adds a course. "
            + "Parameters: "
            + "COURSE_CODE\n"
            + "Example: " + COMMAND_WORD
            + " " + COMMAND_TYPE_WORD
            + " " + "CS2103T ";

    public static final String MESSAGE_SUCCESS = "New course added: %1$s";
    public static final String MESSAGE_DUPLICATE_COURSE = "This course already exists";

    private final Course courseToAdd;

    public AddCourseCommand(Course course) {
        this.courseToAdd = course;
    }

    /**
     * Executes the command and returns the result message.
     * @param model {@code Model} which the command should operate on.
     * @param currentSelection of the {@code LogicManager}.
     * @throws CommandException If an error occurs during command execution.
     */
    public CommandResult execute(Model model, CurrentSelection currentSelection) throws CommandException {
        requireNonNull(model);

        if (currentSelection.getCurrentPage() != PageType.COURSE_PAGE) {
            throw new CommandException("Wrong page. Navigate to course page to add course");
        }

        if (model.hasCourse(courseToAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_COURSE);
        }

        model.addCourse(courseToAdd);
        return new CommandResult(this, String.format(MESSAGE_SUCCESS, courseToAdd), willModifyState);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddCourseCommand // instanceof handles nulls
                && courseToAdd.equals(((AddCourseCommand) other).courseToAdd));
    }
}