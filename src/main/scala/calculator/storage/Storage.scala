package calculator.storage

import calculator.model.Household
import java.io.File
import java.sql.{DriverManager, Connection}
import scala.util.{Try, Using}

object Storage {
  private val dbFile = "data/households.db"
  private val dbUrl  = s"jdbc:sqlite:$dbFile"

  private def getConnection(): Connection = {
    val file = new File(dbFile)
    if (!file.getParentFile.exists()) {
      file.getParentFile.mkdirs()
    }
    DriverManager.getConnection(dbUrl)
  }

  private def initDb(): Unit = {
    Using.resource(getConnection()) { conn =>
      Using.resource(conn.createStatement()) { stmt =>
        val sql =
          """CREATE TABLE IF NOT EXISTS households (
            |  id TEXT PRIMARY KEY,
            |  headName TEXT NOT NULL,
            |  monthlyIncome REAL NOT NULL,
            |  dependents INTEGER NOT NULL,
            |  hasDisability INTEGER NOT NULL
            |);""".stripMargin
        stmt.execute(sql)
      }
    }
  }

  // Initialize table when Storage object is loaded
  Try(initDb())

  def saveHousehold(household: Household): Try[Unit] = Try {
    initDb()
    Using.resource(getConnection()) { conn =>
      val sql =
        """INSERT OR REPLACE INTO households (id, headName, monthlyIncome, dependents, hasDisability)
          |VALUES (?, ?, ?, ?, ?);""".stripMargin
      Using.resource(conn.prepareStatement(sql)) { pstmt =>
        pstmt.setString(1, household.id)
        pstmt.setString(2, household.headName)
        pstmt.setDouble(3, household.monthlyIncome)
        pstmt.setInt(4, household.dependents)
        pstmt.setInt(5, if (household.hasDisability) 1 else 0)
        pstmt.executeUpdate()
      }
    }
  }

  def saveHouseholds(households: List[Household]): Try[Unit] = Try {
    initDb()
    Using.resource(getConnection()) { conn =>
      conn.setAutoCommit(false)
      try {
        val clearSql = "DELETE FROM households;"
        Using.resource(conn.createStatement()) { stmt => stmt.executeUpdate(clearSql) }

        val insertSql =
          """INSERT INTO households (id, headName, monthlyIncome, dependents, hasDisability)
            |VALUES (?, ?, ?, ?, ?);""".stripMargin
        Using.resource(conn.prepareStatement(insertSql)) { pstmt =>
          for (householdItem <- households) {
            pstmt.setString(1, householdItem.id)
            pstmt.setString(2, householdItem.headName)
            pstmt.setDouble(3, householdItem.monthlyIncome)
            pstmt.setInt(4, householdItem.dependents)
            pstmt.setInt(5, if (householdItem.hasDisability) 1 else 0)
            pstmt.executeUpdate()
          }
        }
        conn.commit()
      } catch {
        case cause: Throwable =>
          conn.rollback()
          throw cause
      } finally {
        conn.setAutoCommit(true)
      }
    }
  }

  def deleteHousehold(id: String): Try[Unit] = Try {
    initDb()
    Using.resource(getConnection()) { conn =>
      val sql = "DELETE FROM households WHERE id = ?;"
      Using.resource(conn.prepareStatement(sql)) { pstmt =>
        pstmt.setString(1, id)
        pstmt.executeUpdate()
      }
    }
  }

  def loadHouseholds(): Try[List[Household]] = Try {
    initDb()
    Using.resource(getConnection()) { conn =>
      val sql = "SELECT id, headName, monthlyIncome, dependents, hasDisability FROM households;"
      Using.resource(conn.createStatement()) { stmt =>
        Using.resource(stmt.executeQuery(sql)) { rs =>
          Iterator.continually(rs)
            .takeWhile(_.next())
            .map { resultSetRow =>
              Household(
                resultSetRow.getString("id"),
                resultSetRow.getString("headName"),
                resultSetRow.getDouble("monthlyIncome"),
                resultSetRow.getInt("dependents"),
                resultSetRow.getInt("hasDisability") == 1
              )
            }.toList
        }
      }
    }
  }
}
