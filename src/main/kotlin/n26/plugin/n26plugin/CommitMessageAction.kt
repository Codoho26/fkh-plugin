package n26.plugin.n26plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsException
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager

class CommitMessageAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

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

        val jiraTicketNumber = extractJiraTicketNumber(branchName)

        commitChanges(repository, jiraTicketNumber)
    }

    private fun commitChanges(repository: GitRepository, message: String) {
        val git = Git.getInstance()
        val handler = GitLineHandler(repository.project, repository.root, GitCommand.COMMIT)
        handler.addParameters("-m", message)
        try {
            git.runCommand(handler).throwOnError()
            Messages.showInfoMessage(repository.project, "Successfully created commit message with Jira ticket number!", "Success")
        } catch (e: VcsException) {
            Messages.showErrorDialog(repository.project, "Failed creating commit message with Jira ticket number!", "Error")
        }
    }

    private fun extractJiraTicketNumber(branchName: String): String {
        val regex = Regex("^[A-Za-z]+-\\d+")
        return regex.find(branchName)?.value ?: ""
    }
}