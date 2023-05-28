# Pixel Art Filter Web

## 簡介

Spring Boot 的版本為

本專案使用Spring Boot及Vaadin框架，為 Pixel Art Filter 的 Java 網頁版本

核心的像素濾鏡轉換程式代碼由 JingShing
提供，可參考其GitHub項目[JingShing/Pixel-Art-Filter-Java](https://github.com/JingShing/Pixel-Art-Filter-Java)

提供使用者上傳圖片、設定轉換參數、生成轉換後的圖片功能，使用者也可查看之前生成過的圖片，下載喜歡的圖片，在網頁方便的與原圖進行比較，也可透過觀察不同參數生成的效果，尋找自己喜歡的風格

此外我們目前提供中英語言，未來可透過加入新的語言配置檔增加支持的語言，不需對代碼進行大幅修改，保持擴充性

順應潮流，我們也提供了暗黑模式，可以依據使用者喜好自行切換

除了使用我們的官方版本外，你也可以自行部署本程式，此時你可以成為管理員，自行決定許多事，像是刪除使用者，為不同使用者設定不同的圖片容量限制...等等，而這些都可透過網頁操作

我們提供自行部署的使用者們可自訂的配置，你可透過配置檔調整程式的系統設定，像是監聽的端口，密碼選用哪種方式雜湊，上傳檔案的限制...等等

## 如何從源碼構建並部署

本專案為 Maven
專案，使用者將源代碼下載到本機後，指令可參考 `git clone https://github.com/keke125/pixel-art-filter-web.git`
，可透過在命令行輸入 `mvnw clean package -Pproduction` (Windows) 或 `.\mvnw clean package -Pproduction` (Windows
PowerShell) 或 `sudo chmod +x mvnw` (如遇到權限問題，執行本命令將調整權限為可執行) `./mvnw clean package -Pproduction` (
Mac & Linux)
編譯，此時將在 `target` 目錄下生成 JAR 檔案，除了 JAR 檔案，你還需要將配置檔(application.properties)一起放入程式根目錄，接著請參考
如何直接部署 章節

如果想將檔案導入IDE，你也可以參考 [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (
Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

## 如何直接部署，更推薦 Docker 部署

從最新版本中根據系統及處理器架構的不同選擇對應的壓縮檔，將壓縮檔解壓縮後進入該資料夾，由於圖片儲存為絕對路徑，請再次確認程式存放路徑

依照下面教學設定資料庫，並調整配置檔 (application.properties)，如果有使用官方 OpenCV 庫，請參考 OpenCV 的導入 (可選) 章節指令

執行

```bash
java -jar /path/to/.jar
```

接著請在瀏覽器開啟 http://localhost:8080，請注意，如果你有調整監聽端口，請將8080換成你自訂的端口

初始的管理員帳號密碼為 admin/admin ，請登入後至使用者檔案更改密碼

另外請注意使用者的圖片計算將只計算使用者上傳的圖片，生成後的效果圖大小並不會被計入，請在設定每位使用者的上限時特別留意

根據實測，通常生成後的圖片大小與原圖相比都不會太大

## 資料庫

本專案使用 MariaDB，原則上你可方便的更改成你慣用的資料庫，請參考 Spring Data JPA 相關設定

為了使應用程式能夠持久儲存資料，請新增可連結的資料庫，可參考以下指令，其中 newdatabase 為資料庫名稱， newdatabaseuser
為資料庫使用者， password 為資料庫使用者密碼

```mysql
CREATE DATABASE newdatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'newdatabaseuser'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON newdatabase.* TO 'newdatabaseuser'@'localhost';
FLUSH PRIVILEGES;
```

資料庫設定後，請設定環境變數，或是直接更改配置檔(請參考後續教學)

- PAFW_DB_URL

資料庫路徑，例如 `jdbc:mariadb://localhost:3306/pafw` 其中 pafw 為資料庫名稱

- PAFW_DB_USER

資料庫使用者，例如 `newdatabaseuser`

- PAFW_DB_PASSWORD

資料庫使用者密碼，例如 `password`

## OpenCV 的導入 (可選)

本專案使用的 OpenCV 版本為 4.6.0，使用 [openpnp/opencv](https://github.com/openpnp/opencv) 打包後的程式庫，你也可以自行導入官方版本

以下教學適用想要自行導入官方版本的使用者

OpenCV 根據作業系統、CPU架構的不同，會需要不同的檔案

在Linux的環境下你可能需要編譯OpenCV，你需要編譯後所得到的兩個檔案，一個是 opencv-460.jar ， 另一個是 libopencv_java460.so
，其中460表示opencv的版本，請根據需求調整

如果你需要編譯 OpenCV 的指令，你可以參考 opencv/docker-linux-install.sh (適用於Debian及Ubuntu)

在Windows的環境下可直接下載官方的檔案，包含 opencv-460.jar 及 opencv_java460.dll

以下指令為手動將OpenCV安裝為maven的依賴

```bash
mvn install:install-file -Dfile=/path/to/opencv/jarfile -DgroupId=org -DartifactId=opencv -Dversion=4.6.0 -Dpackaging=jar
```

並將 pom.xml 當中的

```xml
        <!-- https://mvnrepository.com/artifact/org.openpnp/opencv -->
<dependency>
    <groupId>org.openpnp</groupId>
    <artifactId>opencv</artifactId>
    <version>4.6.0-0</version>
</dependency>
```

替換成

```xml
        <!-- https://mvnrepository.com/artifact/opencv/opencv -->
<dependency>
    <groupId>org</groupId>
    <artifactId>opencv</artifactId>
    <version>4.6.0</version>
</dependency>
```

除了安裝為maven的依賴外，你還需要設定載入程式庫 (native libraries) 的路徑，也就是存放 .dll (Windows) 或 .jar (Linux) 的資料夾

### 開發環境 (使用IDE)

請在 IDE 外部函式庫 (External Libraries) 設定 OpenCV

### 生產環境 (打包成jar執行)

在 Windows 下需要 .dll 檔，請根據指令指定 .dll 的路徑

```bash
java -Djava.library.path=\path\to\.dll -jar \path\to\.jar
```

在 Linux 下需要 .so 檔，請根據指令指定 .so 的路徑

```bash
java -Djava.library.path=/path/to/.so -jar /path/to/.jar
```

請注意，\path\to\.dll 及 /path/to/.so 需要填的是存放 .dll 的資料夾，\path\to\.jar則需填包含jar檔名的完整路徑

你還需要調整 src/main/java/com/keke125/pixel/data/service/ImageService.java
的程式碼，將 `nu.pattern.OpenCV.loadLocally();` 替換成 `System.loadLibrary(Core.NATIVE_LIBRARY_NAME);`
並 import OpenCV `import org.opencv.core.Core;`

## 檔案結構參考，其中pafw為程式根目錄

```bash=
.
└── pafw
    ├── application.properties
    ├── pixel-art-filter-web-1.x.x.jar
    └── opencv_java460.dll / libopencv_java460.so  (Optional)
```

## 設定配置檔

- PAFW_DB_URL

資料庫路徑，例如 `jdbc:mariadb://db:3306/pafw` 其中 pafw 為資料庫名稱

- PAFW_DB_USER

資料庫使用者，例如 `pafw`

- PAFW_DB_PASSWORD

資料庫使用者密碼，例如 `changeme`

- spring.jpa.hibernate.ddl-auto

如果要在程式停止時刪除資料庫，請使用 `create-drop` ，適合開發環境
如果要在程式停止時保留資料庫，請使用 `update` ，適合生產環境

- spring.servlet.multipart.max-file-size

如果有調整上傳限制，請一併調整至單次最大上傳檔案總和，如`30MB`

- spring.servlet.multipart.max-request-size

如果有調整上傳限制，請一併調整至單次最大上傳檔案總和，如`30MB`

- app.maxImageSizeInMegaBytes

單一檔案最大上傳大小，單位為MB，如`10`，最高上限應為2047

- app.maxImageFiles

單次上傳的檔案數量上限，如`3`

- app.maxAvatarSizeInMegaBytes

使用者頭像的大小限制，單位為MB，如`3`

- app.newSignupImageSizeLimit

使用者註冊時預設的圖片大小總限制，單位為MB，如`30`

- app.idForEncode

要使用何種哈希(Hash)算法計算使用者的密碼，並將計算後的值存入資料庫
提供`BCrypt`、`pbkdf2`、`argon2`，預設為`argon2`

- app.webCountry

網站所在國家

- app.webNameEN

英文網站名

- app.webNameTC

中文網站名

- app.webDescriptionEN

英文網站敘述，於登入頁面顯示

- app.webDescriptionTC

中文網站敘述，於登入頁面顯示，請注意中文須輸入Unicode字碼，如`\u5982`

- app.webLink

網站網址，如`https://example.com`

- app.adminContactEmail

網站管理員聯絡信箱，如`admin@example.com`

- app.loginInfoTC

中文登入訊息，於登入頁面底部顯示，請注意中文須輸入Unicode字碼，如`\u5982`

- app.loginInfoEN

英文登入訊息，於登入頁面底部顯示

## Docker 部署

[Docker Hub](https://hub.docker.com/r/keke125/pixel-art-filter-web)
