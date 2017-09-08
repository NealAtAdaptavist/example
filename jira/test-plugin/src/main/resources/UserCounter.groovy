import com.atlassian.jira.ComponentManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.workflow.WorkflowManager
import com.atlassian.sal.api.ApplicationProperties
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl



class JIRAInfo {
    def userManager = ComponentAccessor.getUserManager() as UserManager
    def projectManager = ComponentAccessor.getProjectManager() as ProjectManager
    def workflowManager = ComponentAccessor.getWorkflowManager() as WorkflowManager
    def customFieldManager = ComponentAccessor.getCustomFieldManager() as CustomFieldManager
    def issueManager = ComponentAccessor.getIssueManager() as IssueManager
    def applicationManager = ScriptRunnerImpl.getOsgiService(ApplicationProperties)

    /**
     * Returns string output of current JIRA information
     **/
    String getCounts() {
        return """
Current User Count ${-> getUserCount()}
Current Project Count ${-> getProjectCount()}
Current Issue Count ${-> getIssueCount()}
Current Workflow Count ${-> getWorkflowCount()}
Current CustomField Count ${-> getCustomFieldCount()}
Build Information: ${-> getbuildInformation()}
"""
    }
    /**
     * Returns current count of users
     **/
    Integer getUserCount() {
        return userManager.getTotalUserCount()
    }
    /**
     * Returns current count of projects
     **/
    Integer getProjectCount() {
        return projectManager.getProjectCount()
        projectManager.createProject()

    }
    Integer getWorkflowCount() {
        return workflowManager.getWorkflows().size()

    }
    Integer getCustomFieldCount() {
        return customFieldManager.getCustomFieldObjects().size()
    }
    Integer getIssueCount() {
        return issueManager.getIssueCount()
    }
    String getbuildInformation() {
        return """ 
${applicationManager.getBuildNumber()} - (Build
${applicationManager.getVersion()}
${applicationManager.getPlatformId()}
${applicationManager.getHomeDirectory()}
"""
    }
}

def thisJIRA = new JIRAInfo()
log.warn thisJIRA.getCounts()




return 'Complete'

