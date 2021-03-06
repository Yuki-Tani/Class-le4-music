package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

public class SpeechRecognizer {
	public static final int A = 1, I = 2, U = 3, E = 4, O = 5;
	private boolean prepared = false;
	private double[][] mean, dispersion; 
	
	
	/*
	 * 教師ファイルを作成(.wav -> vowel.tch)
	 */
	public static void main(String[] args) {
		if(args.length != 5) {
			System.out.println("please give 5 teacher wav files - A,I,U,E,O");
			return;
		}
		double[][] teacherDatas = new double[10][Constants.CEPSTRUM_DIMENSION];
		try {
		// AIUEOそれぞれのファイルで学習
		for(int i=0; i<5; i++) {
			System.out.print("...file("+(i+1)+"): ");
			// wavファイル読み込み
			File wavFile = new File(args[i]);
			AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
			double[] wave = Le4MusicUtils.readWaveformMonaural(stream);
			AudioFormat format = stream.getFormat();
			double sampleRate = format.getSampleRate();
			stream.close();
			
			double frameDuration = Le4MusicUtils.frameDuration;
			double shiftDuration = frameDuration / 8.0;
			int frameSize = (int) Math.round(frameDuration * sampleRate);
			int shiftSize = (int) Math.round(shiftDuration * sampleRate);
			
			// ケプストラム抽出
			ArrayList<double[]> cepstrum = new ArrayList<>();
			Le4MusicUtils.sliding(wave, frameSize , shiftSize).forEach(
					frame -> {
						// 有声音のデータだけ抽出
//						if(TransformTools.isVoicedSound(wave, sampleRate)) {
							double[] ceps = TransformTools.getCepstrum(frame);
							if(!Double.isInfinite(ceps[0])) cepstrum.add(ceps);
//						}
					}
			);
			// データの平均を計算
			double[] mean = new double[Constants.CEPSTRUM_DIMENSION];
			for(int k=0; k<cepstrum.size(); k++) {
				double[] ceps = cepstrum.get(k);
				for(int j=0;j<ceps.length;j++) {
					mean[j] += ceps[j]/cepstrum.size();
				}
			}			
			//データの分散を計算
			double[] dispersion = new double[Constants.CEPSTRUM_DIMENSION];
			for(int k=0; k<cepstrum.size(); k++) {
				double[] ceps = cepstrum.get(k);
				for(int j=0;j<ceps.length;j++) {
					dispersion[j] += Math.pow((ceps[j]-mean[j]),2)/cepstrum.size();
				}
			}
			teacherDatas[i*2] = mean;
			teacherDatas[i*2+1] = dispersion;
			System.out.println("complete");
		}
			
		// データを書き込み
		BufferedWriter out = new BufferedWriter(
				new FileWriter(new File(Constants.SPEECH_RECOGNITION_TEACHER_FILE)));
		for(int i=0;i<teacherDatas.length; i++) {
			double[] data = teacherDatas[i];
			for(int j=0; j<data.length; j++) {
				out.write(data[j]+" ");
			}
			out.newLine();
		}
			
		out.close();
		System.out.println("finish.");
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("File reading error!");
		}
	}
	
	/*
	 * 音声認識器の作成
	 */
	public SpeechRecognizer() {this(Constants.SPEECH_RECOGNITION_TEACHER_FILE);}
	public SpeechRecognizer(String teacherFile) {
		//　認識モデル読み込み(vowel.tch -> data)
		mean = new double[6][Constants.CEPSTRUM_DIMENSION];
		dispersion = new double[6][Constants.CEPSTRUM_DIMENSION];
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(teacherFile)));
			for(int i=1;i<=5;i++) {
				double[] valuesM = Arrays.stream(in.readLine().trim().split(" "))
						.mapToDouble(v -> Double.valueOf(v))
						.toArray();
				double[] valuesD = Arrays.stream(in.readLine().trim().split(" "))
						.mapToDouble(v -> Double.valueOf(v))
						.toArray();
				if(valuesM.length == Constants.CEPSTRUM_DIMENSION
						&& valuesD.length == Constants.CEPSTRUM_DIMENSION) {
					mean[i] = valuesM;
					dispersion[i] = valuesD;
				}else {
					in.close();
					throw new Exception("data dimension must be "+Constants.CEPSTRUM_DIMENSION);
				}
			}
			in.close();
			prepared = true;
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("file read error!");
		}
	}
	
	/*
	 * 短時間の音波を音声認識する
	 * @return 母音を表す整数(SpeechRecognizer.A,I,U,E,O)
	 */
	public double recognize(double[] miniWave) {
		if(!prepared) {
			System.out.println("SpeechRecognizer is NOT prepared!");
			return 0;
		}
		double[] cepstrum = TransformTools.getCepstrum(miniWave);
		double max = Double.NEGATIVE_INFINITY;
		double maxVowel = 0;
		double likelihood = calcLikelihood(cepstrum,A);
		if(likelihood > max) {
			max = likelihood;
			maxVowel = A;
		}
		likelihood = calcLikelihood(cepstrum,I);
		if(likelihood > max) {
			max = likelihood;
			maxVowel = I;
		}
		likelihood = calcLikelihood(cepstrum,U);
		if(likelihood > max) {
			max = likelihood;
			maxVowel = U;
		}
		likelihood = calcLikelihood(cepstrum,E);
		if(likelihood > max) {
			max = likelihood;
			maxVowel = E;
		}
		likelihood = calcLikelihood(cepstrum,O);
		if(likelihood > max) {
			max = likelihood;
			maxVowel = O;
		}
		return maxVowel;
 	}
	
	/*
	 * 音波を母音の列に変換
	 */
	public double[] toSpeach(double[] wave, double sampleRate, int frameSize, int shiftSize) {
		double[] speach = 
				Le4MusicUtils.sliding(wave, frameSize , shiftSize).mapToDouble(frame ->
				// 無声音,雑音は0とする
					(//TransformTools.isVoicedSound(frame, sampleRate) &&
						TransformTools.getRMSdB(frame)>Constants.LOUDNESS_FILTER)? 
							recognize(frame):
							0
				).toArray();
		return speach;
	}
	
	/*
	 * 母音に対する特徴ベクトルの尤度を計算
	 */
	private double calcLikelihood(double[] cepstrum, int vowel) {
		if(vowel != A && vowel != I &&vowel != U &&vowel != E &&vowel != O) {
			return 0;
		}
		return calcNomalDistribution(cepstrum, mean[vowel], dispersion[vowel]);
	}
	
	/*
	 * 正規分布(簡易)の確率密度関数から値を算出
	 */
	private double calcNomalDistribution(double[] x, double[] mean, double[] dispersion) {
		
		double logsum = 0;
		
		for (int i = 0; i < mean.length; i++) {
			logsum -= Math.log(Math.sqrt(dispersion[i]))
					+Math.pow(x[i]-mean[i], 2)/(2*dispersion[i]);
		}
		return logsum;
		
		/*
		double powSum = 0;
		double disPdt = 1;
		
		for(int i=0; i<mean.length; i++) {
			powSum += Math.pow((x[i]-mean[i]),2) /(2 * dispersion[i]);
			disPdt *= Math.sqrt(dispersion[i]);
		}
		
		return Math.exp(-powSum) / (Math.pow(2*Math.PI,mean.length/2.0) * disPdt);	
		*/
	}
	
	
}
