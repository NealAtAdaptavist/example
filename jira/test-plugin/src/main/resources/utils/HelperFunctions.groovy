package utils

import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.bc.user.UserService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.comments.Comment
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.greenhopper.service.sprint.Sprint
import com.atlassian.greenhopper.service.sprint.SprintManager
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.workflow.TransitionOptions
import com.atlassian.mail.Email
import com.onresolve.scriptrunner.runner.customisers.PluginModuleCompilationCustomiser
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import groovy.json.JsonSlurper

@WithPlugin("com.pyxis.greenhopper.jira")

class HelperFunctions {

    ApplicationUser asUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    UserService userService = ComponentAccessor.getComponent(UserService)
    SearchService searchService = ComponentAccessor.getComponent(SearchService)
    IssueManager issueManager = ComponentAccessor.getIssueManager()
    SprintManager sprintManager = PluginModuleCompilationCustomiser.getGreenHopperBean(SprintManager)
    CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager()


    void creteTestUsers (String... userNames) {

        userNames?.each { it ->
            UserService.CreateUserRequest createUserRequest = UserService.CreateUserRequest.
                withUserDetails (
                    asUser,
                    it,
                    "password",
                    "${it}@example.com",
                    "${it} Full Name "
                )

            UserService.CreateUserValidationResult result = userService.validateCreateUser(createUserRequest)
            if (result.isValid()) {
                def newUser = userService.createUser(result)
                log.info "User ${newUser} succesfully created"
            }
            else {
                log.error "Failed to create asUser with username $it. " + result.getErrorCollection()
            }
        }
    }

    def deactivateUser (ApplicationUser user) {
        def updateUser = userService.newUserBuilder(user).active(false).build()
        def updateUserValidationResult = userService.validateUpdateUser(updateUser)

        if (updateUserValidationResult.isValid()) {
            userService.updateUser(updateUserValidationResult)
            log.info "${updateUser.name} deactivated"
        }
        else {
            log.error "Update of ${user.name} failed. ${updateUserValidationResult.getErrorCollection()}"
        }
    }

    List<Issue> getIssuesFromJQL (String jql) {
        SearchService.ParseResult parseResult =  searchService.parseQuery(asUser, jql)

        if (parseResult.isValid()) {
            def searchResult = searchService.search(asUser, parseResult.getQuery(), PagerFilter.getUnlimitedFilter())
            return searchResult.issues.collect { issueManager.getIssueObject(it.id) } as List <Issue>
        }

        []
    }

    void sendEmail(String to, String subject, String body) {
        def mailServer = ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer()

        if (mailServer) {
            Email email = new Email(to)
            email.setSubject(subject)
            email.setBody(body)
            mailServer.send(email)
        }
        else {
            log.warn("There was an issue with the mail Server")
        }
    }

    Issue transitIssue(Issue issue, int actionId, ApplicationUser cwdUser = asUser) {
        def issueService = ComponentAccessor.getIssueService()

        IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
        issueInputParameters.setSkipScreenCheck(true)

        def transitionOptions= new TransitionOptions.Builder()
            .skipConditions()
            .skipPermissions()
            .skipValidators()
            .build()

        def transitionValidationResult =
            issueService.validateTransition(cwdUser, issue.id, actionId, issueInputParameters, transitionOptions)

        if (transitionValidationResult.isValid()) {
            return issueService.transition(cwdUser, transitionValidationResult).getIssue()
        }
        else {
            log.error "Transition for issue $issue.key failed. $transitionValidationResult.errorCollection"
            return null
        }
    }


    //-------------------------------------------------------- Service Desk Related ----------------------------------------------------------


    void updateRequesType (String issueKey, String requestTypeKey) {
        def issue = issueManager.getIssueByCurrentKey(issueKey)
        def tgtField = customFieldManager.getCustomFieldObjectByName("Customer Request Type")
        def requestType = tgtField.getCustomFieldType().getSingularObjectFromString(requestTypeKey)

        tgtField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(tgtField), requestType), new DefaultIssueChangeHolder())
    }


    boolean isSDCommentInternal (Comment comment) {
        def commentPropertyService = ComponentAccessor.getComponent(CommentPropertyService)
        def commentProperty = commentPropertyService.getProperty(asUser, comment.id, "sd.public.comment").getEntityProperty().getOrNull()

        if (commentProperty) {
            def props = new JsonSlurper().parseText(commentProperty.getValue())
            return props['internal']
        }
        else {
            return false
        }
    }


    // ----------------------------------------------------- JIRA SOFTWARE RELATED -------------------------------------------------------------


    void renameSprint (String fromName, String toName) {
        def sprint = getSprintWithName("fromName")

        if (! sprint) {
            log.debug "Could not find sprint with name $fromName"
            return
        }

        def newSprint = sprint.builder(sprint).name(toName).build()
        def outcome = sprintManager.updateSprint(newSprint)

        if (outcome.isInvalid()) {
            log.debug "Could not update sprint with name : Sprint 2 because ${outcome.getErrors()}"
        }
        else {
            log.debug "Sprint Updated !!"
        }

    }

    void updateSprintState(String sprintName, Sprint.State state) {

        def sprint = getSprintWithName(sprintName)
        if (! sprint) {
            log.debug "Could not find sprint with name $sprintName"
            return
        }

        def newSprint = sprint.builder(sprint).state(state).build()
        def outcome = sprintManager.updateSprint(newSprint)

        if (outcome.isInvalid()) {
            log.debug "Could not update sprint with name ${sprint.name}, ${outcome.getErrors()}"
        }
        else {
            log.debug "${sprint.name} updated."
        }
    }

    Sprint getSprintWithName (String sprintName) {
        def sprintServiceOutcome = sprintManager.getAllSprints()

        if (sprintServiceOutcome.valid) {
            return sprintServiceOutcome.getValue().find { it.name == sprintName } as Sprint
        }
        else {
            log.error "${sprintServiceOutcome.getErrors()}"
            return null
        }
    }

}
