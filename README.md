# Pixel Art Filter Web Java Version

[![Build Docker](https://github.com/keke125/pixel-art-filter-web/actions/workflows/maven-docker.yml/badge.svg)](https://github.com/keke125/pixel-art-filter-web/actions/workflows/maven-docker.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/72d781f4fe5a4583887c2d9f31399e12)](https://app.codacy.com/gh/keke125/pixel-art-filter-web/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
![Snyk Vulnerabilities for GitHub Repo](https://img.shields.io/snyk/vulnerabilities/github/keke125/pixel-art-filter-web)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fkeke125%2Fpixel-art-filter-web.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fkeke125%2Fpixel-art-filter-web?ref=badge_shield)

[![GitHub release](https://img.shields.io/github/release/keke125/pixel-art-filter-web.svg)](https://github.com/keke125/pixel-art-filter-web/releases/latest)
![GitHub language count](https://img.shields.io/github/languages/count/keke125/pixel-art-filter-web)
![GitHub top language](https://img.shields.io/github/languages/top/keke125/pixel-art-filter-web)

![GitHub Repo stars](https://img.shields.io/github/stars/keke125/pixel-art-filter-web?style=social)

Pixel Art Filter Web Java Version 為免費且開放原始碼的跨平臺網頁程式，提供使用者不同風格的像素濾鏡轉換功能。

## 簡介

本專案使用 `Spring Boot` 及 `Vaadin` 框架，為 Pixel Art Filter 的 Java 網頁版本。

核心的像素濾鏡轉換程式碼由 JingShing
提供，可參考其GitHub項目[JingShing/Pixel-Art-Filter-Java](https://github.com/JingShing/Pixel-Art-Filter-Java)。

提供使用者上傳圖片、透過設定轉換參數為圖片套上不同風格的像素濾鏡，使用者除了可查看、下載原始圖片及經轉換過後的效果圖，也可透過觀察不同參數生成的效果，尋找自己喜歡的風格。

此外我們目前提供中英語言，未來可透過加入新的語言配置檔增加支援的語言，不需對程式碼進行大幅修改，保持擴充性。

順應潮流，我們也提供了暗黑模式，可以依據使用者喜好自行切換。

除了使用我們的官方版本 [pafw.eu.org](https://pafw.eu.org)
外，你也可以自行部署本程式，此時你將成為管理員，擁有管理員權限，像是刪除使用者、更新使用者資訊、為不同使用者設定不同的圖片容量限制...等等，而這些操作都可透過網頁達成。

我們提供自行部署的使用者們可自訂的配置，你可透過配置檔調整程式的系統設定，像是監聽的連接埠，密碼選用哪種方式雜湊，上傳檔案的限制...等等。

## Demo

[pafw.eu.org](https://pafw.eu.org) 由 Pixel Art Filter Web 官方維護，提供使用者
Demo。

測試使用者帳號: `test` / 密碼: `testtest`。

![DigitalOcean](https://img.shields.io/badge/DigitalOcean-%230167ff.svg?style=for-the-badge&logo=digitalOcean&logoColor=white) ![Cloudflare](https://img.shields.io/badge/Cloudflare-F38020?style=for-the-badge&logo=Cloudflare&logoColor=white)

Demo 伺服器託管於 Digital Ocean 新加坡機房，使用 Cloudflare CDN 加速。

## 技術

### 資料庫

![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)

### 外部函式庫

![OpenCV](https://img.shields.io/badge/opencv-%23white.svg?style=for-the-badge&logo=opencv&logoColor=white)

### 框架

![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)

### IDE

![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

### 程式語言

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

### 專案管理

![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)

### 支援的作業系統

![Linux](https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black)

![Windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)

### 推薦使用網頁伺服器

![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white)

### 容器化

![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

### 軟體測試 (預計)

![Selenium](https://img.shields.io/badge/-selenium-%43B02A?style=for-the-badge&logo=selenium&logoColor=white)

### 版本控制

![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white)

![Gitea](https://img.shields.io/badge/Gitea-34495E?style=for-the-badge&logo=gitea&logoColor=5D9425)

![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)

### CI

![Dependabot](https://img.shields.io/badge/dependabot-025E8C?style=for-the-badge&logo=dependabot&logoColor=white)

![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)

## 組建

本專案為 Maven 專案，請參考以下指令組建

下載專案

```bash
git clone https://github.com/keke125/pixel-art-filter-web.git
```

移動至專案根目錄

```bash
cd pixel-art-filter-web
```

使用 `mvnw` 組建

Windows

```bash
mvnw clean package -Pproduction
```

Windows PowerShell

```bash
.\mvnw clean package -Pproduction
```

Mac & Linux

```bash
./mvnw clean package -Pproduction
```

此時在 `target` 資料夾底下，你會發現打包後的 JAR 檔案，接著請參考 [部署](#部署)
章節。

如果想將專案導入IDE，你也可以參考 [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (
Eclipse, IntelliJ IDEA, NetBeans, and VS Code)。

## 檔案結構

<pre>
.
└── pafw
    ├── application.properties
    ├── pixel-art-filter-web-1.x.x.jar
    └── opencv_java460.dll / libopencv_java460.so  (Optional)
</pre>

## 部署

### 下載程式

請根據作業系統及處理器架構選擇對應的壓縮檔下載(請根據需求調整下載連結):

- 使用 `wget` 下載

  ```bash
  wget https://github.com/keke125/pixel-art-filter-web/releases/download/v1.1.x/pafw-1.1.x-linux-amd64.tar.gz
  ```

-
也可選擇直接至 [GitHub Release](https://github.com/keke125/pixel-art-filter-web/releases/latest)
網頁下載

### 解壓縮到指定路徑

解壓縮檔案至指定的路徑(
⚠️由於圖片儲存為絕對路徑，移動程式資料夾將導致讀取圖片錯誤、⚠️絕對路徑中不可出現中文)
，請確認程式放置的路徑，並進入該資料夾

```bash
tar -xzvf pafw-1.1.x-linux-amd64.tar.gz -C /path/to/application
```

```bash
cd pafw-1.1.x-linux-amd64
```

### 建立資料庫

本專案使用 MariaDB，你也可以改成你喜歡的資料庫，請參考 `Spring Data JPA` 相關設定。

為了使程式能夠持久儲存資料，請參考以下指令新增資料庫，其中 `newdatabase`
為資料庫名稱， `newdatabaseuser`
為資料庫使用者， `password` 為資料庫使用者密碼。

```mariadb
CREATE DATABASE newdatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'newdatabaseuser'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON newdatabase.* TO 'newdatabaseuser'@'localhost';
FLUSH PRIVILEGES;
```

### 設定資料庫連線資訊

新增資料庫後，請設定資料庫連線資訊，你可以選擇設定環境變數，或是修改配置檔。

#### 使用環境變數

- PAFW_DB_URL

資料庫路徑，例如 `jdbc:mariadb://localhost:3306/pafw` 其中 `pafw` 為資料庫名稱。

- PAFW_DB_USER

資料庫使用者，例如 `newdatabaseuser`

- PAFW_DB_PASSWORD

資料庫使用者密碼，例如 `password`

#### 使用配置檔

請修改 `application.properties` (
位於程式根目錄底下，專案路徑為 `src/main/resources/application.properties` )。

- spring.datasource.url

資料庫路徑，例如 `jdbc:mariadb://localhost:3306/pafw` 其中 pafw 為資料庫名稱。

- spring.datasource.username

資料庫使用者，例如 `pafw`。

- spring.datasource.password

資料庫使用者密碼，例如 `password`。

### 設定配置檔 (可選)

如需額外系統設定，請參考 [配置檔](#配置檔) 章節。

### 執行程式

```bash
java -jar pixel-art-filter-web-1.x.x.jar
```

接著請在瀏覽器開啟 http://localhost:8080 ，如果你有調整監聽連接埠，請將`8080`
換成你自訂的連接埠。

初始的管理員帳號密碼為 `admin/admin` ，⚠️請登入後至使用者檔案更改密碼。

⚠️另外使用者的圖片計算將只計算使用者上傳的圖片，經轉換後的圖片大小並不會被計入，根據實測，通常轉換後的圖片大小與原圖相比都不會太大。

### Docker 部署

參考 [Docker Hub](https://hub.docker.com/r/keke125/pixel-art-filter-web)。

### OpenCV (可選)

本專案使用的 OpenCV 版本為 `4.6.0`
，使用 [openpnp/opencv](https://github.com/openpnp/opencv) 打包後的函式庫，你也可以改為使用
OpenCV 官方提供的函式庫。

#### Linux

在Linux的環境下你需要編譯 OpenCV ，編譯後取得 `opencv-460.jar`
及 `libopencv_java460.so` 兩個檔案。

如果你需要編譯 OpenCV
的指令，你可以參考 [docker-linux-install.sh](opencv/docker-linux-install.sh) (
適用於 `Debian` 及 `Ubuntu` )。

#### Windows

在Windows的環境下可至 [OpenCV](https://sourceforge.net/projects/opencvlibrary/files/4.6.0/opencv-4.6.0-vc14_vc15.exe/download)
官網下載，包含 `opencv-460.jar` 及 `opencv_java460.dll` 兩個檔案。

取得檔案後，輸入以下指令將 `OpenCV` 相依性( dependency )安裝到 Maven 專案:

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

除此之外，你還需要設定函式庫 (native libraries) 。

- 開發環境 (使用IDE)

請在 IDE 外部函式庫 (External Libraries) 設定 `OpenCV`。

- 生產環境 (打包成JAR執行)

#### Windows

請指定 `.dll` 檔的路徑:
```bash
java -Djava.library.path=\path\to\.dll -jar \path\to\.jar
```

#### Linux

請指定 `.so` 檔的路徑。
```bash
java -Djava.library.path=/path/to/.so -jar /path/to/.jar
```

⚠️`\path\to\.dll` 及 `/path/to/.so` 需要填的是儲存 .dll 或 .so
的資料夾，`\path\to\.jar` 則需填包含 JAR 檔名的完整路徑。

你還需要調整 `src/main/java/com/keke125/pixel/data/service/ImageService.java`
程式碼，將 `nu.pattern.OpenCV.loadLocally();`
替換成 `System.loadLibrary(Core.NATIVE_LIBRARY_NAME);`
並匯入 OpenCV `import org.opencv.core.Core;`。

### 配置檔

- spring.datasource.url

資料庫路徑，例如 `jdbc:mariadb://localhost:3306/pafw` 其中 pafw 為資料庫名稱。

- spring.datasource.username

資料庫使用者，例如 `pafw`。

- spring.datasource.password

資料庫使用者密碼，例如 `changeme`。

- spring.jpa.hibernate.ddl-auto

如果要在程式停止時刪除資料庫，請使用 `create-drop` ，適合開發環境，如果要在程式停止時保留資料庫，請使用 `update` ，適合生產環境。

- spring.servlet.multipart.max-file-size

如果有調整上傳限制，請一併調整至單次最大上傳檔案總和，如`30MB`。

- spring.servlet.multipart.max-request-size

如果有調整上傳限制，請一併調整至單次最大上傳檔案總和，如`30MB`。

- app.maxImageSizeInMegaBytes

單一檔案最大上傳大小，單位為MB，如`10`。

- app.maxImageFiles

單次上傳的檔案數量上限，如`3`。

- app.maxAvatarSizeInMegaBytes

使用者頭像的大小限制，單位為MB，如`3`。

- app.newSignupImageSizeLimit

使用者註冊時預設的圖片大小總限制，單位為MB，如`30`。

- app.idForEncode

要使用何種演算法雜湊(Hash)使用者的密碼，並將結果存入資料庫，提供`BCrypt`、`pbkdf2`、`argon2`，預設為`argon2`。

- app.webCountry

網站所在國家。

- app.webNameEN

英文網站名。

- app.webNameTC

中文網站名。

- app.webDescriptionEN

英文網站敘述，於登入頁面顯示。

- app.webDescriptionTC

中文網站敘述，於登入頁面顯示，請注意中文須輸入Unicode字碼，如`\u5982`。

- app.webLink

網站網址，如`https://example.com`。

- app.adminContactEmail

網站管理員聯絡信箱，如`admin@example.com`。

- app.loginInfoTC

中文登入訊息，於登入頁面底部顯示，請注意中文須輸入Unicode字碼，如`\u5982`。

- app.loginInfoEN

英文登入訊息，於登入頁面底部顯示。

## 授權條款

[![Licence](https://img.shields.io/github/license/keke125/pixel-art-filter-web?style=for-the-badge)](LICENSE)


## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fkeke125%2Fpixel-art-filter-web.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fkeke125%2Fpixel-art-filter-web?ref=badge_large)