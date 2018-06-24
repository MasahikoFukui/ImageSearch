package jp.fukui.imagesearch.app;

import java.util.*;
import java.io.*;
import javafx.scene.image.*;

/** ファイル検索クラス */
class FileSearcher {
	//------------------------------------------------------------------------------
	// オブザーバー
	//------------------------------------------------------------------------------

	/**
	 * ファイル検索のオブザーバインタフェース
	 */
	public interface Observer {
		public void hits(File file, Image img);
		public void searching(File file);
		public void finished();
	}

	/** オブザーバ */
	private Observer o = null;
	/** オブザーバ設定 */
	public void setObserver(Observer o) { this.o = o; }

	//------------------------------------------------------------------------------
	// 検索結果
	//------------------------------------------------------------------------------

	/** 一致したファイルリスト */
	private ArrayList<File> hitList = new ArrayList<File>();
	/** 一致したファイルリストのクリア */
	private void clearHitList() { this.hitList.clear(); }
	/** 一致したファイルリストへの追加 */
	private void addHitList(File f) { this.hitList.add(f); }
	/** 一致したファイルリストの参照 */
	public ArrayList<File> getHitList() { return(this.hitList); }

	//------------------------------------------------------------------------------
	// 検索先
	//------------------------------------------------------------------------------

	/** 検索先ディレクトリ */
	private File directory = null;
	/** 検索先ディレクトリの参照 */
	public void setDirectory(File dir) { this.directory = dir; }
	/** 検索先ディレクトリの参照 */
	public File getDirectory() { return(this.directory); }

	//------------------------------------------------------------------------------
	// ソース画像のファイル
	//------------------------------------------------------------------------------

	/** 画像ファイル */
	private File sourceFile = null;
	/** 画像ファイルの設定 */
	public void setSourceFile(File src) { this.sourceFile = src; }
	/** 画像ファイルの参照 */
	public File getSourceFile() { return(this.sourceFile); }

	//------------------------------------------------------------------------------
	// ソース画像の正規化後のピクセル値
	//------------------------------------------------------------------------------

	/** 正規化後のピクセル値 */
	int[][] sourcePix = null;
	/** 正規化後のピクセル値の設定 */
	public void setSourcePix(int[][] pix) { this.sourcePix = pix; }
	/** 正規化後のピクセル値の参照 */
	public int[][] getSourcePix() { return(this.sourcePix); }

	//------------------------------------------------------------------------------
	// 閾値
	//------------------------------------------------------------------------------

	/** 閾値 */
	private double threshold = 0;
	/** 閾値の設定 */
	public void setThreshold(double th) { this.threshold = th; }
	/** 閾値の参照 */
	public double getThreshold() { return(this.threshold); }

	//------------------------------------------------------------------------------
	// 検索中断
	//------------------------------------------------------------------------------

	/** 検索中断フラグ */
	private boolean breakFlag = false;
	/** 検索中断 */
	public void setInterrupt() { this.breakFlag = true; }
	/** 検索中断解除 */
	protected void resetInterrupt() { this.breakFlag = false; }
	/** 検索中断の確認 */
	protected boolean isInterrupted() { return(this.breakFlag); }

	//------------------------------------------------------------------------------
	// 検索
	//------------------------------------------------------------------------------

	/** 検索 */
	public void search() {
		resetInterrupt();
		clearHitList();

		Image srcImg = new Image(getSourceFile().toURI().toString());
		setSourcePix(normalizeImage(srcImg));
		search(getDirectory(), srcImg, getThreshold());

		if(this.o != null)
			this.o.finished();
	}

	public void search(File file, Image srcImg, double differency) {
		if(isInterrupted())
			return;

		if(file.isFile()) {
			try {
				String fileName = file.getName().toLowerCase();
				if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
					if(this.o != null)
						this.o.searching(file);

					Image img = new Image(file.toURI().toString());
					int w1 = (int) srcImg.getWidth();
					int h1 = (int) srcImg.getHeight();
					int w2 = (int) img.getWidth();
					int h2 = (int) img.getHeight();
					// 元画像の縦横比の違いが１０％以内ならばサイズは一致とみなす
					if(((double) (w1*h2) / (h1*w2)) > 0.9 && ((double) (h1*w2) / (w1*h2)) > 0.9) {
						// 正規化後の画像の比較
						int[][] pix = normalizeImage(img);
						if(compare(getSourcePix(), pix, differency)) {
							addHitList(file);
							if(this.o != null)
								this.o.hits(file, img);
						}
					}
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		} else {
			for(File f : file.listFiles()) {
				if(isInterrupted())
					return;
				search(f, srcImg, differency);
			}
		}
	}

	//------------------------------------------------------------------------------
	// 画像の正規化
	//------------------------------------------------------------------------------

	/** 画像の正規化サイズ */
	private static final int SIZE = 128;

	/** 画像の正規化 */
	protected int[][] normalizeImage(Image img) {
		PixelReader rd = img.getPixelReader();

		int w = (int) img.getWidth();
		int h = (int) img.getHeight();
		int[][] pix = new int[SIZE][SIZE];

		for(int y = 0; y < SIZE; y++) {
			for(int x = 0; x < SIZE; x++) {
				int x1 = (int) (w * x     / ((double) SIZE) + 0.5D);
				int x2 = (int) (w * (x+1) / ((double) SIZE) + 0.5D);
				int y1 = (int) (h * y     / ((double) SIZE) + 0.5D);
				int y2 = (int) (h * (y+1) / ((double) SIZE) + 0.5D);

				//  256x256未満の画像対策
				if(x1 == x2)
					x2 = x1+1;
				if(y1 == y2)
					y2 = y1+1;

				pix[y][x] = 0;
				for(int _y = y1; _y < y2; _y++) {
					for(int _x = x1; _x < x2; _x++) {
						int p = rd.getArgb(_x, _y);
						p = (((p >>> 16) & 0x00FF) + ((p >>> 8) & 0x00FF) + ((p >>> 0) & 0x00FF)) / 3;
						pix[y][x] += p;
					}
				}
				pix[y][x] /= (x2 - x1) * (y2 - y1);
			}
		}
		return(pix);
	}

	//------------------------------------------------------------------------------
	// 画像の比較
	//------------------------------------------------------------------------------

	/** 画像の比較 */
	public boolean compare(int[][] pix1, int[][] pix2, double differency) {
		// 1ライン分の移動平均
		int[] a1 = new int[SIZE-2];
		int[] a2 = new int[SIZE-2];

		double eH = 0;
		double eV = 0;

		// 差異（明度）
		double e1;
		// 差異（明度の縦横方向の変化量）
		double e2;

		// 横方向の画像の差異を計算
		e1 = 0;
		e2 = 0;
		for(int y = 0; y < SIZE; y++) {
			for(int x = 0; x < SIZE-2; x++) {
				a1[x] = ((pix1[y][x] + pix1[y][x+1] + pix1[y][x+2]) / 3);
				a2[x] = ((pix2[y][x] + pix2[y][x+1] + pix2[y][x+2]) / 3);
				e1 += Math.abs(a1[x] - a2[x]);
			}
			for(int x = 0; x < SIZE-3; x++) {
				e2 += Math.abs((a1[x] - a1[x+1]) - (a2[x] - a2[x+1]));
			}
		}
		e1 = e1 / ((SIZE) * (SIZE-2));
		e2 = e2 / ((SIZE) * (SIZE-3));
		eH += Math.sqrt(e1*e2);

		// 縦方向の画像の差異を計算
		e1 = 0;
		e2 = 0;
		for(int x = 0; x < SIZE; x++) {
			for(int y = 0; y < SIZE-2; y++) {
				a1[y] = ((pix1[y][x] + pix1[y+1][x] + pix1[y+2][x]) / 3);
				a2[y] = ((pix2[y][x] + pix2[y+1][x] + pix2[y+2][x]) / 3);
				e1 += Math.abs(a1[y] - a2[y]);
			}
			for(int y = 0; y < SIZE-3; y++) {
				e2 += Math.abs((a1[y] - a1[y+1]) - (a2[y] - a2[y+1]));
			}
		}
		e1 = e1 / ((SIZE) * (SIZE-2));
		e2 = e2 / ((SIZE) * (SIZE-3));
		eV += Math.sqrt(e1*e2);

		return((eH + eV)/2 <= differency);
	}
}
