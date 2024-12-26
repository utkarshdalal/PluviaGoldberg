package com.OxGames.Pluvia.data

data class UserFilesUploadResult(
    val uploadBatchSuccess: Boolean,
    val appChangeNumber: Long,
    val filesUploaded: Int,
    val bytesUploaded: Long,
)
