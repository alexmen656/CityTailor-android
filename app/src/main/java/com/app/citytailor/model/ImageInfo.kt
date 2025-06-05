package com.app.citytailor.model

/**
 * Represents information about an image, including its source and attribution.
 */
data class ImageInfo(
    val url: String,
    val description: String,
    val photographer: String,
    val photographerLink: String
)
