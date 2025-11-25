package com.musicmusic.domain.model

/**
 * Banda de frecuencia del ecualizador.
 *
 * @param frequency Frecuencia central en Hz
 * @param gain Ganancia en dB (rango típico: -20.0 a +20.0)
 */
data class EqualizerBand(
    val frequency: Float,
    val gain: Float = 0f
)

/**
 * Configuración del ecualizador.
 *
 * @param isEnabled Si el ecualizador está activo
 * @param preamp Preamplificación en dB (rango típico: -20.0 a +20.0)
 * @param bands Lista de bandas de frecuencia (típicamente 10 bandas)
 */
data class EqualizerSettings(
    val isEnabled: Boolean = false,
    val preamp: Float = 0f,
    val bands: List<Float> = List(10) { 0f }  // 10 bandas con ganancia 0
)

/**
 * Preset predefinido del ecualizador.
 */
data class EqualizerPreset(
    val name: String,
    val preamp: Float,
    val bands: List<Float>
) {
    companion object {
        /**
         * Frecuencias estándar del ecualizador de 10 bandas (en Hz)
         */
        val STANDARD_FREQUENCIES = listOf(
            31.25f,   // 31 Hz
            62.5f,    // 62 Hz
            125f,     // 125 Hz
            250f,     // 250 Hz
            500f,     // 500 Hz
            1000f,    // 1 kHz
            2000f,    // 2 kHz
            4000f,    // 4 kHz
            8000f,    // 8 kHz
            16000f    // 16 kHz
        )

        /**
         * Preset plano (ningún ajuste)
         */
        val FLAT = EqualizerPreset(
            name = "Flat",
            preamp = 0f,
            bands = List(10) { 0f }
        )

        /**
         * Preset para música clásica
         */
        val CLASSICAL = EqualizerPreset(
            name = "Classical",
            preamp = 0f,
            bands = listOf(0f, 0f, 0f, 0f, 0f, 0f, -7.2f, -7.2f, -7.2f, -9.6f)
        )

        /**
         * Preset para música electrónica/club
         */
        val CLUB = EqualizerPreset(
            name = "Club",
            preamp = 0f,
            bands = listOf(0f, 0f, 8f, 5.6f, 5.6f, 5.6f, 3.2f, 0f, 0f, 0f)
        )

        /**
         * Preset para música dance
         */
        val DANCE = EqualizerPreset(
            name = "Dance",
            preamp = 0f,
            bands = listOf(9.6f, 7.2f, 2.4f, 0f, 0f, -5.6f, -7.2f, -7.2f, 0f, 0f)
        )

        /**
         * Preset Full Bass
         */
        val FULL_BASS = EqualizerPreset(
            name = "Full Bass",
            preamp = 0f,
            bands = listOf(-8f, 9.6f, 9.6f, 5.6f, 1.6f, -4f, -8f, -10.4f, -11.2f, -11.2f)
        )

        /**
         * Preset Full Treble
         */
        val FULL_TREBLE = EqualizerPreset(
            name = "Full Treble",
            preamp = 0f,
            bands = listOf(-9.6f, -9.6f, -9.6f, -4f, 2.4f, 11.2f, 16f, 16f, 16f, 16.8f)
        )

        /**
         * Preset Full Bass & Treble
         */
        val FULL_BASS_AND_TREBLE = EqualizerPreset(
            name = "Full Bass & Treble",
            preamp = 0f,
            bands = listOf(7.2f, 5.6f, 0f, -7.2f, -4.8f, 1.6f, 8f, 11.2f, 12f, 12f)
        )

        /**
         * Preset para laptops / altavoces pequeños
         */
        val LAPTOP = EqualizerPreset(
            name = "Laptop",
            preamp = 0f,
            bands = listOf(4.8f, 11.2f, 5.6f, -3.2f, -2.4f, 1.6f, 4.8f, 9.6f, 12.8f, 14.4f)
        )

        /**
         * Preset para headphones grandes
         */
        val LARGE_HALL = EqualizerPreset(
            name = "Large Hall",
            preamp = 0f,
            bands = listOf(10.4f, 10.4f, 5.6f, 5.6f, 0f, -4.8f, -4.8f, -4.8f, 0f, 0f)
        )

        /**
         * Preset para música en vivo
         */
        val LIVE = EqualizerPreset(
            name = "Live",
            preamp = 0f,
            bands = listOf(-4.8f, 0f, 4f, 5.6f, 5.6f, 5.6f, 4f, 2.4f, 2.4f, 2.4f)
        )

        /**
         * Preset para música pop
         */
        val POP = EqualizerPreset(
            name = "Pop",
            preamp = 0f,
            bands = listOf(-1.6f, 4.8f, 7.2f, 8f, 5.6f, 0f, -2.4f, -2.4f, -1.6f, -1.6f)
        )

        /**
         * Preset para reggae
         */
        val REGGAE = EqualizerPreset(
            name = "Reggae",
            preamp = 0f,
            bands = listOf(0f, 0f, 0f, -5.6f, 0f, 6.4f, 6.4f, 0f, 0f, 0f)
        )

        /**
         * Preset para rock
         */
        val ROCK = EqualizerPreset(
            name = "Rock",
            preamp = 0f,
            bands = listOf(8f, 4.8f, -5.6f, -8f, -3.2f, 4f, 8.8f, 11.2f, 11.2f, 11.2f)
        )

        /**
         * Preset para ska
         */
        val SKA = EqualizerPreset(
            name = "Ska",
            preamp = 0f,
            bands = listOf(-2.4f, -4.8f, -4f, 0f, 4f, 5.6f, 8.8f, 9.6f, 11.2f, 9.6f)
        )

        /**
         * Preset para música suave
         */
        val SOFT = EqualizerPreset(
            name = "Soft",
            preamp = 0f,
            bands = listOf(4.8f, 1.6f, 0f, -2.4f, 0f, 4f, 8f, 9.6f, 11.2f, 12f)
        )

        /**
         * Preset para música soft rock
         */
        val SOFT_ROCK = EqualizerPreset(
            name = "Soft Rock",
            preamp = 0f,
            bands = listOf(4f, 4f, 2.4f, 0f, -4f, -5.6f, -3.2f, 0f, 2.4f, 8.8f)
        )

        /**
         * Preset para música techno
         */
        val TECHNO = EqualizerPreset(
            name = "Techno",
            preamp = 0f,
            bands = listOf(8f, 5.6f, 0f, -5.6f, -4.8f, 0f, 8f, 9.6f, 9.6f, 8.8f)
        )

        /**
         * Lista de todos los presets predefinidos
         */
        val ALL_PRESETS = listOf(
            FLAT,
            CLASSICAL,
            CLUB,
            DANCE,
            FULL_BASS,
            FULL_TREBLE,
            FULL_BASS_AND_TREBLE,
            LAPTOP,
            LARGE_HALL,
            LIVE,
            POP,
            REGGAE,
            ROCK,
            SKA,
            SOFT,
            SOFT_ROCK,
            TECHNO
        )

        /**
         * Obtiene un preset por nombre
         */
        fun getPresetByName(name: String): EqualizerPreset? {
            return ALL_PRESETS.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}

/**
 * Configuración del normalizador de volumen.
 *
 * @param isEnabled Si el normalizador está activo
 * @param level Nivel de normalización (0.0 a 1.0)
 */
data class VolumeNormalizerSettings(
    val isEnabled: Boolean = false,
    val level: Float = 0.5f
)
