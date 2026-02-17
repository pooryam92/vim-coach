package com.github.pooryam92.vimcoach.services.source

import com.google.gson.annotations.SerializedName

/**
 * Response model from GitHub Contents API.
 * @see <a href="https://docs.github.com/en/rest/repos/contents">GitHub API Documentation</a>
 */
data class GitHubApiResponse(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("sha")
    val sha: String,
    
    @SerializedName("size")
    val size: Int,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("encoding")
    val encoding: String
)
