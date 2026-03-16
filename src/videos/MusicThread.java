package videos;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MusicThread extends Thread {


    //音频文件名
    private String filename;
    private AudioFormat audioFormat;
    private byte[] samples;

    private volatile boolean stopped = false;
    private volatile boolean loop = false;

    public MusicThread(String filename) {
        //初始化filename
        this.filename = filename;
        reverseMusic();
    }

    public MusicThread(String filename, boolean loop) {
        this.filename = filename;
        this.loop = loop;
        reverseMusic();
    }
    public void reverseMusic() {
        try {
            //定义一个AudioInputStream用于接收输入的音频数据，使用AudioSystem来获取音频的音频输入流
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filename));
            //用AudioFormat来获取AudioInputStream的格式
            audioFormat = stream.getFormat();
            samples = getSamples(stream);
        } catch (UnsupportedAudioFileException e) {
            System.err.println("不支持的音频格式: " + filename);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("读取音频文件失败: " + filename);
            e.printStackTrace();
        }
    }

    public byte[] getSamples(AudioInputStream stream) {
        int size = (int) (stream.getFrameLength() * audioFormat.getFrameSize());
        byte[] samples = new byte[size];
        DataInputStream dataInputStream = new DataInputStream(stream);
        try {
            dataInputStream.readFully(samples);
        } catch (IOException e) {
            System.err.println("读取音频数据失败");
            e.printStackTrace();
        }
        return samples;
    }

    public void play(InputStream source) {
        int size = (int) (audioFormat.getFrameSize() * audioFormat.getSampleRate());
        byte[] buffer = new byte[size];
        //源数据行SoureDataLine是可以写入数据的数据行
        SourceDataLine dataLine = null;
        //获取受数据行支持的音频格式DataLine.info
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, size);
        } catch (LineUnavailableException e) {
            System.err.println("音频线路不可用");
            e.printStackTrace();
        }
        dataLine.start();

        try {
            do {
                int numBytesRead = 0;
                source = new ByteArrayInputStream(samples); // 重置流以支持循环

                while (!stopped && (numBytesRead = source.read(buffer, 0, buffer.length)) != -1) {
                    if (stopped) break;
                    if (numBytesRead > 0) {
                        dataLine.write(buffer, 0, numBytesRead);
                    }
                }
            } while (loop && !stopped);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        dataLine.drain();
        dataLine.close();

    }

    @Override
    public void run() {
        InputStream stream = new ByteArrayInputStream(samples);
        play(stream);
    }

    public void stopMusic() {
        stopped = true;
        this.interrupt();
    }
}


