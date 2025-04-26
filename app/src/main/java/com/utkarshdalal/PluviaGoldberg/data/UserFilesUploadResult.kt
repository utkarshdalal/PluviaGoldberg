package com.utkarshdalal.PluviaGoldberg.data

data class UserFilesUploadResult(
    val uploadBatchSuccess: Boolean,
    val appChangeNumber: Long,
    val filesUploaded: Int,
    val bytesUploaded: Long,
)
