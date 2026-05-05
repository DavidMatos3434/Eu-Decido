package com.david.eudecido.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class NotificationsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllNotifications(mapper: (
    id: String,
    title: String,
    message: String,
    is_read: Long,
    created_at: String,
  ) -> T): Query<T> = Query(616_138_402, arrayOf("notifications"), driver, "notifications.sq",
      "selectAllNotifications",
      "SELECT notifications.id, notifications.title, notifications.message, notifications.is_read, notifications.created_at FROM notifications ORDER BY created_at DESC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun selectAllNotifications(): Query<Notifications> = selectAllNotifications { id, title,
      message, is_read, created_at ->
    Notifications(
      id,
      title,
      message,
      is_read,
      created_at
    )
  }

  public fun insertNotification(
    id: String,
    title: String,
    message: String,
    is_read: Long,
    created_at: String,
  ) {
    driver.execute(-909_617_533, """
        |INSERT INTO notifications(id, title, message, is_read, created_at) 
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindString(0, id)
          bindString(1, title)
          bindString(2, message)
          bindLong(3, is_read)
          bindString(4, created_at)
        }
    notifyQueries(-909_617_533) { emit ->
      emit("notifications")
    }
  }

  public fun markAsRead(id: String) {
    driver.execute(-1_843_721_900, """UPDATE notifications SET is_read = 1 WHERE id = ?""", 1) {
          bindString(0, id)
        }
    notifyQueries(-1_843_721_900) { emit ->
      emit("notifications")
    }
  }
}
