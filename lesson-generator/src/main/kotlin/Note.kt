import Note.c4

/**
 * [frequency]: The physical frequency in Hz of the note.
 *
 * The following table lists the frequency for each note:
 * Octave 	  C	      C#	  D       D#      E	      F       F#	  G	      G# 	  A	      A#	  B
 * 0 	      16.35	  17.32	  18.35	  19.45   20.60	  21.83	  23.12	  24.50	  25.96	  27.50	  29.14	  30.87
 * 1 	      32.70	  34.65	  36.71	  38.89   41.20	  43.65	  46.25	  49.00	  51.91	  55.00	  58.27	  61.74
 * 2 	      65.41	  69.30	  73.42	  77.78	  82.41	  87.31	  92.50	  98.00	  103.8	  110.0	  116.5	  123.5
 * 3 	      130.8	  138.6	  146.8	  155.6	  164.8	  174.6	  185.0	  196.0	  207.7	  220.0	  233.1	  246.9
 * 4 	      261.6	  277.2	  293.7	  311.1	  329.6	  349.2	  370.0	  392.0	  415.3	  440.0	  466.2	  493.9
 * 5 	      523.3	  554.4	  587.3	  622.3	  659.3	  698.5	  740.0	  784.0	  830.6	  880.0	  932.3	  987.8
 * 6	      1047	  1109	  1175	  1245	  1319	  1397	  1480	  1568	  1661	  1760	  1865	  1976
 * 7	      2093	  2217	  2349	  2489	  2637	  2794	  2960	  3136	  3322	  3520	  3729	  3951
 * 8	      4186	  4435	  4699	  4978	  5274	  5588	  5920	  6272	  6645	  7040	  7459	  7902
 *
 * [nameo]: The character representing the note, including the ♯ if appropriate.
 * [nameFlat]: The character representing the minor key version of the note, including the ♭ if appropriate.
 * [clefIndex]: The index relative to middle C ([c4]), where the note resides on the staff.
 */
enum class Note(
  val frequency: Float,
  val nameo: String,
  val nameFlat: String,
  val clefIndex: Float,
) {
  c0(16.35f, "C", "C", -28f),
  cs0(17.32f, "C♯", "D♭", -27.5f),
  d0(18.35f, "D", "D", -27f),
  ds0(19.45f, "D♯", "E♭", -26.5f),
  e0(20.60f, "E", "E", -26f),
  f0(21.83f, "F", "F", -25f),
  fs0(23.12f, "F♯", "G♭", -24.5f),
  g0(24.50f, "G", "G", -24f),
  gs0(25.96f, "G♯", "A♭", -23.5f),
  a0(27.50f, "A", "A", -23f),
  as0(29.14f, "A♯", "B♭", -22.5f),
  b0(30.87f, "B", "B", -22f),
  c1(32.70f, "C", "C", -21f),
  cs1(34.65f, "C♯", "D♭", -20.5f),
  d1(36.71f, "D", "D", -20f),
  ds1(38.89f, "D♯", "E♭", -19.5f),
  e1(41.20f, "E", "E", -19f),
  f1(43.65f, "F", "F", -18f),
  fs1(46.25f, "F♯", "G♭", -17.5f),
  g1(49.00f, "G", "G", -17f),
  gs1(51.91f, "G♯", "A♭", -16.5f),
  a1(55.00f, "Aå", "A", -16f),
  as1(58.27f, "A♯", "B♭", -15.5f),
  b1(61.74f, "B", "B", -15f),
  c2(65.41f, "C", "C", -14f),
  cs2(69.30f, "C♯", "D♭", -13.5f),
  d2(73.42f, "D", "D", -13f),
  ds2(77.78f, "D♯", "E♭", -12.5f),
  e2(82.41f, "E", "E", -12f),
  f2(87.31f, "F", "F", -11f),
  fs2(92.50f, "F♯", "G♭", -10.5f),
  g2(98.00f, "G", "G", -10f),
  gs2(103.8f, "G♯", "A♭", -9.5f),
  a2(110.0f, "A", "A", -9f),
  as2(116.5f, "A♯", "B♭", -8.5f),
  b2(123.5f, "B", "B", -8f),
  c3(130.8f, "C", "C", -7f),
  cs3(138.6f, "C♯", "D♭", -6.5f),
  d3(146.8f, "D", "D", -6f),
  ds3(155.6f, "D♯", "E♭", -5.5f),
  e3(164.8f, "E", "E", -5f),
  f3(174.6f, "F", "F", -4f),
  fs3(185.0f, "F♯", "G♭", -3.5f),
  g3(196.0f, "G", "G", -3f),
  gs3(207.7f, "G♯", "A♭", -2.5f),
  a3(220.0f, "A", "A", -2f),
  as3(233.1f, "A♯", "B♭", -1.5f),
  b3(246.9f, "B", "B", -1f),
  c4(261.6f, "C", "C", 0f), // Middle C.
  cs4(277.2f, "C♯", "D♭", 0.5f),
  d4(293.7f, "D", "D", 1f),
  ds4(311.1f, "D♯", "E♭", 1.5f),
  e4(329.6f, "E", "E", 2f),
  f4(349.2f, "F", "F", 3f),
  fs4(370.0f, "F♯", "G♭", 3.5f),
  g4(392.0f, "G", "G", 4f),
  gs4(415.3f, "G♯", "A♭", 4.5f),
  a4(440.0f, "A", "A", 5f),
  as4(466.2f, "A♯", "B♭", 5.5f),
  b4(493.9f, "B", "B", 6f),
  c5(523.3f, "C", "C", 7f),
  cs5(554.4f, "C♯", "D♭", 7.5f),
  d5(587.3f, "D", "D", 8f),
  ds5(622.3f, "D♯", "E♭", 8.5f),
  e5(659.3f, "E", "E", 9f),
  f5(698.5f, "F", "F", 10f),
  fs5(740.0f, "F♯", "G♭", 10.5f),
  g5(784.0f, "G", "G", 11f),
  gs5(830.6f, "G♯", "A♭", 11.5f),
  a5(880.0f, "A", "A", 12f),
  as5(932.3f, "A♯", "B♭", 12.5f),
  b5(987.8f, "B", "B", 13f),
  c6(1047f, "C", "C", 14f),
  cs6(1109f, "C♯", "D♭", 14.5f),
  d6(1175f, "D", "D", 15f),
  ds6(1245f, "D♯", "E♭", 15.5f),
  e6(1319f, "E", "E", 16f),
  f6(1397f, "F", "F", 17f),
  fs6(1480f, "F♯", "G♭", 17.5f),
  g6(1568f, "G", "G", 18f),
  gs6(1661f, "G♯", "A♭", 18.5f),
  a6(1760f, "A", "A", 19f),
  as6(1865f, "A♯", "B♭", 19.5f),
  b6(1976f, "B", "B", 20f),
  c7(2093f, "C", "C", 21f),
  cs7(2217f, "C♯", "D♭", 21.5f),
  d7(2349f, "D", "D", 22f),
  ds7(2489f, "D♯", "E♭", 22.5f),
  e7(2637f, "E", "E", 23f),
  f7(2794f, "F", "F", 24f),
  fs7(2960f, "F♯", "G♭", 24.5f),
  g7(3136f, "G", "G", 25f),
  gs7(3322f, "G♯", "A♭", 25.5f),
  a7(3520f, "A", "A", 26f),
  as7(3729f, "A♯", "B♭", 26.5f),
  b7(3951f, "B", "B", 27f),
  c8(4186f, "C", "C", 28f),
  cs8(4435f, "C♯", "D♭", 28.5f),
  d8(4699f, "D", "D", 29f),
  ds8(4978f, "D♯", "E♭", 29.5f),
  e8(5274f, "E", "E", 30f),
  f8(5588f, "F", "F", 31f),
  fs8(5920f, "F♯", "G♭", 31.5f),
  g8(6272f, "G", "G", 32f),
  gs8(6645f, "G♯", "A♭", 32.5f),
  a8(7040f, "A", "A", 33f),
  as8(7459f, "A♯", "B♭", 33.5f),
  b8(7902f, "B", "B", 34f);

  inline fun chromaticIndex() = ordinal % 12
  inline fun matchesChromatic(note: Note) = chromaticIndex() == note.chromaticIndex()
  inline fun matchesChromaticIndex(chromaticIndex: Int) = chromaticIndex() == chromaticIndex

  inline fun sharpen(sharpCount: Int = 1): Note? = entries.getOrNull(ordinal + sharpCount)
  inline fun flatten(flatCount: Int = 1): Note? = entries.getOrNull(ordinal - flatCount)

  val octave: Int = ordinal / 12

  inline fun natural() = when(ordinal % 12) {
    0 -> true
    1 -> false
    2 -> true
    3 -> false
    4 -> true
    5 -> true
    6 -> false
    7 -> true
    8 -> false
    9 -> true
    10 -> false
    11 -> true
    else -> false
  }

  fun name(unnaturalType: UnnaturalType = UnnaturalType.sharp) = when(unnaturalType) {
    UnnaturalType.sharp -> nameo
    UnnaturalType.flat -> nameFlat
  }

  companion object {
    fun octave(octaveIndex: Int): List<Note> {
      if (octaveIndex < 0) throw IllegalArgumentException()
      if (octaveIndex > 8) throw IllegalArgumentException()

      val startIndex = octaveIndex * 12
      val endIndex = (octaveIndex + 1) * 12

      return Note.entries.subList(startIndex, endIndex)
    }
  }

  enum class UnnaturalType {
    sharp, flat,
  }
}