package tfifteenfour.clipboard.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import tfifteenfour.clipboard.commons.core.Messages;
import tfifteenfour.clipboard.commons.core.index.Index;
import tfifteenfour.clipboard.logic.CurrentSelection;
import tfifteenfour.clipboard.logic.commands.exceptions.CommandException;
import tfifteenfour.clipboard.model.Model;
import tfifteenfour.clipboard.model.course.Course;
import tfifteenfour.clipboard.model.course.Group;
import tfifteenfour.clipboard.model.course.Session;
import tfifteenfour.clipboard.model.student.Student;

/**
 * Views a student in the student list.
 */
public class SelectCommand extends Command {

    public static final String COMMAND_WORD = "select";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Views the selected item at the index number in the current displayed list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    private final Index targetIndex;

    /**
     * Creates a SelectCommand to select a student at the specified index
     */
    public SelectCommand(Index targetIndex) {
        super(false);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model, CurrentSelection currentSelection) throws CommandException {
        requireNonNull(model);



        switch (currentSelection.getCurrentPage()) {
        case COURSE_PAGE:
            // if you are on course page now, means you can only select a course
            Course selectedCourse = handleSelectCourse(model, currentSelection);
            return new CommandResult(this,
                    String.format("[GROUP PAGE]\nViewing: groups for course %s", selectedCourse), willModifyState);

        case GROUP_PAGE:
            // if you are on group page now, means you can only select a group
            Group selectedGroup = handleSelectGroup(model, currentSelection);
            return new CommandResult(this, String.format("[STUDENT PAGE]\nViewing: students in group %s of %s", selectedGroup,
                    currentSelection.getSelectedCourse()), willModifyState);

        case STUDENT_PAGE:
            Student selectedStudent = handleSelectStudent(model, currentSelection);
            return new CommandResult(this, String.format("Viewing: %s", selectedStudent), willModifyState);

        case SESSION_PAGE:
            Session selectedSession = handleSelectSession(model, currentSelection);
            return new CommandResult(this, String.format("[ATTENDANCE PAGE]\nViewing: session attendance for %s", selectedSession), willModifyState);

        default:
            throw new CommandException("Unable to select");
        }
    }

    private Course handleSelectCourse(Model model, CurrentSelection currentSelection) throws CommandException {

        List<Course> courseList = model.getUnmodifiableFilteredCourseList();
        if (targetIndex.getZeroBased() >= courseList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_COURSE_DISPLAYED_INDEX);
        }
        Course selectedCourse = courseList.get(targetIndex.getZeroBased());

        currentSelection.selectCourse(selectedCourse);
        return selectedCourse;
    }

    private Group handleSelectGroup(Model model, CurrentSelection currentSelection) throws CommandException {
        List<Group> groupList = currentSelection.getSelectedCourse().getUnmodifiableGroupList();
        if (targetIndex.getZeroBased() >= groupList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_GROUP_DISPLAYED_INDEX);
        }
        Group selectedGroup = groupList.get(targetIndex.getZeroBased());

        currentSelection.selectGroup(selectedGroup);
        return selectedGroup;
    }

    private Student handleSelectStudent(Model model, CurrentSelection currentSelection) throws CommandException {
        List<Student> studentList = currentSelection.getSelectedGroup().getUnmodifiableStudentList();
        if (targetIndex.getZeroBased() >= studentList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student selectedStudent = studentList.get(targetIndex.getZeroBased());
        currentSelection.selectStudent(selectedStudent);

        // end of navigation, no longer need to call setters of currentSelection
        return selectedStudent;
    }

    private Session handleSelectSession(Model model, CurrentSelection currentSelection) throws CommandException {
        List<Session> sessionList = currentSelection.getSelectedGroup().getUnmodifiableSessionList();
        if (targetIndex.getZeroBased() >= sessionList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_SESSION_DISPLAYED_INDEX);
        }
        Session selectedSession = sessionList.get(targetIndex.getZeroBased());

        currentSelection.selectSession(selectedSession);
        // end of navigation, no longer need to call setters of currentSelection
        return selectedSession;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SelectCommand)) {
            return false;
        }

        // state check
        SelectCommand e = (SelectCommand) other;
        return targetIndex.equals(e.targetIndex);
    }
}
