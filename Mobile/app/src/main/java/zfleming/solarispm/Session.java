package zfleming.solarispm;

public class Session {

    private User currentUser;
    private Project currentProject;
    private Task currentTask;


    public Project getCurrentProject() {
        return currentProject;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
