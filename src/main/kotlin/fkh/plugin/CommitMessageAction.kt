package fkh.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.changes.ui.CommitChangeListDialog
import git4idea.repo.GitRepositoryManager

class CommitMessageAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project

        if (project == null) {
            Messages.showErrorDialog("Could not find the project!", "Error")
            return
        }


        val repository = project.service<GitRepositoryManager>().repositories.firstOrNull()
        if (repository == null) {
            Messages.showErrorDialog(project, "No Git repository found for project: ${project.name}", "Error")
            return
        }

        val branchName = repository.currentBranchName
        if (branchName == null) {
            Messages.showErrorDialog(project, "No current branch found for project ${project.name}", "Error")
            return
        }
        Messages.showInfoMessage(project, "parsed branch name: $branchName", "Success")

        val jiraTicketNumber = extractJiraTicketNumber(branchName)

        Messages.showInfoMessage(project, "parsed Jira ticket number: $jiraTicketNumber", "Success")

        openCommitDialog(project, jiraTicketNumber)
    }

    private fun openCommitDialog(project: Project, commitMessage: String) {
        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.allChanges
        val initialSelection = changeListManager.defaultChangeList
        val executors = emptyList<CommitExecutor>()

        CommitChangeListDialog.commitChanges(
            project,
            changes,
            initialSelection,
            executors,
            true,
            commitMessage,
            null
        )
    }

    private fun extractJiraTicketNumber(branchName: String): String {
        val regex = Regex("^[A-Za-z]+-\\d+")
        return "[${regex.find(branchName)?.value ?: ""}]"
    }
}
