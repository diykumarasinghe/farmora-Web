import { defineStore } from 'pinia'
import { notificationService } from '../services/notificationService'

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    notifications: [],
    unreadCount: 0,
    loading: false
  }),

  actions: {
    async fetchNotifications() {
      this.loading = true
      try {
        this.notifications = await notificationService.getNotifications()
        this.unreadCount = await notificationService.getUnreadCount()
      } catch (err) {
        console.error('Failed to load notifications', err)
      } finally {
        this.loading = false
      }
    },

    async fetchUnreadCount() {
      try {
        this.unreadCount = await notificationService.getUnreadCount()
      } catch (err) {
        console.error('Failed to load unread count', err)
      }
    },

    async markAsRead(id) {
      try {
        await notificationService.markAsRead(id)
        const item = this.notifications.find(n => n.id === id)
        if (item && !item.readStatus) {
          item.readStatus = true
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        }
      } catch (err) {
        console.error('Failed to mark notification read', err)
      }
    },

    async markAllAsRead() {
      try {
        await notificationService.markAllAsRead()
        this.notifications.forEach(n => (n.readStatus = true))
        this.unreadCount = 0
      } catch (err) {
        console.error('Failed to mark all notifications read', err)
      }
    }
  }
})
