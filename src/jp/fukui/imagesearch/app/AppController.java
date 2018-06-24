package jp.fukui.imagesearch.app;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;

public class AppController implements Initializable {
	@Override public void initialize(URL location, ResourceBundle resources) { }

	@FXML private Button reference;
	@FXML private ImageView thumbnail;
	@FXML private AnchorPane top;
	@FXML private AnchorPane opPanel;
	@FXML private Button stop;
	@FXML private Slider differency;
	@FXML private Pane dropMessage;
	@FXML private Label searching;
	@FXML private Button start;
	@FXML private TextField directory;
	@FXML private ListView results;

	public Button getReference() { return(this.reference); }
	public ImageView getThumbnail() { return(this.thumbnail); }
	public AnchorPane getTop() { return(this.top); }
	public AnchorPane getOpPanel() { return(this.opPanel); }
	public Button getStop() { return(this.stop); }
	public Slider getDifferency() { return(this.differency); }
	public Pane getDropMessage() { return(this.dropMessage); }
	public Label getSearching() { return(this.searching); }
	public Button getStart() { return(this.start); }
	public TextField getDirectory() { return(this.directory); }
	public ListView getResults() { return(this.results); }

	@FXML public void folderDropped(DragEvent e) { this._o.folderDropped(e);  }
	@FXML public void folderDragOver(DragEvent e) { this._o.folderDragOver(e);  }
	@FXML public void directoryKeyPressed(KeyEvent e) { this._o.directoryKeyPressed(e);  }
	@FXML public void referenceClicked(ActionEvent e) { this._o.referenceClicked(e);  }
	@FXML public void fileDropped(DragEvent e) { this._o.fileDropped(e);  }
	@FXML public void fileDragOver(DragEvent e) { this._o.fileDragOver(e);  }
	@FXML public void fileClicked(MouseEvent e) { this._o.fileClicked(e);  }
	@FXML public void resultClicked(MouseEvent e) { this._o.resultClicked(e);  }
	@FXML public void startClicked(ActionEvent e) { this._o.startClicked(e);  }
	@FXML public void stopClicked(ActionEvent e) { this._o.stopClicked(e);  }
	@FXML public void closeMenu(ActionEvent e) { this._o.closeMenu(e);  }
	@FXML public void aboutMenu(ActionEvent e) { this._o.aboutMenu(e);  }

	//------------------------------------------------------------------------------
	// Event notification
	//------------------------------------------------------------------------------

	public interface Observer {
		public void folderDropped(DragEvent e);
		public void folderDragOver(DragEvent e);
		public void directoryKeyPressed(KeyEvent e);
		public void referenceClicked(ActionEvent e);
		public void fileDropped(DragEvent e);
		public void fileDragOver(DragEvent e);
		public void fileClicked(MouseEvent e);
		public void resultClicked(MouseEvent e);
		public void startClicked(ActionEvent e);
		public void stopClicked(ActionEvent e);
		public void closeMenu(ActionEvent e);
		public void aboutMenu(ActionEvent e);
	}

	private Observer _o = null;
	public void setObserver(Observer o) { this._o = o; }
}
