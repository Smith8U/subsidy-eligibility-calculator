package calculator

import calculator.ui._
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.geometry.Insets
import scalafx.Includes._

object MainApp extends JFXApp3 {
  override def start(): Unit = {
    // Suppress internal JavaFX PlatformImpl module configuration warning
    java.util.logging.Logger.getLogger("com.sun.javafx").setLevel(java.util.logging.Level.OFF)

    val navButtonStyle =
      "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 13px; " +
      "-fx-padding: 10 16 10 16; -fx-background-radius: 6; -fx-cursor: hand;"
    val navButtonHoverStyle =
      "-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-size: 13px; " +
      "-fx-padding: 10 16 10 16; -fx-background-radius: 6; -fx-cursor: hand;"

    def makeNavBtn(label: String, targetView: => scalafx.scene.layout.Region): Button = {
      val btn = new Button(label) {
        maxWidth = Double.MaxValue
        style = navButtonStyle
      }
      btn.onMouseEntered = _ => btn.style = navButtonHoverStyle
      btn.onMouseExited  = _ => btn.style = navButtonStyle
      btn.onAction = handle {
        UIState.currentView.value = targetView
      }
      btn
    }

    val navBar = new VBox {
      padding = Insets(16)
      spacing = 10
      style = "-fx-background-color: #2c3e50;"
      prefWidth = 210

      children = Seq(
        makeNavBtn("🏠  Home",               HomeView.view),
        makeNavBtn("📝  Register Household", RegistrationView.view),
        makeNavBtn("📊  Evaluate Subsidies", SubsidyEvalView.view),
        makeNavBtn("📋  My Households",      HouseholdsView.view),
        makeNavBtn("ℹ️  Help & Info",        HelpView.view)
      )
    }

    val rootPane = new BorderPane {
      left   = navBar
      center = HomeView.view
    }

    // Region cannot bind directly to BorderPane's Node property, so update it on change.
    UIState.currentView.onChange { (_, _, newView) =>
      rootPane.center = newView
    }

    stage = new JFXApp3.PrimaryStage {
      title  = "Subsidy Eligibility Calculator — SDG 1"
      width  = 950
      height = 620
      scene  = new Scene(rootPane) {
        stylesheets = Seq(getClass.getResource("/style.css") match {
          case null => ""
          case url  => url.toExternalForm
        })
      }
    }
  }
}
