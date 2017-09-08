/**
 * Bulk clone issues returned from a JQL to a different project
 */

import com.atlassian.jira.component.ComponentAccessor
import utils.HelperFunctions

def PROJECT_KEY_TO = "PT"
def jqlSearch = """ project = 'Source Project' """

def issueManager = ComponentAccessor.getIssueManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def issueFactory = ComponentAccessor.getIssueFactory()


def projectTo = ComponentAccessor.getProjectManager().getProjectByCurrentKey(PROJECT_KEY_TO)
def issuesFrom = new HelperFunctions().getIssuesFromJQL(jqlSearch)

issuesFrom?.each { it ->
    def newIssue = issueFactory.cloneIssueWithAllFields(it)

    // set the project and any field you want to have a different value
    newIssue.setProjectObject(projectTo)

    def newIssueParams = ["issue" : newIssue] as Map<String,Object>
    issueManager.createIssueObject(user, newIssueParams)

    log.info "Issue ${newIssue?.key} cloned to project ${projectTo.key}"
}