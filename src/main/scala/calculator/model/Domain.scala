package calculator.model

// Immutability: using case classes and sealed traits with no vars.
case class Household(
  id: String,
  headName: String,
  monthlyIncome: Double,
  dependents: Int,
  hasDisability: Boolean
)

// DRY: Helper object providing common logic for formatting income
object FormattingUtils {
  // Encapsulation: private field used as a shared constant, hidden from outside.
  // why: We don't want other classes tampering with the currency symbol.
  private val currencySymbol = "RM"

  def formatCurrency(amount: Double): String = f"$currencySymbol $amount%.2f"
}

// Parametric Polymorphism: A generic trait for validation
trait Validator[T] {
  def validate(value: T): Either[String, T]
}

object Validators {
  class PositiveDoubleValidator extends Validator[Double] {
    override def validate(value: Double): Either[String, Double] = {
      if (value > 0) Right(value)
      else Left("Income must be greater than zero")
    }
  }
  
  class NonNegativeIntValidator extends Validator[Int] {
    override def validate(value: Int): Either[String, Int] = {
      if (value >= 0) Right(value)
      else Left("Count must be zero or positive")
    }
  }
}

// Inheritance: sealed trait with multiple concrete subclasses
sealed trait Subsidy {
  def name: String
  def description: String
  def subsidyAmount: Double
  
  // Subtype Polymorphism: Method to be overridden
  def checkEligibility(household: Household): Boolean
}

case class HousingSubsidy() extends Subsidy {
  val name = "Public Housing Subsidy"
  val description = "Assistance for low-income households with housing rentals."
  val subsidyAmount = 300.0

  override def checkEligibility(household: Household): Boolean = {
    household.monthlyIncome < 2500.0 && household.dependents > 2
  }
}

case class EducationSubsidy() extends Subsidy {
  val name = "B40 Education Grant"
  val description = "Support for schooling children in B40 families."
  val subsidyAmount = 150.0

  override def checkEligibility(household: Household): Boolean = {
    household.monthlyIncome < 4000.0 && household.dependents > 0
  }
}

case class FoodSubsidy() extends Subsidy {
  val name = "Basic Necessities Basket"
  val description = "Monthly food basket for extreme poverty or disabled households."
  val subsidyAmount = 200.0

  override def checkEligibility(household: Household): Boolean = {
    household.monthlyIncome < 1500.0 || household.hasDisability
  }
}
