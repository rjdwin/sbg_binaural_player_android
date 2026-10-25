package org.cigaes.binaural_player

data class Preset(
    val title: String,
    val description: String,
    val sequence: String
)

object Presets {
    val items = listOf(
        Preset(
            title = "Alpha Focus & Relaxation (10 Hz)",
            description = "440 Hz base carrier with 10 Hz alpha wave beat for calm mental clarity.",
            sequence = """
                # 30-minute Alpha Relaxation
                beat: 440+10/50
                off: -
                NOW beat
                +00:30:00 off
            """.trimIndent()
        ),
        Preset(
            title = "Theta Deep Meditation (6 Hz)",
            description = "220 Hz base with a gentle 6 Hz beat and mild pink noise.",
            sequence = """
                # 20-minute Theta Meditation
                beat: pink/20 220+6/60
                off: -
                NOW beat
                +00:20:00 off
            """.trimIndent()
        ),
        Preset(
            title = "Delta Deep Sleep (2 Hz)",
            description = "136.1 Hz carrier with 2 Hz delta frequency to assist restorative sleep.",
            sequence = """
                # 45-minute Delta Sleep
                beat: pink/30 136.1+2/50
                off: -
                NOW beat
                +00:45:00 off
            """.trimIndent()
        ),
        Preset(
            title = "Schumann Earth Resonance (7.83 Hz)",
            description = "Grounding frequency with harmonics across 200 Hz carrier.",
            sequence = """
                # Schumann Grounding Resonance
                beat: pink/15 200+7.83/60 400+7.83/30
                off: -
                NOW beat
                +00:30:00 off
            """.trimIndent()
        )
    )
}