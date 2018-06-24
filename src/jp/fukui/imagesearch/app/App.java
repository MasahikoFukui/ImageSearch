package jp.fukui.imagesearch.app;

import java.io.*;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.application.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.beans.value.*;

/**
 * 類似画像検索アプリケーションクラス
 */
public class App extends Application {
	//------------------------------------------------------------------------------
	// 初期化
	//------------------------------------------------------------------------------

	/** 初期化 */
	@Override public void start(Stage primaryStage) {
		try {
			// ステージの表示
			openAppStage(primaryStage);

			// 検索中の通知処理指定
			getSearcher().setObserver(new FileSearcher.Observer() {
				public void finished()                 { searchFinished(); }
				public void searching(File file)       { searchNext(file); }
				public void hits(File file, Image img) { searchHits(file, img); }
			});

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//------------------------------------------------------------------------------
	// アプリケーション画面
	//------------------------------------------------------------------------------

	/** アプリケーション画面を開く */
	public void openAppStage(Stage stage) {
		try {
			setAppStage(stage);

			// タイトルセット
			getAppStage().setTitle("類似画像検索");

			// シーンの生成
			FXMLLoader loader = new FXMLLoader(getClass().getResource("App.fxml"));
			Scene scene = new Scene(loader.load());
			setAppScene(scene);

			// JavaFXコントローラの生成
			setAppController(loader.getController());

			// ステージクローズ時の処理ハンドラ定義
			getAppStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent e) { appClose(); }
			});

			// 検索先パス変更時の処理ハンドラ定義
			getAppController().getDirectory().textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					directoryChangedHndl(newValue);
				}
			});

			// 差異値変更時の処理ハンドラ定義
			getAppController().getDifferency().valueProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					differencyChangedHndl(newValue.intValue());
				}
			});

			// 検索先の初期設定
			File dir = getPreference().getDirectory();
			if(dir == null)
				dir = new File(".");
			getAppController().getDirectory().setText(dir.getAbsolutePath());
			getSearcher().setDirectory(dir);

			// 検索画像の初期設定
			File  src    = getPreference().getSourceImage();
			Image srcImg = null;
			if(src != null)
				srcImg = new Image(src.toURI().toString());
			getAppController().getThumbnail().setImage(srcImg);
			getSearcher().setSourceFile(src);
			getAppController().getStart().setDisable(src == null);
			getAppController().getStop().setDisable(true);

			// 差異値の初期設定
			int differency = getPreference().getDifferency();
			getAppController().getDifferency().setValue(differency);
			getSearcher().setThreshold(differency * 1.0D);
			getAppStage().setMinWidth(getAppController().getTop().getMinWidth());
			getAppStage().setMinHeight(getAppController().getTop().getMinHeight());

			if(getSearcher().getSourceFile() != null && getSearcher().getSourceFile().exists())
				getAppController().getDropMessage().setVisible(false);

			// FXML由来のイベントハンドラの設定
			getAppController().setObserver(new AppController.Observer() {
				@Override public void folderDropped(DragEvent e)      { folderDroppedHndl(e); }
				@Override public void folderDragOver(DragEvent e)     { folderDragOverHndl(e); }
				@Override public void directoryKeyPressed(KeyEvent e) { directoryKeyPressedHndl(e); }
				@Override public void referenceClicked(ActionEvent e) { referenceClickedHndl(e); }
				@Override public void fileDropped(DragEvent e)        { fileDroppedHndl(e); }
				@Override public void fileDragOver(DragEvent e)       { fileDragOverHndl(e); }
				@Override public void fileClicked(MouseEvent e)       { fileClickedHndl(e); }
				@Override public void resultClicked(MouseEvent e)     { resultClickedHndl(e); }
				@Override public void startClicked(ActionEvent e)     { startClickedHndl(e); }
				@Override public void stopClicked(ActionEvent e)      { stopClickedHndl(e); }
				@Override public void closeMenu(ActionEvent e)        { closeMenuHndl();  }
				@Override public void aboutMenu(ActionEvent e)        { aboutMenuHndl();  }
			});

			// ステージの表示
			getAppStage().setScene(scene);
			getAppStage().show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//------------------------------------------------------------------------------
	// アバウト画面
	//------------------------------------------------------------------------------

	/** アバウト画面を開く */
	public void openAboutStage() {
		try {
			setAboutStage(new Stage());

			// タイトルセット
			getAboutStage().setTitle("類似画像検索");

			// シーンの生成
			FXMLLoader loader = new FXMLLoader(getClass().getResource("About.fxml"));
			Scene scene = new Scene(loader.load());

			// JavaFXコントローラの生成
			setAboutController(loader.getController());

			// ステージクローズ時の処理ハンドラ定義
			getAboutStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent e) { appClose(); }
			});

			// FXML由来のイベントハンドラの設定
			getAboutController().setObserver(new AboutController.Observer() {
				@Override public void closeClicked(ActionEvent e) { aboutCloseClickedHndl();  }
			});

			// ステージの表示
			getAboutStage().setScene(scene);
			getAboutStage().setResizable(false);
			getAboutStage().initModality(Modality.WINDOW_MODAL);
			getAboutStage().initOwner(getAppScene().getWindow());
			getAboutStage().show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** アバウト画面を閉じる */
	private void closeAboutStage() {
		getAboutStage().close();
		setAboutStage(null);
	}

	//------------------------------------------------------------------------------
	// 画像表示画面
	//------------------------------------------------------------------------------

	/** 画像表示画面を開く */
	class ImageWindow {
		private Stage stage;

		public ImageWindow(File f) {
			Image img = new Image(f.toURI().toString());
			ImageView v = new ImageView(img);

			v.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					close();
				}
			});

			ScrollPane pane = new ScrollPane();
			pane.setContent(v);

			Scene scene = new Scene(pane, img.getWidth(), img.getHeight());

			this.stage = new Stage();
			this.stage.setTitle(f.getPath());
			this.stage.setScene(scene);
			this.stage.show();
		}

		public void close() {
			this.stage.close();
		}
	}

	//------------------------------------------------------------------------------
	// 通知ハンドラ
	//------------------------------------------------------------------------------

	/** 参照ボタンクリック */
	public void referenceClickedHndl(ActionEvent e) {
		File dir = getSearcher().getDirectory();
		if(dir.exists() == false)
			dir = new File(".");

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("検索先");
		chooser.setInitialDirectory(dir);
		dir = chooser.showDialog(null);
		if(dir != null) {
			getSearcher().setDirectory(dir);
			getAppController().getDirectory().setText(dir.getPath());
			getAppController().getDirectory().setStyle("-fx-background-color: white;");
			getPreference().setDirectory(dir);
		}
	}

	/** 検索ファイルのドラッグオーバー */
	public void fileDragOverHndl(DragEvent e) {
		if(e.getDragboard().hasFiles() && e.getDragboard().getFiles().size() == 1) {
			File f = e.getDragboard().getFiles().get(0);
			if(isImageFile(f))
				e.acceptTransferModes(TransferMode.COPY);
		}
	}

	/** 検索ファイルにファイルをドロップ */
	public void fileDroppedHndl(DragEvent e) {
		File f = e.getDragboard().getFiles().get(0);

		try {
			getAppController().getStart().setDisable(false);
			getAppController().getStop().setDisable(true);
			Image img = new Image(f.toURI().toString());
			getAppController().getThumbnail().setImage(img);
			this.getSearcher().setSourceFile(f);
			getAppController().getResults().getItems().clear();
			getAppController().getSearching().setText("");
			getAppController().getDropMessage().setVisible(false);
			getPreference().setSourceImage(f);
		}
		catch(Exception ex) {
			getAppController().getStart().setDisable(true);
			getAppController().getStop().setDisable(true);
			ex.printStackTrace();
		}
	}

	/** 検索先パスへのフォルダドラッッグオーバー */
	public void folderDragOverHndl(DragEvent e) {
		if(e.getDragboard().hasFiles() && e.getDragboard().getFiles().size() == 1) {
			File f = e.getDragboard().getFiles().get(0);
			if(f.exists())
				e.acceptTransferModes(TransferMode.COPY);
		}
	}

	/** 検索先パスへのフォルダドロップ */
	public void folderDroppedHndl(DragEvent e) {
		File f = e.getDragboard().getFiles().get(0);
		if(f.isFile())
			f = f.getParentFile();
		getAppController().getDirectory().setText(f.getPath());
	}

	/** 検索開始ボタンのクリック */
	public void startClickedHndl(ActionEvent e) {
		getAppController().getSearching().setText("");
		getAppController().getStart().setDisable(true);
		getAppController().getStop().setDisable(false);
		getAppController().getOpPanel().setDisable(true);

		getAppController().getResults().getItems().clear();

		startSearchThread();
	}

	/** 中断ボタンのクリック */
	public void stopClickedHndl(ActionEvent e) {
		stopSearchThread();
	}

	/** 検索ファイルのクリック */
	public void fileClickedHndl(MouseEvent e) {
		File file = getSearcher().getSourceFile();
		File dir  = new File(".");
		if(file != null && file.exists())
			dir = file.getParentFile();

		FileChooser chooser = new FileChooser();
		chooser.setTitle("検索する画像");
		chooser.setInitialDirectory(dir);
		chooser.setInitialFileName(file.getName());
		file = chooser.showOpenDialog(null);
		if(file != null) {
			try {
				getAppController().getStart().setDisable(false);
				getAppController().getStop().setDisable(true);
				Image img = new Image(file.toURI().toString());
				getAppController().getThumbnail().setImage(img);
				this.getSearcher().setSourceFile(file);
				getAppController().getResults().getItems().clear();
				getAppController().getSearching().setText("");
				getAppController().getDropMessage().setVisible(false);
				getPreference().setSourceImage(file);
			}
			catch(Exception ex) {
				getAppController().getStart().setDisable(true);
				getAppController().getStop().setDisable(true);
				ex.printStackTrace();
			}
		}
	}

	/** 検索結果のクリック */
	public void resultClickedHndl(MouseEvent e) {
		if(e.getClickCount() == 2) {
			int idx = getAppController().getResults().getSelectionModel().getSelectedIndex();
			new ImageWindow(getSearcher().getHitList().get(idx));
		}
	}

	/** 検索先パスへのキー入力 */
	public void directoryKeyPressedHndl(KeyEvent e) {
		if(e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER)
			getAppController().getDirectory().setText(getSearcher().getDirectory().getPath());
	}

	/** 検索先パスの変更 */
	public void directoryChangedHndl(String path) {
		File dir = new File(path);
		if(dir.exists()) {
			getSearcher().setDirectory(dir);
			getAppController().getDirectory().setStyle("-fx-background-color: white;");
			boolean startDisable = false;
			if(getSearcher().getSourceFile() == null)
				startDisable = true;
			getAppController().getStart().setDisable(startDisable);
			getAppController().getStop().setDisable(true);
			getPreference().setDirectory(dir);
		} else {
			getAppController().getDirectory().setStyle("-fx-background-color: red;");
			getAppController().getStart().setDisable(true);
			getAppController().getStop().setDisable(true);
		}
	}

	/** 差異値の変更 */
	public void differencyChangedHndl(int val) {
		getSearcher().setThreshold(val * 1.0D);
		getPreference().setDifferency(val);
	}

	/** 「閉じる」メニュー */
	public void closeMenuHndl() {
		getAppStage().close();
		setAppStage(null);
	}

	/** 「このアプリについて」メニュー */
	public void aboutMenuHndl() {
		openAboutStage();
	}

	/** アバウト画面の閉じるボタンクリック */
	private void aboutCloseClickedHndl() {
		closeAboutStage();
	}

	//------------------------------------------------------------------------------
	// 検索中の処理
	//------------------------------------------------------------------------------

	/** 検索終了 */
	private void searchFinished() {
		Platform.runLater(new Runnable() {
			public void run() {
				getAppController().getSearching().setText(getSearcher().getHitList().size() + " files hits...");
				getAppController().getStart().setDisable(false);
				getAppController().getStop().setDisable(true);
				getAppController().getOpPanel().setDisable(false);
			}
		});
	}

	/** 次のファイルを検索 */
	private void searchNext(File file) {
		Platform.runLater(new Runnable() {
			public void run() {
				getAppController().getSearching().setText(file.getPath());
			}
		});
	}

	/** 一致 */
	private void searchHits(File file, Image img) {
		Platform.runLater(new Runnable() {
			public void run() {
				ImageView icon = new ImageView();
				icon.setFitWidth(64);
				icon.setFitHeight(64);
				icon.setCache(false);
				icon.setPreserveRatio(true);
				icon.setImage(img);
				Label item = new Label(file.getPath(), icon);
				getAppController().getResults().getItems().add(item);
			}
		});
	}

	//------------------------------------------------------------------------------
	// ファイル検証
	//------------------------------------------------------------------------------

	/** 画像ファイルの判定 */
	private boolean isImageFile(File f) {
		String fileName = f.getName();
		return(f.exists() && (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")));
	}

	//------------------------------------------------------------------------------
	// 検索
	//------------------------------------------------------------------------------

	/** 検索を行うスレッド */
	private Thread searchThread = null;

	/** 検索開始 */
	private void startSearchThread() {
		this.searchThread = new Thread() {
			public void run() {
				File dir = new File(getAppController().getDirectory().getText());
				if(dir.exists() == false)
					dir = new File(".");
				getSearcher().search();
			}
		};

		this.searchThread.start();
	}

	/** 検索停止 */
	private void stopSearchThread() {
		if(this.searchThread == null)
			return;

		getSearcher().setInterrupt();
		try {
			this.searchThread.join();
		}
		catch(Exception ex) {
		}
		finally {
			this.searchThread = null;
		}
	}

	//------------------------------------------------------------------------------
	// アプリケーション終了
	//------------------------------------------------------------------------------

	/** アプリ終了時の処理 */
	private void appClose() {
		stopSearchThread();
		getPreference().close();
	}

	//------------------------------------------------------------------------------
	// 初期設定
	//------------------------------------------------------------------------------

	/** 初期設定インスタンス */
	private Preference preference = new Preference();
	/** 初期設定インスタンス参照 */
	private Preference getPreference() { return(this.preference); }

	//------------------------------------------------------------------------------
	// 画像検索インスタンス
	//------------------------------------------------------------------------------

	/** 画像検索インスタンス */
	private FileSearcher searcher = new FileSearcher();
	/** 画像検索インスタンス参照 */
	private FileSearcher getSearcher() { return(this.searcher); };

	//------------------------------------------------------------------------------
	// コントローラ
	//------------------------------------------------------------------------------

	/** アプリケーション画面のコントローラ */
	private AppController appController;
	/** アプリケーション画面のコントローラ設定 */
	private void setAppController(AppController c) { this.appController = c; }
	/** アプリケーション画面のコントローラ参照 */
	private AppController getAppController() { return(this.appController); }

	/** アバウト画面のコントローラ */
	private AboutController aboutController;
	/** アバウト画面のコントローラ設定 */
	private void setAboutController(AboutController c) { this.aboutController = c; }
	/** アバウト画面のコントローラ参照 */
	private AboutController getAboutController() { return(this.aboutController); }

	//------------------------------------------------------------------------------
	// ステージ
	//------------------------------------------------------------------------------

	/** アプリケーション画面のステージ */
	private Stage appStage = null;
	/** アプリケーション画面のステージ設定 */
	private void setAppStage(Stage s) { this.appStage = s; }
	/** アプリケーション画面のステージ参照 */
	private Stage getAppStage() { return(this.appStage); }

	/** アバウト画面のステージ */
	private Stage aboutStage = null;
	/** アバウト画面のステージ設定 */
	private void setAboutStage(Stage s) { this.aboutStage = s; }
	/** アバウト画面のステージ参照 */
	private Stage getAboutStage() { return(this.aboutStage); }

	//------------------------------------------------------------------------------
	// シーン
	//------------------------------------------------------------------------------

	/** アプリケーション画面のシーン */
	private Scene appScene = null;
	/** アプリケーション画面のシーン設定 */
	private void setAppScene(Scene s) { this.appScene = s; }
	/** アプリケーション画面のシーン参照 */
	private Scene getAppScene() { return(this.appScene); }

	//------------------------------------------------------------------------------
	// 動作
	//------------------------------------------------------------------------------

	public static void main(String[] args) {
		launch(args);
	}
}
