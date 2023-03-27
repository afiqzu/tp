package tfifteenfour.clipboard.logic.parser;

import tfifteenfour.clipboard.logic.parser.exceptions.ParseException;

/**
 * Handles which entity a command is targeted at
 */
public enum CommandTargetType {
    MODULE("course"),
    GROUP("group"),
    SESSION("session"),
    STUDENT("student");

    private String type;

    private CommandTargetType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    /**
     * Placeholder
     */
    public static CommandTargetType fromString(String type) throws ParseException {
        for (CommandTargetType sc : CommandTargetType.values()) {
            if (sc.getType().equalsIgnoreCase(type)) {
                return sc;
            }
        }

        throw new ParseException("Unrecognised category for command: " + type);
    }

    /**
     * Placeholder
     */
    public static boolean isValidAddType(String type) throws ParseException {
        for (CommandTargetType sc : CommandTargetType.values()) {
            if (sc.getType().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
