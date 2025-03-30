import java.io.File
import java.io.ByteArrayInputStream
import javax.sound.sampled.*
import java.nio.file.Paths
import kotlin.math.pow
import kotlin.math.min

// Example usage
fun main(args: Array<String>) {
  val generator = SfzSampleWavGenerator()

  val projectDir = Paths.get("").toAbsolutePath().toString()
  val sfzPath = File("$projectDir/audio-files/sfz/piano-kawai/map.sfz").absolutePath

  val outputDir = File("$projectDir/audio-files/instruments/piano-kawai").absolutePath

  generator.generateWavNotesFromSfz(sfzPath, outputDir, 21..108)
}

class SfzSampleWavGenerator {

  /**
   * Generates WAV files for each semitone from an SFZ file and its WAV samples
   * @param sfzFilePath Path to the SFZ file
   * @param outputDir Directory to save generated WAV files (defaults to same directory as SFZ)
   * @param noteRange Range of MIDI notes to generate (default: 21-108, piano range)
   */
  fun generateWavNotesFromSfz(
    sfzFilePath: String,
    outputDir: String = File(sfzFilePath).parent,
    noteRange: IntRange = 21..108
  ) {
    val sfzFile = File(sfzFilePath)
    if (!sfzFile.exists()) {
      throw IllegalArgumentException("SFZ file not found: $sfzFilePath")
    }

    // Parse SFZ file to extract regions with sample information
    val sfzContent = sfzFile.readText()
    val sfzRegions = parseSfzRegions(sfzContent)

    if (sfzRegions.isEmpty()) {
      println("Warning: No valid regions found in SFZ file")
      return
    }

    // Debug: Print all found regions
    println("Found ${sfzRegions.size} regions in SFZ file:")
    sfzRegions.forEach { region ->
      println("Sample: ${region.samplePath}, Root: ${region.rootKey}, Range: ${region.loKey}-${region.hiKey}")
    }

    // Create output directory if it doesn't exist
    val outputDirectory = File(outputDir)
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs()
    }

    // Generate WAV for each MIDI note in the range
    for (midiNote in noteRange) {
      try {
        val region = findBestRegionForNote(sfzRegions, midiNote)
        if (region != null) {
          generateWavForNote(region, midiNote, sfzFile.parent, outputDirectory)
        } else {
          println("No suitable region found for MIDI note $midiNote")
        }
      } catch (e: Exception) {
        println("Error generating WAV for MIDI note $midiNote: ${e.message}")
        e.printStackTrace()
      }
    }

    println("WAV generation complete. Files saved to: ${outputDirectory.absolutePath}")
  }

  /**
   * Represents a region in the SFZ file
   */
  data class SfzRegion(
    val samplePath: String,
    val rootKey: Int,
    val loKey: Int,
    val hiKey: Int,
    val loVel: Int = 0,
    val hiVel: Int = 127,
    val volume: Double = 0.0
  )

  /**
   * Parses an SFZ file content and extracts regions
   */
  fun parseSfzRegions(sfzContent: String): List<SfzRegion> {
    val regions = mutableListOf<SfzRegion>()
    val regionBlocks = sfzContent.split("<region>").drop(1)

    for (block in regionBlocks) {
      var samplePath = ""
      var rootKey = 60 // Default to middle C
      var loKey = 0
      var hiKey = 127
      var loVel = 0
      var hiVel = 127
      var volume = 0.0

      // Extract parameters from region
      val lines = block.split("\n")
      for (line in lines) {
        val trimmed = line.trim()
        when {
          trimmed.startsWith("sample=") ->
            samplePath = trimmed.substring(7).trim().removeSurrounding("\"")
          trimmed.startsWith("key=") -> {
            rootKey = trimmed.substring(4).trim().toInt()
            loKey = rootKey
            hiKey = rootKey
          }
          trimmed.startsWith("pitch_keycenter=") ->
            rootKey = trimmed.substring(16).trim().toInt()
          trimmed.startsWith("lokey=") ->
            loKey = trimmed.substring(6).trim().toInt()
          trimmed.startsWith("hikey=") ->
            hiKey = trimmed.substring(6).trim().toInt()
          trimmed.startsWith("lovel=") ->
            loVel = trimmed.substring(6).trim().toInt()
          trimmed.startsWith("hivel=") ->
            hiVel = trimmed.substring(6).trim().toInt()
          trimmed.startsWith("volume=") ->
            volume = trimmed.substring(7).trim().toDouble()
        }
      }

      if (samplePath.isNotEmpty()) {
        regions.add(SfzRegion(samplePath, rootKey, loKey, hiKey, loVel, hiVel, volume))
      }
    }

    return regions
  }

  /**
   * Finds the best region to use for a given MIDI note
   */
  fun findBestRegionForNote(regions: List<SfzRegion>, midiNote: Int): SfzRegion? {
    // First, find regions where the note is in range
    val validRegions = regions.filter { midiNote in it.loKey..it.hiKey }

    if (validRegions.isEmpty()) return null

    // Prefer exact key match, then closest root key
    val exactMatch = validRegions.find { it.rootKey == midiNote }
    if (exactMatch != null) return exactMatch

    // Otherwise, return region with closest root key
    return validRegions.minByOrNull { Math.abs(it.rootKey - midiNote) }
  }

  /**
   * Generates WAV file for a specific MIDI note using the given region
   */
  fun generateWavForNote(
    region: SfzRegion,
    midiNote: Int,
    sfzDirectory: String,
    outputDirectory: File
  ) {
    val sampleFile = File(sfzDirectory, region.samplePath)
    if (!sampleFile.exists()) {
      println("Warning: Sample file not found: ${sampleFile.absolutePath}")
      return
    }

    println("Processing MIDI note $midiNote using sample: ${sampleFile.absolutePath}")
    println("Root key: ${region.rootKey}, Semitone difference: ${midiNote - region.rootKey}")

    // Calculate pitch shift ratio
    val semitonesDifference = midiNote - region.rootKey
    val pitchShiftRatio = 2.0.pow(semitonesDifference / 12.0)
    println("Pitch shift ratio: $pitchShiftRatio")

    // Generate output file path
    val noteName = midiNoteToName(midiNote)
    val wavFile = File(outputDirectory, "${noteName}_${midiNote}.wav")

    try {
      // Pitch shift the sample directly to the final WAV file
      pitchShiftSample(sampleFile.absolutePath, wavFile.absolutePath, pitchShiftRatio)

      // Verify the file was created and has content
      if (wavFile.exists() && wavFile.length() > 0) {
        println("Successfully generated WAV for note $noteName (MIDI: $midiNote) - Size: ${wavFile.length()} bytes")
      } else {
        println("Error: Generated WAV file is empty or doesn't exist")
      }
    } catch (e: Exception) {
      println("Error processing sample for note $midiNote: ${e.message}")
      e.printStackTrace()
    }
  }

  /**
   * Pitch shifts a WAV sample by the given ratio with improved handling
   */
  fun pitchShiftSample(inputWavPath: String, outputWavPath: String, pitchRatio: Double) {
    // Open input audio file
    val inputFile = File(inputWavPath)
    val audioInputStream = AudioSystem.getAudioInputStream(inputFile)
    val format = audioInputStream.format

    println("Input audio format: $format")

    // Read all audio data
    val inputBytes = audioInputStream.readAllBytes()
    println("Read ${inputBytes.size} bytes from input WAV")

    val frameSize = format.frameSize
    val inputFrames = inputBytes.size / frameSize

    // Calculate output size
    val outputFrames = (inputFrames / pitchRatio).toInt()
    val outputBytes = ByteArray(outputFrames * frameSize)

    println("Input frames: $inputFrames, Output frames: $outputFrames")

    // Simple resampling with proper bounds checking
    for (outFrame in 0 until outputFrames) {
      val inFrame = (outFrame * pitchRatio).toInt()
      val inOffset = inFrame * frameSize
      val outOffset = outFrame * frameSize

      if (inOffset + frameSize <= inputBytes.size && outOffset + frameSize <= outputBytes.size) {
        System.arraycopy(
          inputBytes, inOffset,
          outputBytes, outOffset,
          frameSize
        )
      }
    }

    println("Processed ${outputBytes.size} bytes for output WAV")

    // Write output file with explicitly constructed AudioInputStream
    val outputAis = AudioInputStream(
      ByteArrayInputStream(outputBytes),
      format,
      outputFrames.toLong()
    )

    try {
      val written = AudioSystem.write(outputAis, AudioFileFormat.Type.WAVE, File(outputWavPath))
      println("Wrote $written bytes to output WAV file")
    } catch (e: Exception) {
      println("Error writing WAV file: ${e.message}")
      throw e
    } finally {
      // Close streams
      outputAis.close()
      audioInputStream.close()
    }
  }

  /**
   * Converts a MIDI note number to a note name (e.g., 60 -> "C4")
   */
  fun midiNoteToName(midiNote: Int): String {
    val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val noteName = noteNames[midiNote % 12]
    val octave = (midiNote / 12) - 1
    return "$noteName$octave"
  }
}