package com.codingchili.core.context;

/**
 * Results returned from an #{@link Command} when it is
 * executed by a #{@link CommandExecutor}.
 * <p>
 * If the command fails and wants to display the error message
 * (stacktrace) that generated the failure, then the future
 * passed from the #{@link CommandExecutor} should be failed,
 * with the exception.
 */
public enum LauncherCommandResult implements CommandResult {
    /**
     * Indicates that the command has executed successfully
     * and that the system should shut down.
     */
    SHUTDOWN("shutdown"),
    /**
     * Indicates that the command has executed successfully and
     * that the #{@link com.codingchili.core.Launcher} should continue
     * processing any deployment blocks.
     */
    CONTINUE("continue"),
    /**
     * Indicates that the command has executed successfully and that the
     * command is to handle deployment. The #{@link com.codingchili.core.Launcher}
     * should not process any deployment blocks.
     */
    STARTED("started");

    private String name;

    LauncherCommandResult(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
