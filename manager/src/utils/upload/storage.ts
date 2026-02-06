/**
 * 大文件切片上传 - IndexedDB 存储封装
 *
 * 用于持久化上传进度，支持断点续传
 *
 * @module utils/upload/storage
 * @author 陈鸿昇
 */

import {
  DB_NAME,
  DB_VERSION,
  STORE_NAME,
  BACKUP_KEY_PREFIX,
  EXPIRED_DAYS
} from './constants'
import type { UploadRecord } from './types'

/**
 * 上传记录存储类
 *
 * 封装 IndexedDB 操作，提供上传记录的增删改查
 */
class UploadStorage {
  private db: IDBDatabase | null = null
  private dbPromise: Promise<IDBDatabase> | null = null

  /**
   * 获取数据库连接
   */
  private async getDB(): Promise<IDBDatabase> {
    if (this.db) return this.db

    if (this.dbPromise) return this.dbPromise

    this.dbPromise = new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION)

      request.onerror = () => {
        console.error('[UploadStorage] Failed to open IndexedDB:', request.error)
        reject(request.error)
      }

      request.onsuccess = () => {
        this.db = request.result
        resolve(this.db)
      }

      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result

        // 创建存储对象
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          const store = db.createObjectStore(STORE_NAME, { keyPath: 'fileHash' })
          // 创建索引用于按时间查询
          store.createIndex('createdAt', 'createdAt', { unique: false })
          store.createIndex('updatedAt', 'updatedAt', { unique: false })
        }
      }
    })

    return this.dbPromise
  }

  /**
   * 保存上传记录
   * @param record 上传记录
   */
  async saveRecord(record: UploadRecord): Promise<void> {
    try {
      const db = await this.getDB()
      const transaction = db.transaction(STORE_NAME, 'readwrite')
      const store = transaction.objectStore(STORE_NAME)

      const now = Date.now()
      const recordToSave: UploadRecord = {
        ...record,
        updatedAt: now,
        createdAt: record.createdAt || now
      }

      return new Promise((resolve, reject) => {
        const request = store.put(recordToSave)
        request.onsuccess = () => resolve()
        request.onerror = () => {
          console.error('[UploadStorage] Failed to save record:', request.error)
          reject(request.error)
        }
      })
    } catch (error) {
      console.error('[UploadStorage] saveRecord error:', error)
      // 降级到 localStorage
      this.backupToLocalStorage(record)
      throw error
    }
  }

  /**
   * 获取上传记录
   * @param fileHash 文件哈希
   */
  async getRecord(fileHash: string): Promise<UploadRecord | null> {
    try {
      const db = await this.getDB()
      const transaction = db.transaction(STORE_NAME, 'readonly')
      const store = transaction.objectStore(STORE_NAME)

      return new Promise((resolve, reject) => {
        const request = store.get(fileHash)
        request.onsuccess = () => resolve(request.result || null)
        request.onerror = () => {
          console.error('[UploadStorage] Failed to get record:', request.error)
          reject(request.error)
        }
      })
    } catch (error) {
      console.error('[UploadStorage] getRecord error:', error)
      // 尝试从 localStorage 恢复
      return this.restoreFromLocalStorage(fileHash)
    }
  }

  /**
   * 更新已上传的分片列表
   * @param fileHash 文件哈希
   * @param uploadedChunks 已上传的分片索引列表
   */
  async updateUploadedChunks(fileHash: string, uploadedChunks: number[]): Promise<void> {
    try {
      const record = await this.getRecord(fileHash)
      if (record) {
        record.uploadedChunks = uploadedChunks
        record.updatedAt = Date.now()
        await this.saveRecord(record)
      }
    } catch (error) {
      console.error('[UploadStorage] updateUploadedChunks error:', error)
      throw error
    }
  }

  /**
   * 添加已上传的分片
   * @param fileHash 文件哈希
   * @param chunkIndex 分片索引
   */
  async addUploadedChunk(fileHash: string, chunkIndex: number): Promise<void> {
    try {
      const record = await this.getRecord(fileHash)
      if (record) {
        if (!record.uploadedChunks.includes(chunkIndex)) {
          record.uploadedChunks.push(chunkIndex)
          record.uploadedChunks.sort((a, b) => a - b)
          record.updatedAt = Date.now()
          await this.saveRecord(record)
        }
      }
    } catch (error) {
      console.error('[UploadStorage] addUploadedChunk error:', error)
      throw error
    }
  }

  /**
   * 删除上传记录
   * @param fileHash 文件哈希
   */
  async deleteRecord(fileHash: string): Promise<void> {
    try {
      const db = await this.getDB()
      const transaction = db.transaction(STORE_NAME, 'readwrite')
      const store = transaction.objectStore(STORE_NAME)

      return new Promise((resolve, reject) => {
        const request = store.delete(fileHash)
        request.onsuccess = () => {
          // 同时清理 localStorage 备份
          this.clearLocalStorageBackup(fileHash)
          resolve()
        }
        request.onerror = () => {
          console.error('[UploadStorage] Failed to delete record:', request.error)
          reject(request.error)
        }
      })
    } catch (error) {
      console.error('[UploadStorage] deleteRecord error:', error)
      throw error
    }
  }

  /**
   * 清理过期记录
   * @param days 过期天数，默认 7 天
   */
  async cleanupExpired(days: number = EXPIRED_DAYS): Promise<number> {
    try {
      const db = await this.getDB()
      const transaction = db.transaction(STORE_NAME, 'readwrite')
      const store = transaction.objectStore(STORE_NAME)
      const index = store.index('updatedAt')

      const expireTime = Date.now() - days * 24 * 60 * 60 * 1000
      const range = IDBKeyRange.upperBound(expireTime)

      let deletedCount = 0

      return new Promise((resolve, reject) => {
        const request = index.openCursor(range)

        request.onsuccess = (event) => {
          const cursor = (event.target as IDBRequest<IDBCursorWithValue>).result
          if (cursor) {
            const record = cursor.value as UploadRecord
            cursor.delete()
            this.clearLocalStorageBackup(record.fileHash)
            deletedCount++
            cursor.continue()
          } else {
            console.log(`[UploadStorage] Cleaned up ${deletedCount} expired records`)
            resolve(deletedCount)
          }
        }

        request.onerror = () => {
          console.error('[UploadStorage] Failed to cleanup expired records:', request.error)
          reject(request.error)
        }
      })
    } catch (error) {
      console.error('[UploadStorage] cleanupExpired error:', error)
      // 清理 localStorage 中的过期备份
      this.cleanupExpiredLocalStorageBackups(days)
      throw error
    }
  }

  /**
   * 获取所有上传记录
   */
  async getAllRecords(): Promise<UploadRecord[]> {
    try {
      const db = await this.getDB()
      const transaction = db.transaction(STORE_NAME, 'readonly')
      const store = transaction.objectStore(STORE_NAME)

      return new Promise((resolve, reject) => {
        const request = store.getAll()
        request.onsuccess = () => resolve(request.result || [])
        request.onerror = () => {
          console.error('[UploadStorage] Failed to get all records:', request.error)
          reject(request.error)
        }
      })
    } catch (error) {
      console.error('[UploadStorage] getAllRecords error:', error)
      return []
    }
  }

  /**
   * 备份到 localStorage（紧急降级方案）
   * @param record 上传记录
   */
  backupToLocalStorage(record: UploadRecord): void {
    try {
      const key = `${BACKUP_KEY_PREFIX}${record.fileHash}`
      localStorage.setItem(key, JSON.stringify(record))
    } catch (error) {
      console.error('[UploadStorage] Failed to backup to localStorage:', error)
    }
  }

  /**
   * 从 localStorage 恢复
   * @param fileHash 文件哈希
   */
  private restoreFromLocalStorage(fileHash: string): UploadRecord | null {
    try {
      const key = `${BACKUP_KEY_PREFIX}${fileHash}`
      const data = localStorage.getItem(key)
      return data ? JSON.parse(data) : null
    } catch (error) {
      console.error('[UploadStorage] Failed to restore from localStorage:', error)
      return null
    }
  }

  /**
   * 清理 localStorage 备份
   * @param fileHash 文件哈希
   */
  private clearLocalStorageBackup(fileHash: string): void {
    try {
      const key = `${BACKUP_KEY_PREFIX}${fileHash}`
      localStorage.removeItem(key)
    } catch (error) {
      console.error('[UploadStorage] Failed to clear localStorage backup:', error)
    }
  }

  /**
   * 清理过期的 localStorage 备份
   * @param days 过期天数
   */
  private cleanupExpiredLocalStorageBackups(days: number): void {
    try {
      const expireTime = Date.now() - days * 24 * 60 * 60 * 1000
      const keysToRemove: string[] = []

      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i)
        if (key && key.startsWith(BACKUP_KEY_PREFIX)) {
          try {
            const data = localStorage.getItem(key)
            if (data) {
              const record: UploadRecord = JSON.parse(data)
              if (record.updatedAt < expireTime) {
                keysToRemove.push(key)
              }
            }
          } catch {
            // 解析失败的记录也删除
            keysToRemove.push(key)
          }
        }
      }

      keysToRemove.forEach((key) => localStorage.removeItem(key))
      console.log(`[UploadStorage] Cleaned up ${keysToRemove.length} expired localStorage backups`)
    } catch (error) {
      console.error('[UploadStorage] Failed to cleanup localStorage backups:', error)
    }
  }

  /**
   * 关闭数据库连接
   */
  close(): void {
    if (this.db) {
      this.db.close()
      this.db = null
      this.dbPromise = null
    }
  }
}

/** 单例导出 */
export const uploadStorage = new UploadStorage()

export default uploadStorage
