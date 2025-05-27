package app.gamenative.data

data class UserFilesUploadResult(
    val uploadBatchSuccess: Boolean,
    val appChangeNumber: Long,
    val filesUploaded: Int,
    val bytesUploaded: Long,
)
