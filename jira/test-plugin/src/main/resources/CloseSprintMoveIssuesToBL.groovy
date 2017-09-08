/**
 * Find and close all active sprints and move incomplete issues back into the backlog.
 */

import com.atlassian.greenhopper.service.sprint.Sprint
import com.atlassian.greenhopper.service.sprint.SprintManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.onresolve.scriptrunner.runner.customisers.PluginModuleCompilationCustomiser
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import utils.HelperFunctions

@WithPlugin("com.pyxis.greenhopper.jira")


def sprintServiceOutcome = PluginModuleCompilationCustomiser.getGreenHopperBean(SprintManager).getAllSprints()

if (sprintServiceOutcome.valid) {
    sprintServiceOutcome.
        getValue().
        findAll { it.state == Sprint.State.ACTIVE }?.
        each {
            moveUnfinishedStoriesBackInBL(it)
            new HelperFunctions().updateSprintState(it.name, Sprint.State.CLOSED)
        }
}
else {
    log.error "Invalid sprint service outcome, ${sprintServiceOutcome.errors}"
}

/**
 * Get all the issues returned by the jql Sprint = "sprint name" and make their sprint custom field null,
 * which means add them back into the backlog
 * @param sprint
 * @return
 */
def moveUnfinishedStoriesBackInBL (Sprint sprint) {

    def jqlSearch = "Sprint = '${sprint.name}'"
    def issues = new HelperFunctions().getIssuesFromJQL(jqlSearch)
    def sprintCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Sprint")

    issues?.
        findAll { it.status.statusCategory.name != "Complete" }?.
        each {
            log.debug "Issue ${it.key} with status ${it.status.name}, moved into backlog"
            sprintCF.updateValue(null, it, new ModifiedValue(it.getCustomFieldValue(sprintCF), null), new DefaultIssueChangeHolder())
        }
}