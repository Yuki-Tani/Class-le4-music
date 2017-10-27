package tools;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathArrays;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

public class TransformTools {
	
	/*
	 * デシベルに変換
	 */
	public static double toDB(double src) {
		return 20.0 * Math.log10(src);
	}
	
	public static double[] toDB(double[] src) {
		return Arrays.stream(src).map(TransformTools::toDB).toArray();	
	}
	
	/*
	 * 音波をスペクトル(R-Hz)に変換
	 */
	public static double[] toSpectrum(double[] wave) {
		// 2^pとなる配列サイズを求める
		final int fftSize = 1 << Le4MusicUtils.nextPow2(wave.length);
		// 信号の長さをfftSizeに伸ばし、信号長で正規化
		final double[] src = Arrays.stream(Arrays.copyOf(wave, fftSize)).map(w -> w /*/ wave.length*/).toArray();
		// 高速フーリエ変換
		final Complex[] spectrum = Le4MusicUtils.rfft(src);
		return 	Arrays.stream(spectrum).mapToDouble(c -> c.abs()).toArray();	
	}
	
	/*
	 * 音波(width-time)をスペクトル(dB-Hz)に変換
	 */
	public static double[] toSpectrumDB(double[] wave) {
		return toDB(toSpectrum(wave));	
	}
	
	/*
	 * スペクトルの横軸スケール(1メモリ)を算出
	 */
	public static double specScaleX(double[] wave,double sampleRate) {
		return sampleRate / (1 << Le4MusicUtils.nextPow2(wave.length));
	}
	
	/*
	 * 音波(width-time)をスペクトログラム(dB-(Hz-time))に変換
	 */
	public static double[][] toSpectrogram(
			double[] wave,
			int frameSize,
			int shiftSize)
	{
		// 2^pとなる配列サイズを求める
		final int fftSize = 1 << Le4MusicUtils.nextPow2(frameSize);

		// 窓関数と正規化
		final double[] window = MathArrays.normalizeArray(
				Arrays.copyOf(Le4MusicUtils.hanning(frameSize),fftSize),
				1.0
		);

		// 短時間フーリエ変換
		final Stream<Complex[]> spectrogram = 
				Le4MusicUtils.sliding(wave, window, shiftSize)
						.map(frame -> Le4MusicUtils.rfft(frame));
		// デシベル化
		return spectrogram.map(sp -> 
				Arrays.stream(sp).mapToDouble(c -> toDB(c.abs())).toArray()
			).toArray(n -> new double[n][]);
	}
	
	/*
	 * 短時間音波を音量(root-mean-square)に変換
	 */
	public static double getRMSdB(
			double[] miniWave)
	{
		double sum = 0;
		for(int i=0; i<miniWave.length; i++) {
			sum += Math.pow(miniWave[i],2);
		}
		sum = Math.sqrt(sum / miniWave.length);
		return toDB(Math.abs(sum));
	}
	
	/*
	 *  音波を音量(root-mean-square)の列に変換
	 */
	public static double[] toRMSdB(
			double[] wave,
			int frameSize,
			int shiftSize)
	{
		final int fftSize = 1 << Le4MusicUtils.nextPow2(frameSize);
		final double[] window = MathArrays.normalizeArray(
				Arrays.copyOf(Le4MusicUtils.hanning(frameSize),fftSize),
				1.0
		);
		
		double[] rms = 
				Le4MusicUtils.sliding(wave, window, shiftSize).mapToDouble(
				frame -> getRMSdB(frame)).toArray();
		return rms;
	}
	
	/*
	 * 短時間音波を音高(Hz)に変換
	 * (基本周波数の抽出)
	 */
	public static double getPitch(
			double[] miniWave,
			double sampleRate) {
		int phase = 0;
		int maxSlide = 0;
		double max = 0;
		for(int i=0;i<miniWave.length;i++) {
			double sum = 0;
			for(int j=0;j<miniWave.length-i;j++) {
				sum += miniWave[j]*miniWave[j+i]; // 自己相関
			}
			//System.out.println("[slide:"+i+"] "+sum);
			if(phase == 0) { // 第一の山
				if(sum < 0) phase = 1;
			}else if(phase == 1) { // 第一の谷
				if(sum > 0) phase = 2;
			}else {
				if(sum > max) {
					maxSlide = i;
					max = sum;
				}
				if(sum < 0) break;
			}
		}
		if(maxSlide == 0) return 0;
		return sampleRate / maxSlide;
	}
	
	/*
	 * 音波を音高(Hz)の列に変換
	 */
	public static double[] toPitch(
			double[] wave,
			double sampleRate,
			int frameSize,
			int shiftSize)
	{
		/*
		final double[] window = MathArrays.normalizeArray(
				Le4MusicUtils.hanning(frameSize),
				1.0
		);
		*/
		double[] pitch = 
				Le4MusicUtils.sliding(wave, frameSize , shiftSize).mapToDouble(
				frame -> getPitch(frame,sampleRate)).toArray();
		return pitch;
	}
	
	/*
	 * ケプストラム係数群を抽出
	 */
	public static double[] getCepstrum(double[] miniWave) {
		return getCepstrum(miniWave,Constants.CEPSTRUM_DIMENSION);
	}
	public static double[] getCepstrum(
			double[] miniWave,
			int dimension)
	{
		// スペクトル変換
		double[] spectrum = toSpectrum(miniWave);
		// ケプストラム抽出
		double[] srcLog = Arrays.stream(spectrum).map(p -> Math.log(p)).toArray();
		int fftSize = 1 << Le4MusicUtils.nextPow2(srcLog.length);
		final double[] input = Arrays.stream(Arrays.copyOf(srcLog, fftSize)).map(w -> w /* /src.length */).toArray();		
		Complex[] cepstrum = Le4MusicUtils.rfft(input);
		// 次元を絞る
		double[] output = new double[dimension];
		for(int i=0; i<dimension; i++) {
			output[i] = cepstrum[i].abs();
		}
		return output;
	}
	
	/*
	 * 低周波成分の抽出
	 */
	public static double[] toEnvelope(double[] src) {
		return toEnvelope(src,Constants.CEPSTRUM_DIMENSION);
	}
	public static double[] toEnvelope(
			double[] src,
			int dimension)
	{
		double[] srcLog = Arrays.stream(src).map(p -> Math.log(Math.abs(p))).toArray();
		
		int fftSize = 1 << Le4MusicUtils.nextPow2(srcLog.length);
		final double[] input = Arrays.stream(Arrays.copyOf(srcLog, fftSize)).toArray();
		
		Complex[] cepstrum = Le4MusicUtils.rfft(input);
		//System.out.println("cepstrum.length" + cepstrum.length);
		
		for(int i=dimension; i<cepstrum.length; i++) {
			cepstrum[i] = Complex.ZERO;
		}
		
		double[] output = Arrays.copyOf(Le4MusicUtils.irfft(cepstrum),src.length);
		double[] dst = Arrays.stream(output).map(p -> Math.exp(p)).toArray();
		//System.out.println("cepstrum.length" + dst.length);
		return dst;
	}

	/*
	 * 有声音かどうかを判定
	 */
	public static boolean isVoicedSound(double[] miniWave,double sampleRate){
		return isVoicedSound(miniWave,sampleRate,getPitch(miniWave,sampleRate));
	}
	public static boolean isVoicedSound(
			double[] miniWave,
			double sampleRate,
			double pitch) {
		// ゼロ交差数を数える
		int count = 0;
		boolean plus = (miniWave[0]>0);
		for(int i=1;i<miniWave.length; i++) {
			if(plus && miniWave[i]<0) {
				count ++;
				plus = false;
			}else if(!plus && miniWave[i]>0) {
				count ++;
				plus = true;
			}
		}
		
		count = (int)(count * sampleRate / miniWave.length);
		
		// ゼロ交差数が基本周波数の約２倍かどうかを判定
		return pitch * 2 - Constants.MARGIN_OF_VOICESOUND < count 
				&& count < pitch *2 + Constants.MARGIN_OF_VOICESOUND ;
	}
	
	/*
	 * 有声音のみ通すフィルターを作成
	 */
	public static double[] getVoicedSoundFilter(
			double[] wave,
			double sampleRate,
			int frameSize,
			int shiftSize)
	{
		double[] filter =
				Le4MusicUtils.sliding(wave, frameSize, shiftSize).mapToDouble(
						frame -> (isVoicedSound(frame,sampleRate,getPitch(frame,sampleRate)))? 1.0 : 0
				).toArray();
		return filter;
	}
	
	/*
	 * フィルターの適応
	 */
	public static double[] applyFilter(double[] origin, double[] filter) {
		double[] filteredWave = new double[origin.length];
		double changeScale = 1.0 * origin.length / filter.length;
		for(int i=0;i<origin.length;i++) {
			filteredWave[i] = origin[i] * filter[(int)(i / changeScale)];
		}
		return filteredWave;
	}
}