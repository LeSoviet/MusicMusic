package com.musicmusic.domain.model

import kotlinx.serialization.Serializable

/**
 * Wrapper para el archivo JSON de radios.
 */
@Serializable
data class RadioList(
    val radios: List<Radio>
)
