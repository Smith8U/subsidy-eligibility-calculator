// See docs/citations.md for third-party citations and licenses (MUnit - Apache 2.0).
package calculator

import calculator.model._

class SubsidySpec extends munit.FunSuite {

  test("PositiveDoubleValidator should validate positive numbers correctly") {
    val validator = new Validators.PositiveDoubleValidator()
    assertEquals(validator.validate(1500.0), Right(1500.0))
    assertEquals(validator.validate(0.0), Left("Income must be greater than zero"))
    assertEquals(validator.validate(-100.0), Left("Income must be greater than zero"))
  }

  test("NonNegativeIntValidator should validate non-negative integers correctly") {
    val validator = new Validators.NonNegativeIntValidator()
    assertEquals(validator.validate(0), Right(0))
    assertEquals(validator.validate(3), Right(3))
    assertEquals(validator.validate(-1), Left("Count must be zero or positive"))
  }

  test("FormattingUtils should format currency with RM prefix") {
    assertEquals(FormattingUtils.formatCurrency(2500.5), "RM 2500.50")
  }

  test("HousingSubsidy eligibility criteria check") {
    val subsidy = HousingSubsidy()
    val eligibleHh = Household("1", "Ahmad", 2000.0, 3, false)
    val highIncomeHh = Household("2", "Bala", 3000.0, 3, false)
    val lowDepHh = Household("3", "Chai", 2000.0, 2, false)

    assert(subsidy.checkEligibility(eligibleHh))
    assert(!subsidy.checkEligibility(highIncomeHh))
    assert(!subsidy.checkEligibility(lowDepHh))
  }

  test("EducationSubsidy eligibility criteria check") {
    val subsidy = EducationSubsidy()
    val eligibleHh = Household("1", "Ahmad", 3500.0, 1, false)
    val highIncomeHh = Household("2", "Bala", 4500.0, 1, false)
    val noDepHh = Household("3", "Chai", 2000.0, 0, false)

    assert(subsidy.checkEligibility(eligibleHh))
    assert(!subsidy.checkEligibility(highIncomeHh))
    assert(!subsidy.checkEligibility(noDepHh))
  }

  test("FoodSubsidy eligibility criteria check") {
    val subsidy = FoodSubsidy()
    val lowIncomeHh = Household("1", "Ahmad", 1200.0, 0, false)
    val disabledHh = Household("2", "Bala", 3000.0, 0, true)
    val ineligibleHh = Household("3", "Chai", 2000.0, 0, false)

    assert(subsidy.checkEligibility(lowIncomeHh))
    assert(subsidy.checkEligibility(disabledHh))
    assert(!subsidy.checkEligibility(ineligibleHh))
  }

  test("Storage should save and load households using SQLite JDBC") {
    val testList = List(
      Household("t1", "Test Household", 2500.0, 2, false)
    )
    assert(calculator.storage.Storage.saveHouseholds(testList).isSuccess)
    val loaded = calculator.storage.Storage.loadHouseholds()
    assert(loaded.isSuccess)
    assert(loaded.toOption.exists(_.exists(_.id == "t1")))
  }
}
