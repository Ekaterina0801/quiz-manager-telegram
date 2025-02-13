package com.Quiz_manager.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
@Service
class CloudinaryService(
    @Value("\${cloudinary.cloud_name}") private val cloudName: String,
    @Value("\${cloudinary.api_key}") private val apiKey: String,
    @Value("\${cloudinary.api_secret}") private val apiSecret: String
) {
    private val cloudinary: Cloudinary = Cloudinary(ObjectUtils.asMap(
        "cloud_name", cloudName,
        "api_key", apiKey,
        "api_secret", apiSecret
    ))

    fun uploadImage(file: File): String {
        val uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
        return uploadResult["url"].toString()
    }
}
