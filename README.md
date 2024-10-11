# JavaSound Enhancement Project - Service Provider Interface

JSE-SPI provides extended audio file format support for the Java Platform, through plugins for the `javax.sound.*` package.

The main goal of this project is to provide support for formats not covered by the JRE itself. 
Support for these formats is important, to be able to read data found "in the wild", as well as to maintain access to data in legacy formats.
As there is lots of modern data out there, we see the need for open implementations of readers for popular formats.

Note that it's *NOT* recommended to use codecs bundled in this library on non-standard JREs such as Android Runtime. 
Use solutions more suitable for these platforms!

## API
- [JavaSound API](https://www.oracle.com/java/technologies/java-sound-api.html)
- [JavaSound Enhancement Project API](https://github.com/jseproject/jse-api)

### Basic Usage
```java
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class PlaybackExample {
    
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        // Step 1. get the encoded AudioInputStream
        try (AudioInputStream encodedInputStream = AudioSystem.getAudioInputStream(new File("/path/to/your/audio"))) {

            // Step 2. decode the input AudioInputStream
            AudioFormat encodedFormat = encodedInputStream.getFormat();
            AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, encodedFormat.getSampleRate(), 16,
                            encodedFormat.getChannels(), 2 * encodedFormat.getChannels(), encodedFormat.getFrameRate(),
                            encodedFormat.isBigEndian(), encodedFormat.properties()),
                    encodedInputStream);

            // Step 3. resample for playback
            AudioFormat decodedFormat = decodedInputStream.getFormat();
            AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, decodedFormat.getSampleSizeInBits(),
                            decodedFormat.getChannels(), decodedFormat.getFrameSize(), 44100,
                            decodedFormat.isBigEndian(), decodedFormat.properties()),
                    decodedInputStream);

            // Step 4. playback
            AudioFormat playbackFormat = playbackInputStream.getFormat();
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(playbackFormat);
            sourceDataLine.open();
            sourceDataLine.start();
            byte[] buffer = new byte[44100];
            for (int read = playbackInputStream.read(buffer); read != -1; read = playbackInputStream.read(buffer)) {
                sourceDataLine.write(buffer, 0, read);
            }
            sourceDataLine.drain();
            sourceDataLine.close();

        }

    }
    
}
```

## SPI
### Sampled
<table>
<tr>
<th>Module</th>
<th>Format</th>
<th>Encoder</th>
<th>Decoder</th>
<th>Channel</th>
<th>Sample Rate (kHz)</th>
<th>Codec PCM</th>
<th>File Types<br/>Container Formats</th>
</tr>
<tr>
<td>jse-spi-flac</td>
<td>FLAC</td>
<td>YES</td>
<td>YES</td>
<td>1-8</td>
<td>0.001-48</td>
<td>u8, s16, u24</td>
<td><ul><li>FLAC (.flac)</li><li>Ogg (.ogg)</li></ul></td>
</tr>
<tr>
<td>jse-spi-opus</td>
<td>Opus</td>
<td>YES</td>
<td>YES</td>
<td>1-2</td>
<td>8, 12, 16, 24, 48</td>
<td>s16</td>
<td><ul><li>Ogg (.opus, .ogg)</li></ul></td>
</tr>
<tr>
<td>jse-spi-vorbis</td>
<td>Vorbis</td>
<td>YES</td>
<td>YES</td>
<td>1-2</td>
<td>8, 11.025, 12, 16, 22.05, 24, 32, 44.1, 48</td>
<td>s16</td>
<td><ul><li>Ogg (.ogg)</li></ul></td>
</tr>
<tr>
<td rowspan="3">jse-spi-speex</td>
<td>Speex-NB</td>
<td>YES</td>
<td>YES</td>
<td>1-2</td>
<td>8</td>
<td>s16</td>
<td rowspan="3"><ul><li>Ogg (.spx, .ogg)</li></ul></td>
</tr>
<tr>
<td>Speex-WB</td>
<td>YES</td>
<td>YES</td>
<td>1-2</td>
<td>16</td>
<td>s16</td>
</tr>
<tr>
<td>Speex-UWB</td>
<td>YES</td>
<td>YES</td>
<td>1-2</td>
<td>32</td>
<td>s16</td>
</tr>
<tr>
<td rowspan="3">jse-spi-mp3</td>
<td>MP3</td>
<td></td>
<td>YES</td>
<td>1-2</td>
<td rowspan="3">8, 11.025, 12, 16, 22.05, 24, 32, 44.1, 48</td>
<td>s16</td>
<td><ul><li>MP3 (.mp3, .mpg)</li></ul></td>
</tr>
<tr>
<td>MP2</td>
<td></td>
<td>PART</td>
<td>1-2</td>
<td>s16</td>
<td><ul><li>MP2 (.mp2, .mpg)</li></ul></td>
</tr>
<tr>
<td>MP1</td>
<td></td>
<td>PART</td>
<td>1-2</td>
<td>s16</td>
<td><ul><li>MP1 (.mp1, .mpg)</li></ul></td>
</tr>
<tr>
<td rowspan="3">jse-spi-aac</td>
<td>AAC LC</td>
<td></td>
<td>YES</td>
<td>1-8</td>
<td rowspan="3">8, 11.025, 12, 16, 22.05, 24, 44.1, 48, 64, 88.2, 96</td>
<td>s16</td>
<td rowspan="3"><ul><li>ADTS raw AAC (.aac)</li></ul></td>
</tr>
<tr>
<td>HE-AACv1<br/>(AAC+)</td>
<td></td>
<td>YES</td>
<td>1-8</td>
<td>s16</td>
</tr>
<tr>
<td>HE-AACv2<br/>(enhanced<br/>AAC+)</td>
<td></td>
<td>YES</td>
<td>1-8</td>
<td>s16</td>
</tr>
</table>

## Installing
### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.jseproject</groupId>
        <artifactId>jse-spi-flac</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.jseproject</groupId>
        <artifactId>jse-spi-opus</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.jseproject</groupId>
        <artifactId>jse-spi-vorbis</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.jseproject</groupId>
        <artifactId>jse-spi-speex</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.jseproject</groupId>
        <artifactId>jse-spi-mp3</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.jseproject</groupId>
        <artifactId>jse-spi-aac</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```
### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.jseproject:jse-spi-flac:1.0.0'
    implementation 'io.github.jseproject:jse-spi-opus:1.0.0'
    implementation 'io.github.jseproject:jse-spi-vorbis:1.0.0'
    implementation 'io.github.jseproject:jse-spi-speex:1.0.0'
    implementation 'io.github.jseproject:jse-spi-mp3:1.0.0'
    implementation 'io.github.jseproject:jse-spi-aac:1.0.0'
}
```

## License
| Module         | License                             |
|----------------|-------------------------------------|
| jse-spi-flac   | Xiph.Org Variant of the BSD License |
| jse-spi-opus   | Xiph.Org Variant of the BSD License |
| jse-spi-vorbis | Xiph.Org Variant of the BSD License |
| jse-spi-speex  | Xiph.Org Variant of the BSD License |
| jse-spi-mp3    | LGPL-2.1                            |
| jse-spi-aac    | BSD 2-Clause                        |

### Dependencies
<table>
<tr>
<th>Module</th>
<th>Dependency</th>
<th>License</th>
<th>Compile</th>
<th>Runtime</th>
</tr>
<tr>
<td>ALL</td>
<td><a href="https://github.com/jseproject/jse-api">JavaSound Enhancement Project API</a></td>
<td>BSD 3-Clause</td>
<td>YES</td>
<td>YES</td>
</tr>
<tr>
<td>ALL</td>
<td><a href="http://www.tritonus.org">Tritonus</a> Share</td>
<td>LGPL-2.1</td>
<td>YES</td>
<td>YES</td>
</tr>
<tr>
<td>jse-spi-flac</td>
<td rowspan="5"><a href="https://github.com/Gagravarr/VorbisJava">VorbisJava</a> Core</td>
<td rowspan="5">Apache-2.0</td>
<td rowspan="5">YES</td>
<td rowspan="5">YES</td>
</tr>
<tr>
<td>jse-spi-opus</td>
</tr>
<tr>
<td>jse-spi-vorbis</td>
</tr>
<tr>
<td>jse-spi-speex</td>
</tr>
</table>
