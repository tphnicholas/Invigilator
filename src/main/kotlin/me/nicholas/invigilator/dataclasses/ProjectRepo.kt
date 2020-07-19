package me.nicholas.invigilator.dataclasses

import java.net.*
import java.lang.Exception
import org.json.JSONObject
import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.isSuccessful

import me.nicholas.invigilator.extensions.makeRequest
import me.nicholas.invigilator.util.nonEmbedded

abstract class ProjectRepo(val url: URI, val owner: String, val repo: String) {
    abstract val baseApiUrl: String

    var isValidAndVisible: Boolean = false
    var asJson: JSONObject = JSONObject("{}")
    var license: String? = null
    var id: Int = 0

    companion object {
        fun parseFromUrl(projectUrl: String): ProjectRepo? {
            try {
                val uri = URI(projectUrl)

                val platform = uri.host
                        .split('.', limit = 2)
                        .first()

                val (owner, repo) = uri.path
                        .trim('/')
                        .split('/', limit = 2)

                 return when(platform) {
                    "github" -> GithubRepo(uri, owner, repo.split('/').first())
                    "gitlab" -> GitlabRepo(uri, owner, repo.substringBefore("/-/"))
                    else -> null
                }
            }
            catch(e: Exception) {
                return null
            }
        }
    }

    override fun toString() = nonEmbedded(url)

    abstract fun isIssueUrl(uri: URI): Boolean
    abstract fun isPullRequestUrl(uri: URI): Boolean
}

class GithubRepo(url: URI, owner: String, repo: String) : ProjectRepo(url, owner, repo) {
    override val baseApiUrl: String = "https://api.github.com"

    init {
        val result = "$baseApiUrl/repos/$owner/$repo"
                .httpGet()
                .header("Accept", "application/vnd.github.v3+json")
                .makeRequest()


        result?.let {
            isValidAndVisible = true
            asJson = it.obj()
            license = asJson.getJSONObject("license").getString("key")
            id = asJson.getInt("id")
        }
    }

    private fun getIssue(issueNumber: Int)
            = "$baseApiUrl/repos/$owner/$repo/issues/$issueNumber"
            .httpGet()
            .makeRequest()
            ?.obj()

    override fun isIssueUrl(uri: URI): Boolean {
        val issueUrlRegex = Regex("/$owner/$repo/issues/(\\d+)/?")

        val match = issueUrlRegex.find(uri.path)
                ?: return false

        val (issueNumber) = match.destructured

        val issue = getIssue(issueNumber.toInt())

        return ((issue != null) && !issue.has("pull_request"))
    }

    override fun isPullRequestUrl(uri: URI): Boolean {
        val pullRequestRegex = Regex("/$owner/$repo/pull/(\\d+)/?")

        val match = pullRequestRegex.find(uri.path)
                ?: return false

        val (pullRequestNumber) = match.destructured

        val issue = getIssue(pullRequestNumber.toInt())

        return ((issue != null) && issue.has("pull_request"))
    }
}

class GitlabRepo(url: URI, owner: String, repo: String) : ProjectRepo(url, owner, repo) {
    override val baseApiUrl: String = "https://gitlab.com/api/v4"

    init {
        val result = "$baseApiUrl/projects/${URLEncoder.encode("$owner/$repo", "UTF-8")}?license=1"
                .httpGet()
                .header("Content-Type", "application/json")
                .makeRequest()

        result?.let {
            isValidAndVisible = true
            asJson = it.obj()
            asJson.get("license")?.let {
                if (!it.equals(JSONObject.NULL)) {
                    license = (it as JSONObject).getString("key")
                }
            }
            id = asJson.getInt("id")
        }
    }

    override fun isIssueUrl(uri: URI): Boolean {
        val issueUrlRegex = Regex("/$owner/$repo(?:/-)?/issues/(\\d+)/?")

        val match = issueUrlRegex.find(uri.path)
                ?: return false

        val (issueNumber) = match.destructured

        val (_, response, _) = "$baseApiUrl/projects/$id/issues/$issueNumber"
                .httpGet()
                .response()

        return response.isSuccessful
    }

    override fun isPullRequestUrl(uri: URI): Boolean {
        val pullRequestRegex = Regex("/$owner/$repo(?:/-)?/merge_requests/(\\d+)/?")

        val match = pullRequestRegex.find(uri.path)
                ?: return false

        val (pullRequestNumber) = match.destructured

        val (_, response, _) = "$baseApiUrl/projects/$id/merge_requests/$pullRequestNumber"
                .httpGet()
                .response()

        return response.isSuccessful
    }
}