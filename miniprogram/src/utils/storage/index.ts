const storage = {
  set(key: string, value: string): boolean {
    try {
      if (key && value) {
        uni.setStorageSync(key, value);
        return true;
      }
      return false;
    }
    catch (error) {
      console.error(`[Storage] set failed for key: ${key}`, error);
      return false;
    }
  },
  get(key: string): string | null {
    try {
      if (!key) return null;
      return uni.getStorageSync(key) || null;
    }
    catch (error) {
      console.error(`[Storage] get failed for key: ${key}`, error);
      return null;
    }
  },
  setJSON(key: string, jsonValue: unknown): boolean {
    try {
      if (jsonValue != null) {
        return this.set(key, JSON.stringify(jsonValue));
      }
      return false;
    }
    catch (error) {
      console.error(`[Storage] setJSON failed for key: ${key}`, error);
      return false;
    }
  },
  getJSON<T = unknown>(key: string): T | null {
    try {
      const value = this.get(key);
      return value ? JSON.parse(value) : null;
    }
    catch (error) {
      console.error(`[Storage] getJSON parse failed for key: ${key}`, error);
      return null;
    }
  },
  remove(key: string): boolean {
    try {
      uni.removeStorageSync(key);
      return true;
    }
    catch (error) {
      console.error(`[Storage] remove failed for key: ${key}`, error);
      return false;
    }
  },
};

export default storage;
