# 兑换码查询系统改造总结

## 改造完成时间
2025-11-05

## 改造目标
将兑换码查询方式从读取本地 .txt 文件改为从服务器查询

---

## 一、新增文件（3个）

### 1. SearchApiService.kt
**路径**: `app/src/main/java/com/plant/levelcodemoreplant/data/api/SearchApiService.kt`
**功能**: 定义查询兑换码的 API 接口
- 接口方法: `searchCode()`
- 请求方式: POST
- 端点: `/api/search`

### 2. SearchRequest.kt
**路径**: `app/src/main/java/com/plant/levelcodemoreplant/data/model/SearchRequest.kt`
**功能**: 查询请求数据模型
- `keyword`: 植物名/装扮名 或 编号组合
- `license_key`: 用户卡密
- `device_id`: 设备ID

### 3. SearchResponse.kt
**路径**: `app/src/main/java/com/plant/levelcodemoreplant/data/model/SearchResponse.kt`
**功能**: 查询响应数据模型
- `success`: 是否成功
- `data`: 包含 search_key, code_type, encrypted_data
- `message`: 错误信息

---

## 二、修改文件（5个）

### 1. AuthApiService.kt
**修改内容**:
- 更新 BASE_URL: `http://106.54.228.106:3000/`

### 2. RetrofitClient.kt
**修改内容**:
- 超时时间从 10 秒改为 30 秒
- 新增 `searchApiService` 实例

### 3. LevelDataSource.kt（单植物/单装扮）
**修改内容**:
- 注入 `SearchApiService` 和 `PrefsManager`
- 新增 `searchCodeFromServer()` 方法
  - 构造 keyword: `"${plantName}兑换码"`
  - 调用服务器 API 查询
  - 返回 JSON 格式的兑换码
- 保留原有的 `loadLevels()` 方法（用于显示植物列表）

### 4. MultiPlantDataSource.kt（多植物）
**修改内容**:
- 注入 `SearchApiService` 和 `PrefsManager`
- 修改 `queryGiftCode()` 方法
  - 构造 keyword: 植物编号用空格连接（如 "1045 200134 111067"）
  - 调用服务器 API 查询
  - 返回 JSON 对象
- 保留原有的 `loadPlantNames()` 方法

### 5. MultiCostumeDataSource.kt（多装扮）
**修改内容**:
- 注入 `SearchApiService` 和 `PrefsManager`
- 修改 `generateCode()` 方法
  - 构造 keyword: 装扮ID用空格连接
  - 调用服务器 API 查询
  - 返回 JSON 字符串
- 保留原有的 `loadCostumeNames()` 方法

### 6. SimpleLevelViewModel.kt
**修改内容**:
- 修改 `generateCode()` 方法：调用 `dataSource.searchCodeFromServer()`
- 修改 `startGeneration()` 方法：批量调用服务器 API 查询

---

## 三、服务器配置

### API 信息
- **服务器地址**: http://106.54.228.106:3000
- **端点**: /api/search
- **请求方式**: POST
- **超时时间**: 30 秒

### 请求参数示例

#### 单植物/装扮
```json
{
  "keyword": "大力花菜兑换码",
  "license_key": "ABCD-1234-EFGH-5678",
  "device_id": "device_12345"
}
```

#### 多植物/装扮
```json
{
  "keyword": "1045 200134 111067",
  "license_key": "ABCD-1234-EFGH-5678",
  "device_id": "device_12345"
}
```

### 响应格式示例

#### 单植物
```json
{
  "success": true,
  "data": {
    "search_key": "大力花菜兑换码",
    "code_type": "单植物-单装扮",
    "encrypted_data": {
      "i": "V330",
      "r": 0,
      "e": "9D9ICLXK-16ceGtlRlji8m9k..."
    }
  }
}
```

#### 多植物
```json
{
  "success": true,
  "data": {
    "search_key": "1045 200134 111067",
    "code_type": "40选3多植物",
    "encrypted_data": {
      "i": "V330",
      "r": 0,
      "e": "9D9ICLXK-16ceGtlRlji8m9k..."
    }
  }
}
```

---

## 四、错误处理

### 错误类型及提示
1. **网络连接失败** → "网络连接失败，请检查网络设置"
2. **请求超时** → "请求超时，请检查网络连接后重试"
3. **未找到兑换码** → 显示服务器返回的错误信息
4. **卡密无效** → 显示服务器返回的错误信息
5. **其他错误** → "查询失败: {具体错误信息}"

### 错误显示方式
- 使用现有的 `errorMessage` 字段在 UI State 中
- 在界面上内联显示错误信息
- 不阻塞用户操作

---

## 五、测试信息

### 测试账号
- **测试卡密**: `TEST-0000-TEST-0000`
- **测试设备ID**: `device_12345`

### 测试步骤
1. 确保已用测试卡密激活应用
2. 测试单植物查询：
   - 搜索任意植物名（如"大力花菜"）
   - 点击生成兑换码
   - 验证是否返回正确的 JSON 数据
3. 测试多植物查询：
   - 选择多个植物
   - 点击生成
   - 验证是否返回正确的兑换码
4. 测试多装扮查询：
   - 选择多个装扮
   - 点击生成
   - 验证是否返回正确的兑换码
5. 测试错误场景：
   - 断网测试
   - 查询不存在的植物
   - 验证错误提示是否正确显示

---

## 六、重要说明

### ✅ 保留内容
- 本地 .txt 文件全部保留（assets 目录）
- `loadLevels()` 等列表加载方法保留（用于显示可选项）
- ViewModel 层接口保持不变
- UI 层完全不需要修改

### ⚠️ 注意事项
1. 用户必须先激活才能使用查询功能（卡密不会为空）
2. 网络权限已在 AndroidManifest.xml 中配置
3. 使用 `usesCleartextTraffic="true"` 允许 HTTP 请求
4. 所有网络请求都在 IO 线程执行

### 🔄 数据流变化
**改造前**: 用户操作 → ViewModel → DataSource → 读取本地文件 → 返回数据  
**改造后**: 用户操作 → ViewModel → DataSource → 调用 API → 服务器查询 → 返回数据

---

## 七、编译状态
✅ 所有文件编译通过，无错误

## 八、下一步
1. 编译并运行应用
2. 使用测试卡密进行功能测试
3. 验证各种查询场景
4. 测试错误处理逻辑
5. 如有问题，查看 Logcat 日志（TAG: LevelDataSource, MultiPlantDataSource, MultiCostumeDataSource）
