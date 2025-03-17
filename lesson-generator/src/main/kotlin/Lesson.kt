data class Lesson(
  val levelIndex: Int,
  val notes: List<Note>,
  val instruments: List<Instrument>,
  val promptSpacingSeconds: Int = 60,
  val promptAnswerSpacingSeconds: Int,
  val totalPrompts: Int,
  val fileName: String = "$levelIndex" +
      "-${notes.joinToString("-") { it.name }}" +
      "-${instruments.joinToString("-") { it.name }}" +
      "-${promptSpacingSeconds}" +
      "-${totalPrompts}" +
      ".mp3"
) {
  companion object {
    val all: List<Lesson>

    init {
      all = mutableListOf()

      all.add(
        Lesson(
          levelIndex = 1,
          notes = listOf(Note.c4, Note.a4),
          instruments = listOf(Instrument.piano),
          promptSpacingSeconds = 60,
          promptAnswerSpacingSeconds = 3,
          totalPrompts = 100,
        )
      )

      all.add(
        Lesson(
          levelIndex = 2,
          notes = listOf(Note.a4, Note.f4),
          instruments = listOf(Instrument.piano),
          promptSpacingSeconds = 60,
          promptAnswerSpacingSeconds = 3,
          totalPrompts = 100,
        )
      )

      all.add(
        Lesson(
          levelIndex = 3,
          notes = listOf(Note.c4, Note.a4, Note.f4),
          instruments = listOf(Instrument.piano),
          promptSpacingSeconds = 60,
          promptAnswerSpacingSeconds = 3,
          totalPrompts = 100,
        )
      )

      all.add(
        Lesson(
          levelIndex = 4,
          notes = listOf(Note.c4, Note.a4, Note.f4, Note.c3, Note.a3),
          instruments = listOf(Instrument.piano),
          promptSpacingSeconds = 60,
          promptAnswerSpacingSeconds = 3,
          totalPrompts = 100,
        )
      )
      all.add(
        Lesson(
          levelIndex = 5,
          notes = listOf(Note.c4, Note.a4, Note.f4, Note.f3, Note.a3),
          instruments = listOf(Instrument.piano),
          promptSpacingSeconds = 60,
          promptAnswerSpacingSeconds = 3,
          totalPrompts = 100,
        )
      )
      all.add(
        Lesson(
          levelIndex = 6,
          notes = listOf(Note.c4, Note.a4, Note.f4, Note.e0),
          instruments = listOf(Instrument.piano),
          promptSpacingSeconds = 60,
          promptAnswerSpacingSeconds = 3,
          totalPrompts = 100,
        )
      )
    }
  }
}
