# Pixel Art Filter Web

本專案使用Spring Boot及Vaadin框架，為 Pixel Art Filter 的 java 網頁版本

提供使用者上傳圖片、設定轉換參數、生成轉換後的圖片功能，使用者也可查看之前生成過的圖片，下載喜歡的圖片，在網頁方便的與原圖進行比較，如果有喜歡的照片，也可透過分享功能分享給他人

此外我們目前提供中英語言，未來可透過加入新的語言配置檔增加支持的語言

順應潮流，我們也提供了暗黑模式，可以依據使用者喜好自行切換

## 如何從源碼構建並部屬

本專案為 Maven 專案，使用者將源代碼下載後，指令可參考 `git clone git@github.com:keke125/pixel-art-filter-web.git`
，可透過在命令行輸入 `mvnw clean package -Pproduction` (Windows) 或 `./mvnw clean package -Pproduction` `sudo chmod +x mvnw` (Mac & Linux)
,此時將在 `target` 目錄下生成 JAR 檔案，接著將生成後的 JAR
檔案放到自訂的資料夾，並在該資料夾下執行 `java -jar target/pixel-art-filter-web-1.0-SNAPSHOT.jar`
，接著請在瀏覽器開啟 http://localhost:8080

如果想將檔案導入IDE，你也可以參考 [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (
Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

本專案使用 IntelliJ IDEA 開發

另外有兩件事是一定要注意的

- 資料庫
- OpenCV 的導入

## 資料庫

本專案使用 MariaDB，原則上你可方便的更改成你慣用的資料庫，請參考 Spring Data JPA 相關設定

為了使應用程式能夠持久儲存資料，請新增可連結的資料庫，可參考以下指令

```mysql
CREATE DATABASE newdatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'newdatabaseuser'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON newdatabase.* TO 'newdatabaseuser'@'localhost';
FLUSH PRIVILEGES;
```

資料庫設定後，請設定環境變數

- PAFW_DB_URL
- PAFW_DB_USER
- PAFW_DB_PASSWORD

範例

PAFW_DB_URL `jdbc:mariadb://localhost:3306/pafw` 其中 pafw 為資料庫名稱

## OpenCV 的導入

OpenCV 根據作業系統，CPU架構的不同，會需要不同的檔案

Linux may need compile, there are two files you need. One is opencv-460.jar, the other is libopencv_java460.so

```bash
mvn install:install-file -Dfile=/path/to/opencv/jarfile -DgroupId=org -DartifactId=opencv -Dversion=4.6.0 -Dpackaging=jar
```

### 開發環境

請在 IDE 設定 OpenCV 為外部函式庫

### 生產環境

在 Windows 下需要 .dll 檔，如果在放置 JAR 檔的資料夾內沒有放置相應的 .dll 檔 (x86/x64)，將導致錯誤

## Docker

TODO
