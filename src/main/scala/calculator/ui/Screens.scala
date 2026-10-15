package calculator.ui

import calculator.model._
import calculator.storage.Storage
import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import java.util.UUID

object UIState {
  val households: ObservableBuffer[Household] =
    ObservableBuffer.from(Storage.loadHouseholds().getOrElse(List.empty))

  val currentView: ObjectProperty[scalafx.scene.layout.Region] =
    ObjectProperty[scalafx.scene.layout.Region](HomeView.view)

  def save(): Unit = Storage.saveHouseholds(households.toList)
}

// Shared View trait — DRY base for all screens
trait View {
  def view: scalafx.scene.layout.Region
}

object Styles {
  val headerStyle  = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
  val subStyle     = "-fx-font-size: 14px; -fx-text-fill: #555;"
  val cardStyle    = "-fx-background-color: #f4f6f8; -fx-background-radius: 10; -fx-padding: 20;"
  val primaryBtn   =
    "-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-size: 13px; " +
    "-fx-padding: 10 24 10 24; -fx-background-radius: 6; -fx-cursor: hand;"
  val dangerBtn    =
    "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; " +
    "-fx-padding: 6 14 6 14; -fx-background-radius: 6; -fx-cursor: hand;"
  val fieldStyle   =
    "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5; " +
    "-fx-background-radius: 5; -fx-padding: 6 10 6 10; -fx-font-size: 13px;"
}

// Home / Dashboard
object HomeView extends View {
  override def view: scalafx.scene.layout.Region = {
    val countLabel = new Label(s"Currently tracking ${UIState.households.size} household(s).") {
      style = Styles.subStyle
    }
    UIState.households.onChange { (buffer, _) =>
      countLabel.text.value = s"Currently tracking ${buffer.size} household(s)."
    }

    new VBox {
      padding   = Insets(40)
      spacing   = 18
      alignment = Pos.TopCenter
      style     = "-fx-background-color: #ecf0f1;"
      children  = Seq(
        new Label("Subsidy Eligibility Calculator") { style = Styles.headerStyle },
        new Label("Empowering NGOs and communities under SDG 1 — No Poverty") {
          style = Styles.subStyle
        },
        new Separator,
        new VBox {
          spacing = 10
          style   = Styles.cardStyle
          children = Seq(
            new Label("How to use:") { style = "-fx-font-weight: bold; -fx-font-size: 15px;" },
            new Label("1. Use '📝 Register Household' to add a new household.") { style = Styles.subStyle },
            new Label("2. Use '📊 Evaluate Subsidies' to view who qualifies for each subsidy.") { style = Styles.subStyle },
            new Label("3. Use '📋 My Households' to review and delete registered households.") { style = Styles.subStyle },
            new Label("4. Use 'ℹ️ Help & Info' to see all available subsidies and criteria.") { style = Styles.subStyle }
          )
        },
        countLabel
      )
    }
  }
}

// Feature 1 — Household Registration
object RegistrationView extends View {
  override def view: scalafx.scene.layout.Region = {
    val nameField       = new TextField { promptText = "e.g., Ali Bin Ahmad"; style = Styles.fieldStyle }
    val incomeField     = new TextField { promptText = "e.g., 2000.00";       style = Styles.fieldStyle }
    val dependentsField = new TextField { promptText = "e.g., 3";             style = Styles.fieldStyle }
    val disabilityBox   = new CheckBox("Household has a member with disability")
    val statusLabel     = new Label { style = Styles.subStyle }

    def styleLabel(ok: Boolean): Unit =
      statusLabel.textFill.value = if (ok) Color.web("#27ae60") else Color.web("#c0392b")

    val form = new GridPane {
      padding = Insets(20)
      hgap    = 16
      vgap    = 14
      alignment = Pos.Center
      add(new Label("Household Head Name:"), 0, 0)
      add(nameField,                          1, 0)
      add(new Label("Monthly Income (RM):"),  0, 1)
      add(incomeField,                        1, 1)
      add(new Label("Number of Dependents:"), 0, 2)
      add(dependentsField,                    1, 2)
      add(disabilityBox,                      1, 3)
    }

    val submitBtn = new Button("Register Household") {
      defaultButton = true
      style = Styles.primaryBtn
      onAction = handle {
        statusLabel.text.value = ""
        val name      = nameField.text.value.trim
        val incomeStr = incomeField.text.value.trim
        val depStr    = dependentsField.text.value.trim

        if (name.isEmpty) {
          styleLabel(false)
          statusLabel.text.value = "❌ Error: Household head name cannot be empty."
        } else {
          val incomeValidator = new Validators.PositiveDoubleValidator()
          val depValidator    = new Validators.NonNegativeIntValidator()

          (scala.util.Try(incomeStr.toDouble).toOption,
           scala.util.Try(depStr.toInt).toOption) match {
            case (Some(inc), Some(dep)) =>
              incomeValidator.validate(inc) match {
                case Left(err) =>
                  styleLabel(false); statusLabel.text.value = s"❌ Income Error: $err"
                case Right(validInc) =>
                  depValidator.validate(dep) match {
                    case Left(err) =>
                      styleLabel(false); statusLabel.text.value = s"❌ Dependents Error: $err"
                    case Right(validDep) =>
                      val newHouse = Household(
                        id           = UUID.randomUUID().toString.take(8),
                        headName     = name,
                        monthlyIncome = validInc,
                        dependents   = validDep,
                        hasDisability = disabilityBox.selected.value
                      )
                      UIState.households += newHouse
                      UIState.save()
                      nameField.text.value       = ""
                      incomeField.text.value     = ""
                      dependentsField.text.value = ""
                      disabilityBox.selected.value = false
                      styleLabel(true)
                      statusLabel.text.value = "✅ Household registered and saved successfully."
                  }
              }
            case _ =>
              styleLabel(false)
              statusLabel.text.value = "❌ Invalid format: Income must be a number, Dependents must be a whole number."
          }
        }
      }
    }

    new VBox {
      padding   = Insets(30)
      spacing   = 20
      alignment = Pos.TopCenter
      style     = "-fx-background-color: #ecf0f1;"
      children  = Seq(
        new Label("Register New Household") { style = Styles.headerStyle },
        new VBox { style = Styles.cardStyle; children = Seq(form) },
        submitBtn,
        statusLabel
      )
    }
  }
}

// Feature 2 — Subsidy Evaluation Table
object SubsidyEvalView extends View {
  override def view: scalafx.scene.layout.Region = {
    val table = new TableView[Household](UIState.households) {
      columnResizePolicy = TableView.ConstrainedResizePolicy
      columns ++= Seq(
        new TableColumn[Household, String] {
          text  = "Household Head"
          cellValueFactory = { data => StringProperty(data.value.headName) }
        },
        new TableColumn[Household, String] {
          text  = "Income (RM)"
          cellValueFactory = { data => StringProperty(FormattingUtils.formatCurrency(data.value.monthlyIncome)) }
        },
        new TableColumn[Household, String] {
          text  = "Dependents"
          cellValueFactory = { data => StringProperty(data.value.dependents.toString) }
        },
        new TableColumn[Household, String] {
          text  = "Disability"
          cellValueFactory = { data => StringProperty(if (data.value.hasDisability) "Yes" else "No") }
        },
        new TableColumn[Household, String] {
          text     = "Eligible Subsidies"
          prefWidth = 260
          cellValueFactory = { data =>
            val household = data.value
            val subsidies = List(HousingSubsidy(), EducationSubsidy(), FoodSubsidy())
            val eligible  = subsidies.filter(_.checkEligibility(household)).map(_.name)
            StringProperty(if (eligible.isEmpty) "None" else eligible.mkString(", "))
          }
        }
      )
    }

    new VBox {
      padding  = Insets(30)
      spacing  = 15
      style    = "-fx-background-color: #ecf0f1;"
      children = Seq(
        new Label("Household Subsidy Evaluation") { style = Styles.headerStyle },
        new Label("All registered households and their subsidy eligibility are shown below.") {
          style = Styles.subStyle
        },
        table
      )
    }
  }
}

// Feature 3 — My Households (View & Delete)
object HouseholdsView extends View {
  override def view: scalafx.scene.layout.Region = {
    val listView = new ListView[Household](UIState.households) {
      cellFactory = (_: ListView[Household]) => new ListCell[Household] {
        item.onChange { (_, _, household) =>
          if (household == null) {
            text.value = ""
          } else {
            text.value =
              s"[${household.id}]  ${household.headName}  |  ${FormattingUtils.formatCurrency(household.monthlyIncome)}  |  " +
              s"${household.dependents} dependent(s)  |  Disability: ${if (household.hasDisability) "Yes" else "No"}"
          }
        }
      }
    }

    val deleteBtn = new Button("Delete Selected") {
      style = Styles.dangerBtn
      onAction = handle {
        val selected = listView.selectionModel.value.getSelectedItem
        if (selected != null) {
          UIState.households -= selected
          UIState.save()
        }
      }
    }

    val statusLabel = new Label("Select a household and click 'Delete Selected' to remove it.") {
      style = Styles.subStyle
    }

    new VBox {
      padding  = Insets(30)
      spacing  = 15
      style    = "-fx-background-color: #ecf0f1;"
      children = Seq(
        new Label("My Registered Households") { style = Styles.headerStyle },
        statusLabel,
        listView,
        deleteBtn
      )
    }
  }
}

// Feature 4 — Help & Information Browser
object HelpView extends View {
  override def view: scalafx.scene.layout.Region = {
    val subsidies    = List(HousingSubsidy(), EducationSubsidy(), FoodSubsidy())
    val subsidyItems = ObservableBuffer.from(subsidies.map { subsidy =>
      s"${subsidy.name}\n   ${subsidy.description}\n   Monthly assistance: RM ${subsidy.subsidyAmount}"
    })
    val listView = new ListView[String] {
      items     = subsidyItems
      prefHeight = 260
    }

    new VBox {
      padding  = Insets(30)
      spacing  = 15
      style    = "-fx-background-color: #ecf0f1;"
      children = Seq(
        new Label("Available Subsidies & Eligibility Criteria") { style = Styles.headerStyle },
        new Label("The following government subsidies are evaluated for each household:") {
          style = Styles.subStyle
        },
        listView,
        new VBox {
          style   = Styles.cardStyle
          spacing = 8
          children = Seq(
            new Label("Eligibility Rules:") { style = "-fx-font-weight: bold;" },
            new Label("• Public Housing Subsidy: Income < RM 2,500 AND more than 2 dependents") { style = Styles.subStyle },
            new Label("• B40 Education Grant:     Income < RM 4,000 AND at least 1 dependent")    { style = Styles.subStyle },
            new Label("• Basic Necessities Basket: Income < RM 1,500 OR household has disability") { style = Styles.subStyle }
          )
        }
      )
    }
  }
}
