package jp.fukui.imagesearch.app;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;

public class AboutController implements Initializable {
	@Override public void initialize(URL location, ResourceBundle resources) { }

	@FXML public void closeClicked(ActionEvent e) { this._o.closeClicked(e);  }

	//------------------------------------------------------------------------------
	// Event notification
	//------------------------------------------------------------------------------

	public interface Observer {
		public void closeClicked(ActionEvent e);
	}

	private Observer _o = null;
	public void setObserver(Observer o) { this._o = o; }
}
