package com.Quiz_manager.mapper

import com.Quiz_manager.service.CloudinaryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.multipart.MultipartFile
import java.io.File


fun MultipartFile?.uploadImageIfPresent(@Autowired cloudinaryService: CloudinaryService): String? {

    return this?.let { file ->
        val tempFile = File.createTempFile("upload-", "-" + file.originalFilename)
        try {
            file.transferTo(tempFile)
            val uploadResult = cloudinaryService.uploadImage(tempFile)
            tempFile.deleteOnExit()
            uploadResult
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

