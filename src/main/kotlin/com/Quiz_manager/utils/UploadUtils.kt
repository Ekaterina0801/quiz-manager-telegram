package com.Quiz_manager.utils

import com.Quiz_manager.service.CloudinaryService
import org.springframework.web.multipart.MultipartFile
import java.io.File

fun MultipartFile?.uploadImageIfPresent(cloudinaryService: CloudinaryService): String? {
    if (this == null) return null

    val tempFile = File(System.getProperty("java.io.tmpdir") + "/" + this.originalFilename)
    this.transferTo(tempFile)
    return cloudinaryService.uploadImage(tempFile)
}